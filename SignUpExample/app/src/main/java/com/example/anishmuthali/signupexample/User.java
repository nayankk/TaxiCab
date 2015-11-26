package com.example.anishmuthali.signupexample;

/**
 * Created by anishmuthali on 11/25/15.
 */
public class User {
    private int driverId;
    private String name;
    public User() {}
    public User(String name, int driverId) {
        this.name = name;
        this.driverId = driverId;
    }
    public int getDriverId() {
        return driverId;
    }
    public String getName() {
        return name;
    }
}
