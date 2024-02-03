package Helpers;

import java.io.PrintWriter;

public class Order {
    private String message = "";
    private final int customerID;
    private final String customerName;
    private final int[] products = {0,0}; // index 0 is for coffee and 1 for tea
    private final PrintWriter writer;

    public Order(String customerName, int customerID, PrintWriter writer) {
        this.customerName = customerName;
        this.customerID = customerID;
        this.writer = writer;
    }

    public void sendToCustomer(String msg) {
        writer.println(msg);
    }
    // Setters

    public void setTeaToZero(){ products[1] = 0; }

    public void setCoffeeToZero(){ products[0] = 0; }

    public void addTea(int numberOfTea){ products[1] += numberOfTea; }

    public void setMessage(String message) { this.message = message; }

    public void addCoffee(int numberOfCoffee){ products[0] += numberOfCoffee; }

    // Getters

    public String getMessage() { return message; }

    public int getNumberOfTea(){return products[1];}

    public int getCustomerID() { return customerID; }

    public int getNumberOfCoffee(){return products[0];}

    public String getCustomerName() {return customerName;}


}
