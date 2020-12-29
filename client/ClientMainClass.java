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
    public static void main(String[] args) {

        boolean logIn_effettuato = false;

        System.out.println("Benvenuto in WORTH");
        printMenu();

        Scanner cmd_line = new Scanner(System.in);
        String function;

        do{
            function = cmd_line.next();

            switch(function){
                case "register":
                    try {
                        Registry r = LocateRegistry.getRegistry(RMI_Port);
                        RegistrationInterface registration = (RegistrationInterface) r.lookup("RegisterUser");
                        
                        String result = registration.register(cmd_line.next(), cmd_line.next());
                        System.out.println("< "+result);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                    break;
                
                case "login":

                    if(logIn_effettuato == true){
                        System.out.println("Login gia' effettuato. Effettuare il logout per accedere con un altro account.");
                        continue;
                    }

                    //registra al callback
                    try{

                        //cerca i metodi condivisi
                        Registry registry = LocateRegistry.getRegistry(RMI_CALLBACK_Port);
                        NotificationSystemServerInterface server = (NotificationSystemServerInterface)registry.lookup("NotificationService");
                        
                        //si registra per la callback
                        NotificationSystemClientInterface callbackObj = new CallbackServiceClient(localUsersDB);
                        NotificationSystemClientInterface stub = (NotificationSystemClientInterface)
                            UnicastRemoteObject.exportObject(callbackObj, 0);
                        
                        //chiede al server di registrarsi per la callback
                        server.registerForCallback(stub);
                        logIn_effettuato = true;

                    }catch(RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                    break;

                case "listUsers":
                    //TODO da fare in mutua esclusione ? 
                    for (User u : localUsersDB.listUser())
                        System.out.println(u.getUsername() + " - "+ u.getStatus());
                    break;

                default:
                    printMenu();
                    break;


            }        
            
        }while(!function.equals("close"));

        cmd_line.close();

    }

    public static void printMenu(){
        System.out.println("Operazioni disponibili:");
        System.out.println("\t\"register username password\" -to register a user");
        System.out.println("\t\"listUsers\" -to show all registered users");

    }

}
