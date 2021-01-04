package server;

import java.util.*;
import common.*;

public class Project {

    public static final String TODO = "todo";
    public static final String INPROGRESS = "inprogress";
    public static final String TOBEREVISED = "toberevised";
    public static final String DONE = "done";

    private String name;
    private UsersDB members;
    private Cards todo;
    private Cards inProgress;
    private Cards toBeRevised;
    private Cards done;
    private Object lock;


    public Project(String n, User firstUser){   //quando viene creato un progetto   
        name = n;
        members = new UsersDB();
        members.addUser(firstUser);
        lock = new Object();

        todo = new Cards(TODO);
        inProgress = new Cards(INPROGRESS);
        toBeRevised = new Cards(TOBEREVISED);
        done = new Cards(DONE);
    }

    public Project(String n, UsersDB m, Cards todo, Cards inprogress, Cards toberevised, Cards done){
        name = n;
        members = m;
        lock = new Object();

        this.todo = new Cards(TODO);
        this.inProgress = new Cards(INPROGRESS);
        this.toBeRevised = new Cards(TOBEREVISED);
        this.done = new Cards(DONE);
        
        this.todo.copy(todo);
        this.inProgress.copy(inprogress);
        this.toBeRevised.copy(toberevised);
        this.done.copy(done);
    }

    public void createLists(){
        todo = new Cards(TODO);
        inProgress = new Cards(INPROGRESS);
        toBeRevised = new Cards(TOBEREVISED);
        done = new Cards(DONE);
    }

    //Ritorna il nome del progetto
    public String getName(){
        return name;
    }

    //Ritorna l'insieme dei membri del progetto
    public UsersDB getUsers() {
        return members;
    }

    //Ritorna un oggetto utilizzato per la concorrenza
    public Object getLock(){
        return lock;
    }

    //Ritorna la lista di Cards in stato To do 
    public Cards getTodoCards(){
        return todo;
    }

    //Ritorna la lista di Cards in stato In progress
    public Cards getInprogressCards(){
        return inProgress;
    }

    //Ritorna la lista di Cards in stato To be Revised
    public Cards getToberevisedCards(){
        return toBeRevised;
    }

    //Ritorna la lista di Cards in stato Done
    public Cards getDoneCards(){
        return done;
    }

    //Aggiunge un nuovo membro al progetto. Ritorna false se l'utente e' gia' membro
    public boolean addMember(User user){
        if(members.containsUser(user.getUsername()))
            return false;
        members.addUser(user);
        return true;
    }

    //ritorna true se l'utente user e' membro del progetto
    public boolean hasMember(String user){
        return members.containsUser(user);
    }

    //Ritorna l'elenco dei membri del progetto
    public Collection<User> getMembers() {
        return members.getStructure().values();
    }

    //Inserisce una nuova card nella lista Todo. Ritorna false se la card e' gia' presente
    public boolean addCard(String cardName, String cardDescription){  //piu' utenti possono creare una card contemporaneamente
        if(existsCard(cardName) == true)    //se la card e' gia' esistente
            return false;

        todo.getList().add(new Card(cardName, cardDescription));
        return true;
    }

    //Ritorna le informazioni (nome, descrizione e lista attuale) sulla Card cardName
    public String findCard(String cardName){

        for (Card c : todo.getList()) 
            if(c.getName().equals(cardName))
                return c.getName() +" "+c.getList() + " " + c.getDescription();
                
        for (Card c : inProgress.getList()) 
            if(c.getName().equals(cardName))
                return c.getName() +" "+c.getList() + " " + c.getDescription();

        for (Card c : toBeRevised.getList()) 
            if(c.getName().equals(cardName))
                return c.getName() +" "+c.getList() + " " + c.getDescription();
        
        for (Card c : done.getList()) 
            if(c.getName().equals(cardName))
                return c.getName() +" "+c.getList() + " " + c.getDescription();

        return "Error. Card " +cardName + " not found.";
    }

    //Ritorna un messaggio testuale contenente le tutte le card del progetto (divise per tipologia)
    public String getCards(){
        String result = "\tTodo: ";
        for (Card c : todo.getList()) 
            result = result + c.getName() + " - ";

        result = result + "\n\tIn progress: ";
        for (Card c : inProgress.getList()) 
            result = result + c.getName() + " - ";

        result = result + "\n\tTo be revised: ";
        for (Card c : toBeRevised.getList()) 
            result = result + c.getName() + " - ";
        
        result = result + "\n\tDone: ";
        for (Card c : done.getList()) 
            result = result + c.getName() + " - ";
        
        return result;
    }


    //Ritorna true se la card cardName e' gia' all'interno di una lista
    private boolean existsCard(String cardName){
        for (Card c : todo.getList()) 
            if(c.getName().equals(cardName))
                return true;

        for (Card c : inProgress.getList()) 
            if(c.getName().equals(cardName))
            return true;
        
        for (Card c : toBeRevised.getList()) 
            if(c.getName().equals(cardName))
                return true;
        
        for (Card c : done.getList()) 
            if(c.getName().equals(cardName))
                return true;
        
        return false;
    }
}
