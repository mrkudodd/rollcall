package com.example.kdar.rollcall2.model;

import java.util.ArrayList;

public class Classes {
    private String nameClass;
    private ArrayList<Student> students;

    public Classes(String nameClass){
        this.nameClass = nameClass;
    }

    public Classes(String nameClass ,ArrayList<Student> students) {
        this.nameClass = nameClass;
        this.students = students;
    }

    public String getNameClass() {
        return nameClass;
    }

    public void setNameClass(String nameClass) {
        this.nameClass = nameClass;
    }

    public ArrayList<Student> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<Student> students) {
        this.students = students;
    }
}
