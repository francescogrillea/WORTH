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
    private ArrayList<Card> todo;
    private ArrayList<Card> inProgress;
    private ArrayList<Card> toBeRevised;
    private ArrayList<Card> done;
    private Object lock;
    private String chatIP;
    private int port;


    public Project(String n, User firstUser){   //quando viene creato un progetto   
        name = n;
        members = new UsersDB();
        members.addUser(firstUser);
        lock = new Object();

        todo = new ArrayList<Card>();
        inProgress = new ArrayList<Card>();
        toBeRevised = new ArrayList<Card>();
        done = new ArrayList<Card>();
    }

    public Project(String n){   //quando viene caricato il progetto
        name = n;
        members = new UsersDB();
        lock = new Object();

        this.todo = new ArrayList<Card>();
        this.inProgress = new ArrayList<Card>();
        this.toBeRevised = new ArrayList<Card>();
        this.done = new ArrayList<Card>();
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

    public void setIP(String ip){
        chatIP = ip;
    }

    public String getIP(){
        return chatIP;
    }

    public int getPort(){
        return port;
    }

    public void setPort(int p){
        port = p;
    }

    //Ritorna la lista di Cards in stato To do 
    public ArrayList<Card> getTodoCards(){
        return todo;
    }

    //Ritorna la lista di Cards in stato In progress
    public ArrayList<Card> getInprogressCards(){
        return inProgress;
    }

    //Ritorna la lista di Cards in stato To be Revised
    public ArrayList<Card> getToberevisedCards(){
        return toBeRevised;
    }

    //Ritorna la lista di Cards in stato Done
    public ArrayList<Card> getDoneCards(){
        return done;
    }

    public ArrayList<Card> getList(String listName){

        if(listName.equals(TODO))
            return todo;
        if(listName.equals(INPROGRESS))
            return inProgress;
        if(listName.equals(TOBEREVISED))
            return toBeRevised;
        if(listName.equals(DONE))
            return done;
        return null;
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
    public Card addCard(String cardName, String cardDescription){  //piu' utenti possono creare una card contemporaneamente
        //TODO modificare exists Card con findCard (return null)
        if(existsCard(cardName) == true)    //se la card e' gia' esistente
            return null;

        Card card = new Card(cardName, cardDescription);
        todo.add(card);
        return card;
    }

    public Card moveCard(String cardName, String srcList, String destList) throws NullPointerException{

        //controllo che la card venga spostata in uno stato valido
        if(srcList.equals(destList))
            throw new IllegalStateException();
        else if(srcList.equals(TODO) && !(destList.equals(INPROGRESS)))
            throw new IllegalStateException();
        else if(destList.equals(TODO) || srcList.equals(DONE))
            throw new IllegalStateException();

        Card card = getCard(cardName);  //torna null se non c'e' corrispondenza
        card.move(destList);

        getList(destList).add(card);    //torna null se destList non e' una lista
        getList(srcList).remove(card);  //torna null se srcList non e' una lista
        
        return card;
    }


    public void restoreMembers(UsersDB users){
        members.copy(users);
    }


    public void restoreCard(Card card){

        String list = card.getListName().toLowerCase();
        getList(list).add(card);
    }

    //Ritorna le informazioni (nome, descrizione e lista attuale) sulla Card cardName
    public Card getCard(String cardName){

        for (Card c : todo) 
            if(c.getName().equals(cardName))
                return c;
                
        for (Card c : inProgress) 
            if(c.getName().equals(cardName))
                return c;

        for (Card c : toBeRevised) 
            if(c.getName().equals(cardName))
                return c;
        
        for (Card c : done) 
            if(c.getName().equals(cardName))
                return c;

        return null;
    }

    //Ritorna un messaggio testuale contenente le tutte le card del progetto (divise per tipologia)
    public String getCards(){
        String result = "\tTodo:\t ";
        for (Card c : todo) 
            result = result + c.getName() + " - ";

        result = result + "\n\tIn progress:\t ";
        for (Card c : inProgress) 
            result = result + c.getName() + " - ";

        result = result + "\n\tTo be revised:\t ";
        for (Card c : toBeRevised) 
            result = result + c.getName() + " - ";
        
        result = result + "\n\tDone:\t ";
        for (Card c : done) 
            result = result + c.getName() + " - ";
        
        return result;
    }


    //Ritorna true se la card cardName e' gia' all'interno di una lista
    private boolean existsCard(String cardName){
        for (Card c : todo) 
            if(c.getName().equals(cardName))
                return true;

        for (Card c : inProgress) 
            if(c.getName().equals(cardName))
            return true;
        
        for (Card c : toBeRevised) 
            if(c.getName().equals(cardName))
                return true;
        
        for (Card c : done) 
            if(c.getName().equals(cardName))
                return true;
        
        return false;
    }
}
