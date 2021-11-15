package curtin.edu.mathtest.classes;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Student implements Serializable {
    private String id;
    private String firstName;
    private String lastName;
    private List<String> phone;
    private  List<String> email;

    public Student(String id, String firstName, String lastName, List<String> phone,  List<String> email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }

    public Student(String firstName, String lastName, List<String> phone,  List<String> email) {
        this (UUID.randomUUID().toString(), firstName, lastName, phone, email);
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getPhone() {
        return phone;
    }

    public void setPhone( List<String> phone) {
        this.phone = phone;
    }

    public  List<String> getEmail() {
        return email;
    }

    public void setEmail( List<String> email) {
        this.email = email;
    }
}
