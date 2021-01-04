package server;

import java.io.*;
import java.util.*;

public class Cards implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private ArrayList<Card> list;
    private String name;

    public Cards(String n){
        list = new ArrayList<Card>();
        name = n;
    }

    public ArrayList<Card> getList(){
        return list;
    }

    public String getName(){
        return name;
    }

    public void copy(Cards newCards){
        list = newCards.getList();
    }

}
