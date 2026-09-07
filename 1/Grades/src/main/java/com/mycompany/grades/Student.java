package com.mycompany.grades;

public class Student {
    
    private String name;
    private String nationality;
    private double gradeAverage;
    
    public Student(String name, String nationality, double gradeAverage) {
        this.name = name;
        this.nationality = nationality;
        this.gradeAverage = gradeAverage;
    }
    
    public String getName() {
        return name;
    }

    public String getNationality() {
        return nationality;
    }

    public double getGradeAverage() {
        return gradeAverage;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setGradeAverage(double gradeAverage) {
        this.gradeAverage = gradeAverage;
    }
    
}
