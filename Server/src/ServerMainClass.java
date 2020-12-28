import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class ServerMainClass {

    private final static String RECOVERY_FILE_PATH = "./recovery/"; // dove sono contenuti i file persistenti
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json";  //nome del file che contiene l'elenco degli utenti registrati
    
    //data structures
    public static UsersDB users;
    //private static LinkedList<Project> projects;

    public static void main(String[] args) throws Exception {
        
        users = new UsersDB();
        restoreBackup();

        for (User u : users.listUser())
            System.out.println("B: "+ u.getUsername());


    }

    public static void restoreBackup(){

        //TODO- fare in modo da esaminare tutta la cartella recovery e legge ogni file/directoy per ripristinare lo stato

        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(RECOVERY_FILE_PATH + FILENAME_utentiRegistrati));) {
            users = (UsersDB) input.readObject();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
