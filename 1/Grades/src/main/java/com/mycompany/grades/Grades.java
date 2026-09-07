package com.mycompany.grades;

import java.util.ArrayList;

public class Grades {

    public static void main(String[] args) {
        
         ArrayList<Student> students = new ArrayList<>();
         
        students.add(new Student("John Doe", "American", 3.8));
        students.add(new Student("Jane Doe", "Canadian", 4.0));
        students.add(new Student("Dora Smith", "British", 4.2));
        students.add(new Student("Alice Johnson", "British", 3.5));
        students.add(new Student("Bob Smith", "Australian", 2.8));
        students.add(new Student("Carlos Hernandez", "Mexican", 3.2));
        
        Student bestStudent = students.get(0);
        Student worstStudent = students.get(0);
        
        for (Student student : students) {
            if (student.getGradeAverage() > bestStudent.getGradeAverage()) {
                bestStudent = student;
            }
            if (student.getGradeAverage() < worstStudent.getGradeAverage()) {
                worstStudent = student;
            }
        }
        
        System.out.println("Student with the best grade average:");
        System.out.println("Name: " + bestStudent.getName());
        System.out.println("Nationality: " + bestStudent.getNationality());
        System.out.println("Grade Average: " + bestStudent.getGradeAverage());
        
        System.out.println("\nStudent with the worst grade average:");
        System.out.println("Name: " + worstStudent.getName());
        System.out.println("Nationality: " + worstStudent.getNationality());
        System.out.println("Grade Average: " + worstStudent.getGradeAverage());
        
        System.out.println("\nStudents with a grade average of 4.0 or higher:");
        for (Student student : students) {
            if (student.getGradeAverage() >= 4.0) {
                System.out.println(student.getName());
            }
        }
    }
}
