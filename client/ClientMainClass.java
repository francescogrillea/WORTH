package client;

import common.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ClientMainClass {

    private static final int RMI_Port = 4567;
    private static final int RMI_CALLBACK_Port = 4568;
    private static UsersDB localUsersDB;
    private static boolean logIn_effettuato;
    private static Registry r;

    public static void main(String[] args) throws RemoteException {

        logIn_effettuato = false;
        localUsersDB = new UsersDB();
        r = LocateRegistry.getRegistry(RMI_Port);
        //registra i metodi remoti per la registrazione
        //registra i metodi remoti utilizzati nelle callback
        NotificationSystemClientInterface callbackObj = new CallbackServiceClient(localUsersDB);
        Registry registry = LocateRegistry.getRegistry(RMI_CALLBACK_Port);


        System.out.println("Benvenuto in WORTH");
        printMenu();

        Scanner cmd_line = new Scanner(System.in);
        String function;
        String result = null;


        do{
            function = cmd_line.next();

            try{
                //funzione di registrazione
                if(function.equals("register")){
                    registerFunction(cmd_line);
                }
                //funzione di accesso
                else if(function.equals("login")){
    
                    //cerca i metodi condivisi
                    NotificationSystemServerInterface server = (NotificationSystemServerInterface)registry.lookup("NotificationService");

                    //si registra per la callback
                    //TODO- portarla fuori dal ciclo come le dichiarazioni di prima?
                    NotificationSystemClientInterface stub = (NotificationSystemClientInterface)
                        UnicastRemoteObject.exportObject(callbackObj, 0);
                                            
                    //chiede al server di registrarsi per la callback
                    server.registerForCallback(stub);
                    
                    logIn_effettuato = true;
                }
                //funzione di visualizzazione lista utenti
                else if(function.equals("listUsers")){

                    result = "List of all users";
                    System.out.println("< "+result);

                    for (User u : localUsersDB.listUser())
                        System.out.println("\t"+u.getUsername() + " - "+ u.getStatus());
                }
                //se non si ha un match stampa un messaggio coi possibili comandi
                else{
                    printMenu();
                    result = "make a choice";
                    System.out.println("< "+result);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            
        }while(!function.equals("close"));

        cmd_line.close();

    }

    public static void printMenu(){
        System.out.println("Operazioni disponibili:");
        System.out.println("\t\"register username password\" -to register a user");
        System.out.println("\t\"login username password\" -to login");
        System.out.println("\t\"listUsers\" -to show all registered users");

    }

    //answer: deve necessariamente effetture il login ?
    public static boolean checkLogIn(boolean mustLogIn){
        if(mustLogIn == true)
            return logIn_effettuato;
        return true;
    }

    public static void registerFunction(Scanner cmd_line) throws RemoteException, NotBoundException {
        RegistrationInterface registration = (RegistrationInterface) r.lookup("RegisterUser");
        String result = registration.register(cmd_line.next(), cmd_line.next());
        System.out.println("< "+result);
    }

}
