package Helpers;

import java.net.Socket;
import java.util.Scanner;
import java.util.Objects;
import java.io.IOException;
import java.io.PrintWriter;

public class CustomerClient implements AutoCloseable {
    private Thread thread;

    String lastResult = "";
    private Scanner reader;
    private final int customerID;
    private String lastOutput = "";
    private final PrintWriter writer;
    private final String customerName;

    public CustomerClient() throws IOException {
        int port = 4199;
        Socket socket = new Socket("localhost", port);
        reader = new Scanner(socket.getInputStream());
        writer = new PrintWriter(socket.getOutputStream(), true);
        customerName = inputHandler(reader.nextLine() + "\n    -> ");
        writer.println(customerName);
        customerID = Integer.parseInt(reader.nextLine());
        thread = new Thread(messageReceiver);
        thread.start();

    }

    // Thread

    // I created a runnable to keep looping to find if there is a new message from the sever, if there is then it prints
    // out, meaning message can be printed at any time from the sever. After that, it prints out the last message on the
    // console.
    Runnable messageReceiver = () -> {
        while (!thread.isInterrupted()) {
            if (reader.hasNext()) {
                String result = reader.nextLine();
                if(!result.equalsIgnoreCase(lastResult)) {
                    System.out.println("\n" + result.replaceAll("-", "\n-") + "\n");
                    if (!Objects.equals(lastOutput, "") && Objects.equals(result.split(" ")[1],
                            "delivered")) {
                        System.out.print(lastOutput);
                        lastOutput = "";
                    }
                    lastResult = "";
                }
                lastResult = result;
            }
        }
    };


    @Override
    public void close() {
        System.out.println("\nGoodbye " + getCustomerName() + ".");
        System.out.println("\nThank you for visiting!!!");
        thread.interrupt();
        writer.println("CLOSE," + customerID);
        reader.close();
        writer.close();
    }


    // Checking user input by checking if it contains the keyword tea or coffee and extracting the numbers with
    // a function
    public void processInput(String customerInput) {
        if (customerInput.toLowerCase().contains("status")) {
            getStatus();
        } else if (!(customerInput.toLowerCase().contains("tea") || customerInput.toLowerCase().contains("coffee"))) {
            System.out.println("\n" + customerInput + " <- command not recognised.");
        } else {
            writer.println("PLACE ORDER," + customerName + "," + customerID + "," + getExtractedNumber(customerInput,
                    "coffee")
                    + "," + getExtractedNumber(customerInput, "tea"));
        }
    }

    // Setter

    public void setLastOutput(String output) { lastOutput = output; }

    // Getters

    public String getCustomerName() { return customerName; }

    void getStatus() { writer.println("STATUS," + customerID); }

    // removing all whitespaces
    // finding the index of the keyword, 'for example' tea then it uses the index of t to go backwards and extract
    // any number found and store it into a string builder, since it is looping backward, I reverse the whole string
    // and then convert it into a string.
    public int getExtractedNumber(String input, String word) {
        int number = 0;
        StringBuilder sb = new StringBuilder();
        input = input.replaceAll("\\s+", "").toLowerCase();
        for (int i = -1; (i = input.indexOf(word, i + 1)) != -1; i++) {
            int subtract = 1;
            while (i - subtract > -1 && Character.isDigit(input.charAt(i - subtract)))
                sb.append(input.charAt(i - subtract++));
            if(!sb.isEmpty())
                number += Integer.parseInt(sb.reverse().toString());
            sb = new StringBuilder();
        }
        return number;
    }

    // Resetting the scanner everytime to avoid getting incorrect information
    // using a while loop to keep requesting input if it is wrong
    public static String inputHandler(String output) {
        Scanner in = new Scanner(System.in);
        String input = "";
        while(Objects.equals(input, ""))
        {
            System.out.print(output);
            input = in.nextLine();
        }
        return input;

    }

}
