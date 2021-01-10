package common;

import java.io.Serializable;
import java.util.*;


public class UsersDB implements Serializable{

    private static final long serialVersionUID = 1L;
    private HashMap<String, User> users;

    public UsersDB(){
        users = new HashMap<String, User>();
    }

    //get HashMap
    public HashMap<String, User> getStructure(){
        return users;
    }

    //new user
    public void addUser(User u){
        users.put(u.getUsername(), u);
    }

    //return a specific user or null
    public User getUser(String name){
        return users.get(name);
    }

    //list all user
    public Collection<User> listUser(){
        return users.values();
    }

    public boolean containsUser(String name){
        return users.containsKey(name);
    }

    //set users status offline
    public void setAllOffline(){

        for (User u : users.values()) 
            u.setOffline();
    }

    //copy newDBs structure in the current data structure
    public void copy(UsersDB newDB){
        this.users = newDB.getStructure();
    }
    

}
