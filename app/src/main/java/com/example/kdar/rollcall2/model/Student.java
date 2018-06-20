package com.example.kdar.rollcall2.model;

public class Student {
    private String name;
    private String number;
    private boolean rolled;

    public Student() {
    }

    public Student(String name, String number, boolean rolled) {
        this.name = name;
        this.number = number;
        this.rolled = rolled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isRolled() {
        return rolled;
    }

    public void setRolled(boolean rolled) {
        this.rolled = rolled;
    }
}
