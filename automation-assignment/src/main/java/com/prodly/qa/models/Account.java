package com.prodly.qa.models;

import java.util.HashMap;
import java.util.Map;

public class Account {
    private String id;
    private String name;
    private String country;
    private Integer numberOfEmployees;
    private String phone;

    public Account() {}

    public Account(String name, String country, Integer numberOfEmployees) {
        this.name = name;
        this.country = country;
        this.numberOfEmployees = numberOfEmployees;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Integer getNumberOfEmployees() { return numberOfEmployees; }
    public void setNumberOfEmployees(Integer numberOfEmployees) { this.numberOfEmployees = numberOfEmployees; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    /** Map for Salesforce sObject fields */
    public Map<String,Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        m.put("Name", name);
        m.put("BillingCountry", country);
        m.put("NumberOfEmployees", numberOfEmployees);
        if (phone != null) m.put("Phone", phone);
        return m;
    }
}
