package edu.sdsu.vyshak.personalexpensetracker;

/**
 * Created by vysha on 5/9/2017.
 */

class Budget {
    String category;
    Double amount;
    String cycle;

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
