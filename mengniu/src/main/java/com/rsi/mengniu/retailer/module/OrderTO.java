package com.rsi.mengniu.retailer.module;

public class OrderTO extends BaseTO {

	private static final long serialVersionUID = -715249571306618131L;

	private String orderNo=""; // 1
	private String orderDate="";// 2
	private String storeID=""; // 3
	private String storeName="";// 4
	private String itemID=""; // 5
	private String itemName=""; // 6
	private String barcode="";// 7
	private String quantity="";// 8
	private String unitPrice="";// 9
	private String totalPrice="";// 10
	private String userID="";// 11


	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getStoreID() {
		return storeID;
	}

	public void setStoreID(String storeID) {
		this.storeID = storeID;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(String unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(String totalPrice) {
		this.totalPrice = totalPrice;
	}
	
	

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public OrderTO() {
		super();
	}

	public String toString() {
		return this.orderNo + "\t" + this.orderDate + "\t" + this.storeID
				+ "\t" + this.storeName + "\t" + this.itemID + "\t"
				+ this.itemName + "\t" + this.barcode + "\t" + this.quantity
				+ "\t" + this.unitPrice + "\t" + this.totalPrice;
		//+ "\t"				+ this.userID;
	}

	/*
	 * return this.orderNo + "\t" + this.orderDate + "\t" + this.storeNo + "\t"
	 * + this.storeName + "\t" + this.itemCode + "\t" + this.itemName + "\t"+
	 * this.barcode + "\t" + this.quantity + "\t" + this.unitPrice + "\t" +
	 * this.totalPrice;
	 */

	public OrderTO(String orderString) {
		String[] orderFieldArray = orderString.split("\t",-1);

		this.setOrderNo(orderFieldArray[0]);
		this.setOrderDate(orderFieldArray[1]);
		this.setStoreID(orderFieldArray[2]);
		this.setStoreName(orderFieldArray[3]);
		this.setItemID(orderFieldArray[4]);
		this.setItemName(orderFieldArray[5]);
		this.setBarcode(orderFieldArray[6]);
		this.setQuantity(orderFieldArray[7]);
		this.setUnitPrice(orderFieldArray[8]);
		this.setTotalPrice(orderFieldArray[9]);
		//this.setUserID(orderFieldArray[10]);

	}

}
