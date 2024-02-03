package Helpers;

public class ItemDetails {
    String Item;
    int Number;

    public ItemDetails(String item, int number) {
        Item = item;
        Number = number;
    }

    public String getItem() {
        return Item;
    }

    public void setItem(String item) {
        Item = item;
    }

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }
}