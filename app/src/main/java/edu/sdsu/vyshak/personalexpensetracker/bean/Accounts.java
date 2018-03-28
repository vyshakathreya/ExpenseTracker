package edu.sdsu.vyshak.personalexpensetracker.bean;

/**
 * Created by Vyshak on 5/6/2017.
 * This class represents the parameters for the user accounts.
 */

public class Accounts {
    private String accountName;
    private String accountType;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

}
