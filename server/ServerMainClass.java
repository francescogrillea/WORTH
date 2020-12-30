package server;

import java.io.*;
import common.*;

public class ServerMainClass {

    private final static String RECOVERY_FILE_PATH = "./recovery/"; // dove sono contenuti i file persistenti
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json";  //nome del file che contiene l'elenco degli utenti registrati

    //data structures
    public static UsersDB users;
    //private static LinkedList<Project> projects;

    public static void main(String[] args){
        
        users = new UsersDB();
        //prova
        restoreBackup();

        System.out.println("Ripristino lo stato iniziale: lista utenti");
        for (User u : users.listUser())
            System.out.println(u.getUsername() + " - "+ u.getStatus());

        //RMI class to implement Register function
        new RegistrationClass(users, RECOVERY_FILE_PATH).start();
        new NotificationClass(users).start();

    }

    public static void restoreBackup(){

        //TODO- fare in modo da esaminare tutta la cartella recovery e legge ogni file/directoy per ripristinare lo stato
        //TODO- se non questo file non e' presente -> crearlo!
        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(RECOVERY_FILE_PATH + FILENAME_utentiRegistrati));) {
            users = (UsersDB) input.readObject();
        }catch(EOFException e){
            //file vuoto
        }catch (Exception e) {
            //ToDo- se il file e' vuoto non catchare l'eccezione
            System.out.println(e);
        }
    }

}