package Helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderProcessor {
    private static Path path = Paths.get("log.json");
    private final AtomicInteger userID = new AtomicInteger(0);
    private final LinkedBlockingQueue<ItemDetails> teaTrayArea = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<Integer, Order> customers = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<ItemDetails> coffeeTrayArea = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ItemDetails> teaWaitingArea = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ItemDetails> coffeeWaitingArea = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ItemDetails> teaBrewingArea = new LinkedBlockingQueue<>(2);
    private final LinkedBlockingQueue<ItemDetails> coffeeBrewingArea = new LinkedBlockingQueue<>(2);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();

    // Threads

    // looping through the list and calculating the values of each tea and coffee and comparing it with the initial
    // value saved in the order class
    Runnable serverServing = () -> {
        while (true){
            for (Order i : customers.values()) {
                System.out.println("loop");
                if (i.getNumberOfTea() + i.getNumberOfCoffee() != 0
                        && getNumberOfProducts(i.getCustomerID(), coffeeTrayArea.iterator()) == i.getNumberOfCoffee()
                        && getNumberOfProducts(i.getCustomerID(), teaTrayArea.iterator()) == i.getNumberOfTea()) {

                    i.sendToCustomer("order delivered to " + i.getCustomerName() + " (" +
                            structureNumbers(i.getNumberOfCoffee(), i.getNumberOfTea()) + ")");
                    System.out.println(getLogDetails());
                    teaTrayArea.removeIf(x -> x.getNumber() == i.getCustomerID());
                    coffeeTrayArea.removeIf(x -> x.getNumber() == i.getCustomerID());
                    i.setCoffeeToZero();
                    i.setTeaToZero();
                    System.out.println(getLogDetails());
                }
            }}
    };

    Runnable teaSeverMaking = () -> {
        while (true)
            serverMaking(teaWaitingArea, teaBrewingArea, teaTrayArea, 30000);
    };

    Runnable coffeeSeverMaking = () -> {
        while (true)
            serverMaking(coffeeWaitingArea, coffeeBrewingArea, coffeeTrayArea, 45000);
    };

    // Methods

    public void startAllThreads() {
        //2 different threads to brew two at a time
        new Thread(coffeeSeverMaking).start();
        new Thread(coffeeSeverMaking).start();

        new Thread(teaSeverMaking).start();
        new Thread(teaSeverMaking).start();

        //new Thread(serverServing).start();
    }

    public boolean isOrderCompleted(ItemDetails itemDetails)
    {
        Order i = customers.get(itemDetails.getNumber());
        if (i.getNumberOfTea() + i.getNumberOfCoffee() != 0
                && getNumberOfProducts(i.getCustomerID(), coffeeTrayArea.iterator()) == i.getNumberOfCoffee()
                && getNumberOfProducts(i.getCustomerID(), teaTrayArea.iterator()) == i.getNumberOfTea()) {

            i.sendToCustomer("order delivered to " + i.getCustomerName() + " (" +
                    structureNumbers(i.getNumberOfCoffee(), i.getNumberOfTea()) + ")");
            System.out.println(getLogDetails());
            teaTrayArea.removeIf(x -> x.getNumber() == i.getCustomerID());
            coffeeTrayArea.removeIf(x -> x.getNumber() == i.getCustomerID());
            i.setCoffeeToZero();
            i.setTeaToZero();
            System.out.println(getLogDetails());
            return true;
        }
        return false;
    }

    // Removing all items from waiting area, so it is not added to the brewing area, then checking what can be reused
    // for each item
    public void close(int customerID) {
        teaWaitingArea.removeIf(x -> x.getNumber() == customerID);
        coffeeWaitingArea.removeIf(x -> x.getNumber() == customerID);

        transferItem(teaBrewingArea, teaWaitingArea, true, customerID);
        transferItem(coffeeBrewingArea, coffeeWaitingArea, true, customerID);
        transferItem(teaTrayArea, teaWaitingArea, false, customerID);
        transferItem(coffeeTrayArea, coffeeWaitingArea, false, customerID);

        teaBrewingArea.removeIf(x -> x.getNumber() == customerID);
        teaTrayArea.removeIf(x -> x.getNumber() == customerID);
        coffeeBrewingArea.removeIf(x -> x.getNumber() == customerID);
        coffeeTrayArea.removeIf(x -> x.getNumber() == customerID);

        customers.remove(customerID);

        if (customers.size() == 0)
            userID.set(0);

        System.out.println(getLogDetails());

    }

    // Whenever a client leaves, the server checks if any of their brewing or tray items can be repurposed
    // for orders belonging to other clients. it also stops the server from adding to brewing area
    private void transferItem(LinkedBlockingQueue<ItemDetails> area,
                              LinkedBlockingQueue<ItemDetails> waitingArea,
                              boolean brewArea,
                              int customerID) {


        for (ItemDetails i : area) {
            if (i.getNumber() == customerID && waitingArea.size() != 0) {
                ItemDetails itemDetails = waitingArea.remove();
                if (brewArea)
                    System.out.println("1 " + i.getItem() + " currently brewing for " +
                            customers.get(customerID).getCustomerName() +
                            " has been transferred to " + customers.get(itemDetails.getNumber()).getCustomerName()
                            + "'s order");
                else
                    System.out.println("1 " + i.getItem() + " in " + customers.get(customerID).getCustomerName()
                            + "'s tray has been transferred to "
                            + customers.get(itemDetails.getNumber()).getCustomerName() + "'s tray");
                i.setNumber(itemDetails.getNumber());
            }
        }
    }

    private static void writeLogs(ArrayList<LogDetails> logs) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(path);
            GSON.toJson(logs, writer);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // using the size of items to structure the words to be used
    private String structureNumbers(int numberOfCoffee, int numberOfTea) {
        StringBuilder stringBuilder = new StringBuilder();
        if (numberOfTea > 0)
            stringBuilder.append(numberOfTea).append(numberOfTea > 1 ? " teas" : " tea");

        if (numberOfCoffee > 0) {
            if (numberOfTea > 0)
                stringBuilder.append(" and ");
            stringBuilder.append(numberOfCoffee).append(numberOfCoffee > 1 ? " coffees" : " coffee");
        }

        return stringBuilder.toString();
    }

    // Since there are multiple thread serving, I check if there is space in the brewing area before sleeping because
    // if I do not, it takes from the waiting area and pauses if the brewing area is full, leading the count in the
    // waiting area minus 1 lees than the actual value
    private void serverMaking(LinkedBlockingQueue<ItemDetails> waitingArea, LinkedBlockingQueue<ItemDetails> src,
                              LinkedBlockingQueue<ItemDetails> dst, int time) {
        try {
                src.put(waitingArea.take());
                System.out.println(getLogDetails());
                Thread.sleep(time);
                ItemDetails id = src.take();
                dst.put(id);
                if(!isOrderCompleted(id))
                    System.out.println(getLogDetails());


        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    // looping the number of items and adding to the waiting areas
    public String placeOrder(String customerName, int customerID, int numberOfCoffee, int numberOfTea,
                             PrintWriter writer) {
        if (!customers.containsKey(customerID))
            customers.put(customerID, new Order(customerName, customerID,writer));
        Order order = customers.get(customerID);
        order.addCoffee(numberOfCoffee);
        order.addTea(numberOfTea);

        for (int i = 0; i < numberOfCoffee; i++)
            coffeeWaitingArea.add(new ItemDetails("coffee", customerID));


        for (int i = 0; i < numberOfTea; i++)
            teaWaitingArea.add(new ItemDetails("tea", customerID));

        System.out.println(getLogDetails());

        return "Order received for " + customerName + " (" +
                structureNumbers(numberOfCoffee, numberOfTea) + ")";

    }
    // Setters

    public void addCustomer(String customerName, int customerId, PrintWriter writer) {
        customers.put(customerId, new Order(customerName, customerId, writer));
        System.out.println(getLogDetails());
    }

    // Getters

    // reading from the json and then adding to it before writing back
    public String getLogDetails() {
        LogDetails logDetails = null;
        try {
            ArrayList<LogDetails> logs = getLogs();
            logDetails = new LogDetails(getNoOfCustomersInCafe(), getClientWaitingForOrders(), getItemInWaitingArea(),
                    new ArrayList<>(
                            Arrays.asList(new ItemDetails("Coffee", coffeeWaitingArea.size()),
                                    new ItemDetails("Tea", teaWaitingArea.size()))), getItemInBrewingArea(),
                    new ArrayList<>(
                            Arrays.asList(new ItemDetails("Coffee", coffeeBrewingArea.size()),
                                    new ItemDetails("Tea", teaBrewingArea.size()))), getItemInTrayArea(),
                    new ArrayList<>(
                            Arrays.asList(new ItemDetails("Coffee", coffeeTrayArea.size()),
                                    new ItemDetails("Tea", teaTrayArea.size()))));

            logs.add(logDetails);


            writeLogs(logs);
        } catch (IOException e) {
            System.out.println("Could not get logs.\n"+e.getMessage());
        }

        assert logDetails != null;
        return logDetails.toString() + '\n';
    }

    public String getStatus(int customerID) {
        int noCoffeeWaiting = getNumberOfProducts(customerID, coffeeWaitingArea.iterator());
        int noCoffeePreparing = getNumberOfProducts(customerID, coffeeBrewingArea.iterator());
        int noCoffeeTray = getNumberOfProducts(customerID, coffeeTrayArea.iterator());
        int noTeaWaiting = getNumberOfProducts(customerID, teaWaitingArea.iterator());
        int noTeaPreparing = getNumberOfProducts(customerID, teaBrewingArea.iterator());
        int noTeaTray = getNumberOfProducts(customerID, teaTrayArea.iterator());

        if (customers.get(customerID) == null)
            return "Please make an order";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Order status for ").append(customers.get(customerID).getCustomerName()).append(":");
        if (noCoffeeWaiting > 0 || noTeaWaiting > 0)
            stringBuilder.append("- ").append(structureNumbers(noCoffeeWaiting, noTeaWaiting))
                    .append(" in waiting area");
        if (noCoffeePreparing > 0 || noTeaPreparing > 0)
            stringBuilder.append("- ").append(structureNumbers(noCoffeePreparing, noTeaPreparing))
                    .append(" currently being prepared");
        if (noCoffeeTray > 0 || noTeaTray > 0)
            stringBuilder.append("- ").append(structureNumbers(noCoffeeTray, noTeaTray)).append(" currently in tray");
        if (noCoffeePreparing + noCoffeeTray + noTeaPreparing + noCoffeeWaiting + noTeaTray + noTeaWaiting == 0)
            stringBuilder.append("- No order found for ").append(customers.get(customerID).getCustomerName());
        return stringBuilder.toString();

    }

    // looping through each customer and checking if they have any orders left
    private int getClientWaitingForOrders() {
        int amount = 0;
        for (int i : customers.keySet())
            if (getWaitOrIdle(i))
                amount++;

        return amount;

    }

    public boolean getWaitOrIdle(int customerID) {  // false is idle, true is waiting
        return customers.get(customerID).getNumberOfTea() + customers.get(customerID).getNumberOfCoffee() != 0;
    }

    public int getNextID() { return userID.getAndIncrement(); }

    public int getNoOfCustomersInCafe() { return customers.size(); }

    // Removing from a queue and adding a different value to another queue
    private int replaceAndGetNewCustomerID(LinkedBlockingQueue<ItemDetails> src,
                                           LinkedBlockingQueue<ItemDetails> dst,
                                           int customerID) {
        for (ItemDetails i : dst) {

        }
        return customerID;
    }

    private static ArrayList<LogDetails> getLogs() throws IOException {
        File file = new File(path.toString());
        BufferedReader reader = null;
        ArrayList<LogDetails> result = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
                return new ArrayList<>();
            }
            reader = Files.newBufferedReader(path);

            Type t = new TypeToken<ArrayList<LogDetails>>() {
            }.getType();
            result = GSON.fromJson(reader, t);
            reader.close();
        }
        catch (IOException e){
            System.out.println("Failed to create file.\n" + e.getMessage());
        }
        catch (JsonSyntaxException e) {
            System.out.println("An error occurred, Making a new JSON file.\n");
            int i = 1;
            do {
                path = Paths.get("log" + i++ + ".json");
                file = new File(path.toString());
                result = null;
            } while (!file.createNewFile());
        }
        result = result == null ? new ArrayList<>() : result;
        return result;
    }

    // checks the number of item in any list
    public int getNumberOfProducts(int customerID, Iterator<ItemDetails> iterator) {
        int amount = 0;
        while (iterator.hasNext())
            if (iterator.next().getNumber() == customerID)
                amount++;
        return amount;
    }

    public int getItemInTrayArea() { return teaTrayArea.size() + coffeeTrayArea.size(); }

    public int getItemInBrewingArea() { return teaBrewingArea.size() + coffeeBrewingArea.size();}

    // Methods for the server to know how many items are in each area.
    public int getItemInWaitingArea() { return teaWaitingArea.size() + coffeeWaitingArea.size(); }

}
