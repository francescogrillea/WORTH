package server;

import java.io.Serializable;
import java.util.ArrayList;

public class Card implements Serializable{
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private ArrayList<String> history;

    public Card(String n, String d){
        name = n;
        description = d;
        history = new ArrayList<String>();
        history.add(Project.TODO);
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public ArrayList<String>getHistory(){
        return history;
    }

    public String getListName(){
        return history.get(history.size()-1);
    }

    public void move(String newState){
        history.add(newState);
    }

}
