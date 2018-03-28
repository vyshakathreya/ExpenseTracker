package edu.sdsu.vyshak.personalexpensetracker.bean;

/**
 * Created by Vyshak on 5/9/2017.
 * This class hosts budget parameters.
 */

public class Budget {
    private String category;
    private Double amount;
    private String cycle;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }
}
