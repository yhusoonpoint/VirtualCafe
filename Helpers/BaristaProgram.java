package Helpers;

import java.net.Socket;
import java.util.Scanner;
import java.util.Objects;
import java.io.PrintWriter;

public class BaristaProgram implements Runnable{
    private final Socket socket;
    private final OrderProcessor orderProcessor;

    public BaristaProgram(Socket socket, OrderProcessor orderProcessor ) {
        this.socket = socket;
        this.orderProcessor = orderProcessor;
    }

    @Override
    public void run() {
        int customerId = 0;
        String customerName = "";
        try (Scanner scanner    = new Scanner( socket.getInputStream() );
             PrintWriter writer = new PrintWriter( socket.getOutputStream(), true ) ) {
            try {

                writer.println("Welcome to the Virtual Cafe. I am your virtual assistant for today, please enter your name.");

                customerName = scanner.nextLine();
                customerId = orderProcessor.getNextID();
                orderProcessor.addCustomer(customerName, customerId, writer);

                writer.println(customerId);


                boolean keepLooping = true; //break the loop from switch

                System.out.println(customerName + " joined the cafe with customer ID " + customerId + ".\n");

                while (keepLooping) {
                    String[] subStrings = scanner.nextLine().split(",");
                    switch (subStrings[0].toLowerCase()) {
                        case "place order":
                            writer.println(orderProcessor.placeOrder(subStrings[1],
                                    Integer.parseInt(subStrings[2]),
                                    Integer.parseInt(subStrings[3]),
                                    Integer.parseInt(subStrings[4]),writer));
                            break;

                        case "status":
                            writer.println(orderProcessor.getStatus(Integer.parseInt(subStrings[1])));
                            break;

                        case "close":
                            writer.close();
                            orderProcessor.close(Integer.parseInt(subStrings[1]));
                            keepLooping = false;
                            scanner.close();
                            break;

                        default:
                            writer.println("Did not recognised that command.");
                    }
                }
            }
            catch (Exception e) { writer.println("ERROR " + e.getMessage());socket.close(); }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally {
            if(!Objects.equals(customerName, ""))
                System.out.println(customerName + " left the cafe..");
            else
                System.out.println("A customer entered and left..");
        }
    }
}
