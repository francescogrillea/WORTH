import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

public class ServerMainClass {

    private final static String RECOVERY_FILE_PATH = "./recovery/"; // dove sono contenuti i file persistenti
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json";  //nome del file che contiene l'elenco degli utenti registrati
    
    //data structures
    public static UsersDB users;
    //private static LinkedList<Project> projects;

    public static void main(String[] args){
        
        users = new UsersDB();
        restoreBackup();

        System.out.println("Ripristino lo stato iniziale: lista utenti");
        for (User u : users.listUser())
            System.out.println(u.getUsername());

        try {
            RegistrationClass registration = new RegistrationClass(users, RECOVERY_FILE_PATH);
            RegistrationInterface stub = (RegistrationInterface) UnicastRemoteObject.exportObject(registration, 4567);

            LocateRegistry.createRegistry(4567);
            Registry r = LocateRegistry.getRegistry(4567);
    
            r.rebind("RegisterUser", stub);
    
        } catch (RemoteException e) {
            e.printStackTrace();
        }    

    }

    //TODO- se uno o piu' file inesistenti -> lanciare eccezione e chiudere il server
    public static void restoreBackup(){

        //TODO- fare in modo da esaminare tutta la cartella recovery e legge ogni file/directoy per ripristinare lo stato
        //TODO- se non questo file non e' presente -> crearlo!
        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(RECOVERY_FILE_PATH + FILENAME_utentiRegistrati));) {
            users = (UsersDB) input.readObject();
        } catch (Exception e) {
            //ToDo- se il file e' vuoto non catchare l'eccezione
            System.out.println(e);
        }
    }

}
