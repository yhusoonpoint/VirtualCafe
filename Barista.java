import Helpers.BaristaProgram;
import Helpers.OrderProcessor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Barista {
    private final static int port = 4199;
    private static final OrderProcessor orderProcessor = new OrderProcessor();

    public static void main(String[] args) {
        RunServer();
    }

    private static void RunServer()
    {

        ServerSocket serverSocket = null;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { // handling SIGTERM
            try {
                System.out.println("CAFE IS CLOSED !!!");
            } catch (Exception e) {System.out.println("Error occurred closing socket");}
        }));
        try
        {   serverSocket = new ServerSocket( port );
            orderProcessor.startAllThreads();
            System.out.println( "CAFE IS OPENED !!!" );
            while (true)
            {   Socket socket = serverSocket.accept();
                new Thread( new BaristaProgram( socket, orderProcessor ) ).start();
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }
}
