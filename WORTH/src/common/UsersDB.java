package common;

import java.io.Serializable;
import java.util.LinkedList;

public class UsersDB implements Serializable{

    private static final long serialVersionUID = 1L;    //TODO- che significa?
    private LinkedList<User> users;// TODO- LinkedList or ArrayList?

    public UsersDB(){
        users = new LinkedList<User>();
    }

    public void addUser(User u){
        users.add(u);
    }

    public User getUser(String name){
        for (User user : users) {
            if(user.getUsername().equals(name))
                return user;
        }
        return null;
    }

    public LinkedList<User> listUser(){
        return users;
    }

    //TODO 
    public LinkedList<User> listOnlineUser(){
        return null;
    }

    public void setOffline(){
        for (User u : users) 
            u.setStatus(false);
    }

    public void copy(UsersDB newDB){
        this.users = newDB.listUser();
    }
    

}
