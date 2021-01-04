package server;

import java.io.*;
import java.util.*;
import common.*;

public class ServerMainClass {

    //dove sono contenuti i file per ripristinare lo stato del sistema
    public final static String RECOVERY_FILE_PATH = "./recovery/";
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json"; 
    public final static String membersFile = "/members.json";
    public final static String todoFile = "/cards_todo.json";
    public final static String inprogressFile = "/cards_inprogress.json";
    public final static String toberevisedFile = "/cards_toberevised.json";
    public final static String doneFile = "/cards_done.json";


    // data structures
    public static UsersDB users;
    private static HashMap<String, Project> projects;

    public static void main(String[] args) {

        users = new UsersDB();
        projects = new HashMap<String, Project>();
        restoreBackup();

        //Il server stampa la lista iniziale degli utenti registrati. Per semplicita' ogni utente ha la stessa password 'myPass'
        System.out.println("Ripristino lo stato iniziale: lista utenti");
        for (User u : users.listUser())
            System.out.println(u.getUsername() + " - " + u.getStatus());

        System.out.println("Ripristino lo stato iniziale: progetti");
        for (Project p : projects.values()) {
            System.out.println(p.getName());
            for (User u : p.getMembers()) 
                System.out.println("\t"+u.getUsername());
            
        }

        ServerNotificationService notificationService = new NotificationClass().start();    //RMI Callback
        new RegistrationClass(users, notificationService).start();  //RMI
        new MultiThreadedServer(users, notificationService, projects).start(); //TCP

    }
    
    private static void restoreBackup(){

        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(RECOVERY_FILE_PATH + FILENAME_utentiRegistrati));) {
            users = (UsersDB) input.readObject();
            users.setAllOffline();
        }catch(FileNotFoundException e){
            //TODO create utentiRegistrati.json
        }catch (Exception e) {
            e.printStackTrace();
        }

        File recoveryDir = new File(RECOVERY_FILE_PATH);

        for (File directory : recoveryDir.listFiles()) {
            if(directory.isDirectory()){
                String projectName = directory.getName();

                Project project = new Project(projectName);

                for (File file : directory.listFiles()) {
                    
                    try(ObjectInputStream inputFile = new ObjectInputStream(
                        new FileInputStream(RECOVERY_FILE_PATH + projectName + "/"+file.getName()))){
                        
                        if(file.getName().startsWith("members")){
                            System.out.println("Leggo i membri");
                            UsersDB members = (UsersDB) inputFile.readObject();
                            project.restoreMembers(members);
                        }
                        else{
                            Card card = (Card) inputFile.readObject();
                            System.out.println("Leggo la card "+card.getName());
                            project.restoreCard(card);
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                projects.put(projectName, project);
            }
        }
    }


    public static boolean saveFile(String path, Object obj, Class<?> type){

        String filePath = RECOVERY_FILE_PATH + path;

        try(ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(filePath));){

            output.writeObject(type.cast(obj));         //salvo in modo persistente le informazioni del progetto 
        }catch(Exception e){
            System.out.println("Impossibile serializzare il file");
            return false;
        }
        return true;
    }

}
