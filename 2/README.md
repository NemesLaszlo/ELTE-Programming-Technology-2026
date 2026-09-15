### Association, Aggregation and Composition

---

### Definitions:

An `Association` can be considered a generic term to indicate the relationship between two independent classes; 
the relationship may be one-to-one, one-to-many, or many-to-many, but it need not indicate ownership.

`Aggregation` is a specific form of association in which one class, the whole, contains a collection of other classes, 
the parts; here, however, the lifecycle of the parts does not depend upon the whole. 
For example, a library and books show aggregation, since books may exist somewhere apart from the library.

In contrast, `Composition` is a stronger form of aggregation that means ownership and lifecycle dependence; 
if the whole gets destroyed, then the parts no longer exist. 
For composition, a good example would be a house and its different rooms; a room cannot exist without a house.

---

### Association Example:

```java
import java.io.*;
import java.util.*;

// Class 1
// Bank class
class Bank {

    // Attributes of bank
    private String bankName;
    private Set<Employee> employees;
  
    // Constructor of Bank class
    public Bank(String bankName)
    {
        this.bankName = bankName;
    }

    // Method of Bank class
    public String getBankName()
    {
        // Returning name of bank
        return this.bankName;
    }

    public void setEmployees(Set<Employee> employees)
    {
        this.employees = employees;
    }
  
    public Set<Employee> getEmployees()
    {
        return this.employees;
    }
}

// Class 2
// Employee class
class Employee {
  
    // Attributes of employee
    private String name;
  
    // Constructor of Employee class
    public Employee(String name)
    {
        // this keyword refers to current instance
        this.name = name;
    }

    // Method of Employee class
    public String getEmployeeName()
    {
        // returning the name of employee
        return this.name;
    }
}

// Class 3
// Association between both the
// classes in main method
class AssociationExample {

    // Main driver method
    public static void main(String[] args)
    {
        // Creating Employee objects
        Employee emp1 = new Employee("Ridhi");
          Employee emp2 = new Employee("Vijay");
        
          // adding the employees to a set
        Set<Employee> employees = new HashSet<>();
        employees.add(emp1);
          employees.add(emp2);

          // Creating a Bank object
          Bank bank = new Bank("ICICI");
      
          // setting the employees for the Bank object
        bank.setEmployees(employees);
        
          // traversing and displaying the bank employees 
          for (Employee emp : bank.getEmployees()) {
              System.out.println(emp.getEmployeeName()
                                 + " belongs to bank "
                                 + bank.getBankName());
        }
    }
}
```

In the above example, two separate classes Bank and Employee are associated through their Objects. Bank can have many employees, So, it is a one-to-many relationship. 

---

### Aggregation Example:

```java
import java.io.*;
import java.util.*;

// Class 1
// Student class
class Student {

    // Attributes of Student
    private String studentName;
    private int studentId;

    // Constructor of Student class
    public Student(String studentName, int studentId)
    {
        this.studentName = studentName;
        this.studentId = studentId;
    }

    public int getstudentId() { 
      return studentId; 
    }

    public String getstudentName() {
      return studentName; 
    }
}

// Class 2
// Department class 
// Department class contains list of Students
class Department {

    // Attributes of Department class
    private String deptName;
    private List<Student> students;

    // Constructor of Department class
    public Department(String deptName, List<Student> students)
    {
        this.deptName = deptName;
        this.students = students;
    }

    public List<Student> getStudents() {
      return students; 
    }

    public void addStudent(Student student)
    {
        students.add(student);
    }
}

// Class 3
// Institute class
// Institute class contains the list of Departments
class Institute {

    // Attributes of Institute
    private String instituteName;
    private List<Department> departments;

    // Constructor of Institute class
    public Institute(String instituteName,
                     List<Department> departments)
    {
        // This keyword refers to current instance itself
        this.instituteName = instituteName;
        this.departments = departments;
    }

    public void addDepartment(Department department)
    {
        departments.add(department);
    }

    // Method of Institute class
    // Counting total students in the institute
    public int getTotalStudentsInInstitute()
    {
        int noOfStudents = 0;
        List<Student> students = null;

        for (Department dept : departments) {
            students = dept.getStudents();

            for (Student s : students) {
                noOfStudents++;
            }
        }
        return noOfStudents;
    }
}

// Class 4
// main class
class AggregationExample {

    // main driver method
    public static void main(String[] args)
    {
        // Creating independent Student objects
        Student s1 = new Student("Parul", 1);
        Student s2 = new Student("Sachin", 2);
        Student s3 = new Student("Priya", 1);
        Student s4 = new Student("Rahul", 2);

        // Creating an list of CSE Students
        List<Student> cse_students = new ArrayList<Student>();
        cse_students.add(s1);
        cse_students.add(s2);

        // Creating an initial list of EE Students
        List<Student> ee_students = new ArrayList<Student>();
        ee_students.add(s3);
        ee_students.add(s4);

        // Creating Department object with a Students list
        // using Aggregation (Department "has" students)
        Department CSE = new Department("CSE", cse_students);
        Department EE = new Department("EE", ee_students);

        // Creating an initial list of Departments
        List<Department> departments = new ArrayList<Department>();
        departments.add(CSE);
        departments.add(EE);

        // Creating an Institute object with Departments list
        // using Aggregation (Institute "has" Departments)
        Institute institute = new Institute("BITS", departments);

        // Display message for better readability
        System.out.print("Total students in institute: ");

        // Calling method to get total number of students
        // in the institute and printing on console
        System.out.print(
            institute.getTotalStudentsInInstitute());
    }
}
```

In this example,

- There is an Institute which has no. of departments like CSE, EE. Every department has no. of students.
- So, we make an Institute class that has a reference to Object or no. of Objects (i.e. List of Objects) of the Department class.
- That means Institute class is associated with Department class through its Object(s).
- And Department class has also a reference to Object or Objects (i.e. List of Objects) of the Student class means it is associated with the Student class through its Object(s).

---

### Composition Example:

```java
import java.io.*;
import java.util.*;

// Class 1
// Department class
class Department {

    // Attributes of Department
    public String departmentName;

    // Constructor of Department class
    public Department(String departmentName)
    {
        this.departmentName = departmentName;
    }

    public String getDepartmentName()
    {
        return departmentName;
    }
}

// Class 2
// Company class
class Company {

    // Reference to refer to list of books
    private String companyName;
    private List<Department> departments;

    // Constructor of Company class
    public Company(String companyName)
    {
        this.companyName = companyName;
        this.departments = new ArrayList<Department>();
    }

    // Method
    // to add new Department to the Company
    public void addDepartment(Department department)
    {
        departments.add(department);
    }

    public List<Department> getDepartments()
    {
        return new ArrayList<>(departments);
    }

    // Method
    // to get total number of Departments in the Company
    public int getTotalDepartments()
    {
        return departments.size();
    }
}

// Class 3
// Main class
class CompositonExample {

    // Main driver method
    public static void main(String[] args)
    {
        // Creating a Company object
        Company techCompany = new Company("Tech Corp");

        techCompany.addDepartment(new Department("Engineering"));
        techCompany.addDepartment(new Department("Operations"));
        techCompany.addDepartment(new Department("Human Resources"));
        techCompany.addDepartment(new Department("Finance"));

        int d = techCompany.getTotalDepartments();
        System.out.println("Total Departments: " + d);

        System.out.println("Department names: ");
        for (Department dept : techCompany.getDepartments()) {
            System.out.println("- " + dept.getDepartmentName());
        }
    }
}
```

In the above example,

- A company can have no. of departments.
- All the departments are part-of the Company.
- So, if the Company gets destroyed then all the Departments within that particular Company will be destroyed, i.e. Departments can not exist independently without the Company.
- That’s why it is composition. Department is Part-of Company.