package server;

import java.io.*;
import common.*;

public class ServerMainClass {

    //dove sono contenuti i file per ripristinare lo stato del sistema
    private final static String RECOVERY_FILE_PATH = "./recovery/";
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json"; 

    // data structures
    public static UsersDB users;
    // private static LinkedList<Project> projects;

    public static void main(String[] args) {

        users = new UsersDB();
        restoreBackup();

        System.out.println("Ripristino lo stato iniziale: lista utenti");
        for (User u : users.listUser())
            System.out.println(u.getUsername() + " - " + u.getStatus());

        ServerNotificationService notificationService = new NotificationClass().start();
        new RegistrationClass(users, RECOVERY_FILE_PATH, notificationService).start();
        
        //create TCP Connection (multithreaded)
        new MultiThreadedServer(users).start(); //TODO- passare il sistema di notifica anche

    }

    public static void restoreBackup(){

        //TODO- fare in modo da esaminare tutta la cartella recovery e legge ogni file/directoy per ripristinare lo stato
        //TODO- se non questo file non e' presente -> crearlo!
        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(RECOVERY_FILE_PATH + FILENAME_utentiRegistrati));) {
            users = (UsersDB) input.readObject();
            users.setOffline();
        }catch(EOFException e){
            //file vuoto
        }catch (Exception e) {
            System.out.println(e);
        }
    }

}
