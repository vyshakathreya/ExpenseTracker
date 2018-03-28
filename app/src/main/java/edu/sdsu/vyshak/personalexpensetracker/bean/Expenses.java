package edu.sdsu.vyshak.personalexpensetracker.bean;

/**
 * Created by Vyshak on 5/3/2017.
 * This class monitors all the paraments related to user expenses
 */

public class Expenses  {
    private String account;
    private String userId;
    private float amount;
    private String currency;
    private String transtype;
    private String category;
    private String date;
    private String desc;
    private String time;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTranstype() {
        return transtype;
    }

    public void setTranstype(String transtype) {
        this.transtype = transtype;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime(){
        return time;
    }
}

