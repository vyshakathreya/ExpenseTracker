package edu.sdsu.vyshak.personalexpensetracker.bean;

/**
 * Created by Vyshak on 5/12/2017.
 * This class monitors the budget and bill payments.
 * Helps to alert user when a budget condition is met or bill is upcoming
 */

public class UserAlerts {
    private String duedate;
    private String duebill;

    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String duedate) {
        this.duedate = duedate;
    }

    public String getDuebill() {
        return duebill;
    }

    public void setDuebill(String duebill) {
        this.duebill = duebill;
    }
}
