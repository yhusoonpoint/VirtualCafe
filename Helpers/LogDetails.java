package Helpers;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogDetails {
    String Date;
    String Time;
    int Number_Of_Clients_In_cafe;
    int Number_Of_Clients_Waiting_For_Order;
    int Waiting_Area_Total;
    ArrayList<ItemDetails> Waiting_Area;
    int Brewing_Area_Total;
    ArrayList<ItemDetails> Brewing_Area;
    int Tray_Area_Total;
    ArrayList<ItemDetails> Tray_Area;



    public LogDetails(int number_Of_Clients_In_cafe,
                      int number_Of_Clients_Waiting_For_Order,int waiting_Area_Total,
                      ArrayList<ItemDetails> waiting_Area, int brewing_Area_Total, ArrayList<ItemDetails> brewing_Area,
                      int tray_Area_Total, ArrayList<ItemDetails> tray_Area) {
        Date = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
        Time = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
        Number_Of_Clients_In_cafe = number_Of_Clients_In_cafe;
        Number_Of_Clients_Waiting_For_Order = number_Of_Clients_Waiting_For_Order;
        Waiting_Area_Total = waiting_Area_Total;
        Waiting_Area = waiting_Area;
        Brewing_Area_Total = brewing_Area_Total;
        Brewing_Area = brewing_Area;
        Tray_Area_Total = tray_Area_Total;
        Tray_Area = tray_Area;
    }

    public int getNumber(ArrayList<ItemDetails> area, String item)
    {
        int number = 0;
        for(ItemDetails i : area)
            if(i.Item.equalsIgnoreCase(item))
                number = i.Number;

        return number;
    }

    @Override
    public String toString() {

        return "\nNumber of clients in the cafe - " + Number_Of_Clients_In_cafe +
                "\nNumber of client waiting for orders - " + Number_Of_Clients_Waiting_For_Order +
                "\nNumber of items in waiting area: " +
                "\n    -> Tea: " + getNumber(Waiting_Area, "tea") +
                "\n    -> Coffee: " + getNumber(Waiting_Area, "Coffee")  +
                "\nNumber of items in brewing area: " +
                "\n    -> Tea: " + getNumber(Brewing_Area, "tea") +
                "\n    -> Coffee: " + getNumber(Brewing_Area, "Coffee") +
                "\nNumber of items in tray area: " +
                "\n    -> Tea: " + getNumber(Tray_Area, "tea") +
                "\n    -> Coffee: " + getNumber(Tray_Area, "Coffee");
    }
}
