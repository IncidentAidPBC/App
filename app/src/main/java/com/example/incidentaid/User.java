package com.example.incidentaid;

import java.io.Serializable;

public class User implements Serializable {

    private String name, email, firestation, role, address, pincode, qualification, job_title, alert;
    private  String token, online, captain, personnel, date, time, notification, latitude, longitude, note_reference, status;


    public User(String name, String email, String qualification, String job_title, String address, String pincode, String firestation, String role, String token, String online) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.pincode = pincode;
        this.qualification = qualification;
        this.job_title = job_title;
        this.firestation = firestation;
        this.role = role;
        this.token = token;
        this.online = online;
    }


    public String getCaptain() {
        return captain;
    }

    public void setCaptain(String captain) {
        this.captain = captain;
    }

    public void setPersonnel(String personnel) {
        this.personnel = personnel;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPersonnel() {
        return personnel;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getNotification() {
        return notification;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setNote_reference(String note_reference) {
        this.note_reference = note_reference;
    }

    public String getNote_reference() {
        return note_reference;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getAlert() {
        return alert;
    }

    public User(String address, String captain, String personnel, String date, String time, String latitude, String longitude, String note_reference, String status, String notification, String alert) {
        this.address = address;
        this.captain = captain;
        this.personnel = personnel;
        this.date = date;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note_reference = note_reference;
        this.status = status;
        this.notification = notification;
        this.alert = alert;
    }

    public String getQualification() {
        return qualification;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getOnline() {
        return online;
    }

    public void setToken(String ttoken) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getJob_title() {
        return job_title;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public void setJob_title(String job_title) {
        this.job_title = job_title;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getAddress() {
        return address;
    }

    public String getPincode() {
        return pincode;
    }

    public String getFirestation() {
        return firestation;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirestation(String firestation) {
        this.firestation = firestation;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
