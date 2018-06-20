package com.example.kdar.rollcall2.model;

public class Roll {
    private String day;
    private boolean rolled;

    public Roll(){

    }

    public Roll(String day, boolean rolled) {
        this.day = day;
        this.rolled = rolled;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public boolean isRolled() {
        return rolled;
    }

    public void setRolled(boolean rolled) {
        this.rolled = rolled;
    }
}
