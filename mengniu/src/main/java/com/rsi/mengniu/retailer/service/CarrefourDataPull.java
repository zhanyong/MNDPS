package com.rsi.mengniu.retailer.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rsi.mengniu.retailer.module.User;
import com.rsi.mengniu.util.OCR;
import com.rsi.mengniu.util.Utils;

//http://supplierweb.carrefour.com.cn/
public class CarrefourDataPull implements RetailerDataPull {
	private static Log log = LogFactory.getLog(CarrefourDataPull.class);

	public void dataPull(User user) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String loginResult = null;
		int loginCount = 0; // 如果验证码出错重新login,最多20次
		try {
			do {
				loginResult = login(httpClient, user);
				loginCount++;
			} while ("InvalidCode".equals(loginResult) && loginCount < 20);
			// Invalid Password and others
			if (!"Success".equals(loginResult)) {
				return;
			}
			// receive
			// getReceiveExcel(httpClient);

			getOrder(httpClient);
		} catch (Exception e) {
			log.error(Utils.getTrace(e));
		}
	}

	public String login(CloseableHttpClient httpClient, User user) throws Exception {
		HttpGet httpGet = new HttpGet("http://supplierweb.carrefour.com.cn/includes/image.jsp");
		String imgName = String.valueOf(java.lang.System.currentTimeMillis());

		FileOutputStream fos = new FileOutputStream("/Users/haibin/Documents/temp/" + imgName + ".jpg");
		CloseableHttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		entity.writeTo(fos);
		response.close();
		fos.close();
		String recognizeStr = OCR.getInstance().recognizeText("/Users/haibin/Documents/temp/" + imgName + ".jpg",
				"/Users/haibin/Documents/temp/" + imgName);
		recognizeStr = recognizeStr.replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", "");
		// login /login.do?action=doLogin
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add(new BasicNameValuePair("login", user.getUserId()));
		formParams.add(new BasicNameValuePair("password", user.getPassword())); // 错误的密码
		formParams.add(new BasicNameValuePair("validate", recognizeStr));
		HttpEntity loginEntity = new UrlEncodedFormEntity(formParams, "UTF-8");
		HttpPost httppost = new HttpPost("http://supplierweb.carrefour.com.cn/login.do?action=doLogin");
		httppost.setEntity(loginEntity);
		CloseableHttpResponse loginResponse = httpClient.execute(httppost);
		String responseStr = EntityUtils.toString(loginResponse.getEntity());
		if (responseStr.contains("验证码失效")) {
			log.error("验证码失效,Relogin...");
			return "InvalidCode";
		} else if (responseStr.contains("错误的密码")) {
			log.error("错误的密码,退出!" + user);
			return "InvalidPassword";
		} else if (responseStr.contains("系统出错")) {
			log.error("系统出错,退出!");
			return "SystemError";
		}
		loginResponse.close();

		return "Success";
	}

	public void getReceiveExcel(CloseableHttpClient httpClient) throws Exception {
		// goMenu('inyr.do?action=query','14','预估进退查询')

		// $('inyrForm').action="inyr.do?action=export";
		// 供应商预估进退查询/Supplier Inyr Inquiry
		List<NameValuePair> receiveformParams = new ArrayList<NameValuePair>();
		receiveformParams.add(new BasicNameValuePair("unitid", "ALL"));
		receiveformParams.add(new BasicNameValuePair("butype", "byjv"));
		receiveformParams.add(new BasicNameValuePair("systemdate", "2013/12/22")); // yyyy/mm/dd
		HttpEntity receiveFormEntity = new UrlEncodedFormEntity(receiveformParams, "UTF-8");
		HttpPost receivePost = new HttpPost("http://supplierweb.carrefour.com.cn/inyr.do?action=export");
		receivePost.setEntity(receiveFormEntity);
		CloseableHttpResponse receiveRes = httpClient.execute(receivePost);
		String responseStr = EntityUtils.toString(receiveRes.getEntity());
		receiveRes.close();

		if (responseStr.contains("Excel文档生成成功")) {
			// Download Excel file
			responseStr = responseStr.substring(responseStr.indexOf("javascript:downloads('") + 22);
			String inyrFileName = responseStr.substring(0, responseStr.indexOf("'"));
			FileOutputStream receiveFos = new FileOutputStream("/Users/haibin/Documents/temp/" + inyrFileName);

			List<NameValuePair> downloadformParams = new ArrayList<NameValuePair>();
			downloadformParams.add(new BasicNameValuePair("filename", inyrFileName));
			downloadformParams.add(new BasicNameValuePair("filenamedownload", "excelpath"));
			HttpEntity downloadFormEntity = new UrlEncodedFormEntity(downloadformParams, "UTF-8");
			HttpPost downloadPost = new HttpPost("http://supplierweb.carrefour.com.cn/download.jsp");
			downloadPost.setEntity(downloadFormEntity);
			CloseableHttpResponse downloadRes = httpClient.execute(downloadPost);
			downloadRes.getEntity().writeTo(receiveFos);
			downloadRes.close();
			receiveFos.close();

		} else {
			log.error("Carrefour export Excel Error!");
		}

	}

	public void getOrder(CloseableHttpClient httpClient) throws Exception {
		// forward to PowerE2E Platform
		HttpGet httpGet = new HttpGet("http://supplierweb.carrefour.com.cn/callSSO.jsp");
		CloseableHttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		if (!EntityUtils.toString(entity).contains("PowerE2E Platform")) {
			log.error("Carrefour get order error,cannot forward to PowerE2E Platform");
			return;
		}
		response.close();

		// https://platform.powere2e.com/platform/mailbox/openInbox.htm?
		List<NameValuePair> searchformParams = new ArrayList<NameValuePair>();
		searchformParams.add(new BasicNameValuePair("receivedDateFrom", "03-01-2014"));
		searchformParams.add(new BasicNameValuePair("receivedDateTo", "03-01-2014"));
		HttpEntity searchFormEntity = new UrlEncodedFormEntity(searchformParams, "UTF-8");
		HttpPost searchPost = new HttpPost("https://platform.powere2e.com/platform/mailbox/openInbox.htm?");
		searchPost.setEntity(searchFormEntity);
		CloseableHttpResponse searchRes = httpClient.execute(searchPost);
		String responseStr = EntityUtils.toString(searchRes.getEntity());
		searchRes.close();
		Document doc = Jsoup.parse(responseStr);
		Elements msgIds = doc.select("input[name=msgId]");
		for (Element msgId: msgIds) {
			HttpGet httpOrderGet = new HttpGet("https://platform.powere2e.com/platform/mailbox/performDocAction.htm?actionId=1&guid="+msgId.attr("value"));
			CloseableHttpResponse orderRes = httpClient.execute(httpOrderGet);
			String orderDetail = EntityUtils.toString(orderRes.getEntity());
			orderRes.close();
			System.out.println(orderDetail);
		}
//		Element dataTable = doc.select("table.tbllist").first();
//		for (Element row : dataTable.select("tr:gt(0)")) {
//			Elements tds = row.select("td:not([rowspan])");
//			System.out.println(tds.get(0).text() + "->" + tds.get(1).text());
//		}
		// viewMessage('/platform', 'C4net2--56f631-1435654b8f5-f528764d624db129b32c21fbca0cb8d6');
		// location.href = (applicationContext + "/mailbox/performDocAction.htm?guid=" + guid + "&actionId=" + 1);

	}

}