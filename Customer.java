import Helpers.CustomerClient;

public class Customer {
    public static void main(String[] args) {


        String customerInput = "";

        try ( CustomerClient cc = new CustomerClient() ) {

            Thread shutDownHookThread = new Thread(() -> { // handling SIGTERM
                try { cc.close(); } catch (Exception e) {
                    System.out.println("Customer failed to exit the Virtual Cafe."); }
            });

            Runtime.getRuntime().addShutdownHook(shutDownHookThread);

            while(true) {
                Thread.sleep(30); // delaying in case there is a message from sever

                System.out.println("\nWelcome, " + cc.getCustomerName() + ".");
                System.out.println("These are the following commands you can input.\n");
                System.out.println("    -> Exit.");
                System.out.println("    -> Order status.");
                System.out.println("    -> Order <amount> <product type>. " +
                        "For example, Order 5 coffees or Order 1 tea.");
                System.out.println("    -> Order <amount> <product type> and <amount> <product type>. " +
                        "For example, Order 5 coffees and 1 tea.\n");

                customerInput = CustomerClient.inputHandler("What would you like to do?\n    -> ");

                cc.setLastOutput("What would you like to order?\n    -> ");

                if(customerInput.equalsIgnoreCase("exit"))
                {
                    Runtime.getRuntime().removeShutdownHook(shutDownHookThread);
                    break;
                }

                if(customerInput.toLowerCase().contains("order"))
                    cc.processInput(customerInput);
                else {
                    System.out.println("\nSorry I did not understand your command. Please try again.");
                }
            }
        }
        catch (Exception e) {
            if(e.getMessage().contains("refused"))
                System.out.println("Sorry, The cafe is closed.");
        }
    }
}
