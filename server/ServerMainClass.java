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
    
    public static void restoreBackup(){
        //todo- da fare private

        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(RECOVERY_FILE_PATH + FILENAME_utentiRegistrati));) {
            users = (UsersDB) input.readObject();
            users.setAllOffline();
        }catch(EOFException e){
            //file vuoto
        }catch (Exception e) {
            System.out.println(e);
        }

        File recoveryDir = new File(RECOVERY_FILE_PATH);
        String projectName;

        for (File directory : recoveryDir.listFiles()) {
            if(directory.isDirectory()){
                projectName = directory.getName();

                try (ObjectInputStream input_members = new ObjectInputStream(
                        new FileInputStream(RECOVERY_FILE_PATH + projectName + membersFile));
                    ObjectInputStream input_todo = new ObjectInputStream(
                        new FileInputStream(RECOVERY_FILE_PATH + projectName + todoFile));
                    ObjectInputStream input_inprogress = new ObjectInputStream(
                        new FileInputStream(RECOVERY_FILE_PATH + projectName + inprogressFile));
                    ObjectInputStream input_toberevised = new ObjectInputStream(
                        new FileInputStream(RECOVERY_FILE_PATH + projectName + toberevisedFile));
                    ObjectInputStream input_done = new ObjectInputStream(
                        new FileInputStream(RECOVERY_FILE_PATH + projectName + doneFile));) {


                    UsersDB members = (UsersDB) input_members.readObject();
                    Cards todoList = (Cards) input_todo.readObject();
                    Cards inprogressList = (Cards) input_inprogress.readObject();
                    Cards toberevisedList = (Cards) input_toberevised.readObject();
                    Cards doneList = (Cards) input_done.readObject();
                    Project currentProject = new Project(projectName, members, todoList, inprogressList, toberevisedList, doneList);
                    projects.put(projectName, currentProject);
                }catch(Exception e){
                    e.printStackTrace();
                }
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
