package server;

import java.io.*;
import java.util.*;
import common.*;

public class ServerMainClass {

    //dove sono contenuti i file per ripristinare lo stato del sistema
    public final static String RECOVERY_FILE_PATH = "./recovery/";
    private final static String FILENAME_utentiRegistrati = "utentiRegistrati.json"; 
    public final static String membersFile = "/members.json";

    // data structures
    public static UsersDB users;
    private static HashMap<String, Project> projects;

    //multicast chat
    private static int PORT = 5000;
    private static String IP = "239.0.0.0";

    public static void main(String[] args) {

        users = new UsersDB();
        projects = new HashMap<String, Project>();
        restoreBackup();

        // Il server stampa la lista iniziale degli utenti registrati. Per semplicita'
        // ogni utente ha la stessa password 'myPass'
        System.out.println("Ripristino lo stato iniziale: lista utenti");
        for (User u : users.listUser())
            System.out.println(u.getUsername() + " - " + u.getStatus());

        System.out.println("Ripristino lo stato iniziale: progetti");
        for (Project p : projects.values()) {
            System.out.println(p.getName());
            for (User u : p.getMembers())
                System.out.println("\t" + u.getUsername());

        }

        ServerNotificationService notificationService = new NotificationClass().start(); // RMI Callback
        new RegistrationClass(users, notificationService).start(); // RMI- creo un riferimento all'oggetto remoto
        new MultiThreadedServer(users, notificationService, projects).start(); // TCP

    }

    /*
        @Overview: ripristino lo stato del sistema andando ad esaminare la cartella recovery
    */
    private static void restoreBackup() {

        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(RECOVERY_FILE_PATH + FILENAME_utentiRegistrati));) {
            users = (UsersDB) input.readObject();
            users.setAllOffline();
        } catch (FileNotFoundException e) {
            System.out.println("System: no users registred");
        } catch (Exception e) {
            e.printStackTrace();
        }

        File recoveryDir = new File(RECOVERY_FILE_PATH);

        for (File directory : recoveryDir.listFiles()) {
            if (directory.isDirectory()) {
                String projectName = directory.getName();

                Project project = new Project(projectName);

                for (File file : directory.listFiles()) {

                    try (ObjectInputStream inputFile = new ObjectInputStream(
                            new FileInputStream(RECOVERY_FILE_PATH + projectName + "/" + file.getName()))) {

                        if (file.getName().startsWith("members")) {
                            UsersDB members = (UsersDB) inputFile.readObject();
                            project.restoreMembers(members);
                        } else {
                            Card card = (Card) inputFile.readObject();
                            project.restoreCard(card);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                project.setIP(generateIP());
                project.setPort(PORT);
                projects.put(projectName, project);
            }
        }
    }

    /*
        @Overview: salvo le modifiche in modo persistente
    */
    public static boolean saveFile(String path, Object obj, Class<?> type) {

        String filePath = RECOVERY_FILE_PATH + path;

        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(filePath));) {

            output.writeObject(type.cast(obj)); // salvo in modo persistente le informazioni del progetto
        } catch (Exception e) {
            System.out.println("System: cant save changes");
            return false;
        }
        return true;
    }

    /*
        @Overview: assegno un IP non utilizzato alla chat di ciascun progetto per comunicare tramite
                    il protocollo UPD
    */
    public synchronized static String generateIP(){

        String[] bytes = IP.split("\\.");

        int[] intBytes = new int[4];
        for (int i = 0; i < bytes.length; i++) 
            intBytes[i] = Integer.parseInt(bytes[i]);


        boolean stop = false;
        for (int i = intBytes.length-1; !stop; i--) {
            if(intBytes[i] < 255){
                intBytes[i]++;
                stop = true;
            }
            else
                intBytes[i] = 0;
        }

        String out = String.valueOf(intBytes[0])+"."+String.valueOf(intBytes[1])+"."+String.valueOf(intBytes[2])+"."+String.valueOf(intBytes[3]);
        IP = out;
        return out;
    }


    /*
        @Overview: restituisco la porta della connessione UDP
    */
    public static int getPort(){
        return PORT;
    }

}
