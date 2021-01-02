package server;

import java.io.*;
import common.*;

public class ServerMainClass {

    //dove sono contenuti i file per ripristinare lo stato del sistema
    private final static String RECOVERY_FILE_PATH = "./recovery/";
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json"; 

    // data structures
    public static UsersDB users;

    public static void main(String[] args) {

        users = new UsersDB();
        restoreBackup();

        //Il server stampa la lista iniziale degli utenti registrati. Per semplicita' ogni utente ha la stessa password 'myPass'
        System.out.println("Ripristino lo stato iniziale: lista utenti");
        for (User u : users.listUser())
            System.out.println(u.getUsername() + " - " + u.getStatus());

        ServerNotificationService notificationService = new NotificationClass().start();    //RMI Callback
        new RegistrationClass(users, RECOVERY_FILE_PATH, notificationService).start();  //RMI
        new MultiThreadedServer(users, notificationService).start(); //TCP

    }

    public static void restoreBackup(){

        //TODO- fare in modo da esaminare tutta la cartella recovery e legge ogni file/directoy per ripristinare lo stato
        //TODO- se non questo file non e' presente -> crearlo!
        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(RECOVERY_FILE_PATH + FILENAME_utentiRegistrati));) {
            users = (UsersDB) input.readObject();
            users.setAllOffline();
        }catch(EOFException e){
            //file vuoto
        }catch (Exception e) {
            System.out.println(e);
        }
    }

}
