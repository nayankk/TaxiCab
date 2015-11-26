package com.example.anishmuthali.signupexample;

/**
 * Created by anishmuthali on 11/25/15.
 */
public class User {
    private int driverId;
    private String name;
    private String carType;
    public User() {}
    public User(String name, int driverId, String carType) {
        this.name = name;
        this.driverId = driverId;
        this.carType = carType;
    }
    public int getDriverId() {
        return driverId;
    }
    public String getName() {
        return name;
    }
    public String getCarType(){return carType;}
}
