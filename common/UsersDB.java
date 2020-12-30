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

    public LinkedList<User> listUser(){
        return users;
    }

    //TODO- implement this  
    //e' una classe da RMI / callback ? penso di si
    public LinkedList<User> listOnlineUser(){
        return null;
    }

    public void copy(UsersDB newDB){
        this.users = newDB.listUser();
    }
    

}
