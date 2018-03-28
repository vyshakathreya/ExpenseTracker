package edu.sdsu.vyshak.personalexpensetracker.bean;

/**
 * Created by Vyshak on 5/8/2017.
 * This class has the parameters associated with the user.
 */

public class User {
    String uid;
    String currency;

    String email;

    String name;

    String phone;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
