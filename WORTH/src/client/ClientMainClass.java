package client;

import common.*;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
//import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ClientMainClass {


    
    private static final int RMI_Port = 4567;
    //private static final int RMI_CALLBACK_Port = 4568;
    private static final int TCP_Port = 4569;
    private static UsersDB localUsersDB;
    private static boolean logIn_effettuato;
    private static Registry registration_registry;

    public static void main(String[] args) throws RemoteException {

        localUsersDB = new UsersDB();
        //registra i metodi remoti per la registrazione
        registration_registry = LocateRegistry.getRegistry(RMI_Port);
        //registra i metodi remoti utilizzati nelle callback
        //NotificationSystemClientInterface callbackObj = new ClientNotificationService(localUsersDB);
        //Registry registry = LocateRegistry.getRegistry(RMI_CALLBACK_Port);


        //TCP Connection
        Socket socket = new Socket();        
        BufferedReader reader = null;
        BufferedWriter writer = null;


        System.out.println("Benvenuto in WORTH");
        printMenu();

        Scanner cmd_line = new Scanner(System.in);
        String function;
        String result;


        do{
            System.out.printf("> ");
            function = cmd_line.next();

            try{
                //funzione di registrazione
                if(function.equals("register")){
                    registerFunction(cmd_line);
                }
                //funzione di accesso
                else if(function.equals("login")){

                    socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), TCP_Port));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    
                    //invio il comando al server
                    writer.write(function+"\r\n");
                    writer.flush();

                    //ricevo la risposta del server
                    result = reader.readLine();
                    System.out.println("< "+result);
        
        

                    /*

                    //Registrazione al servizio di notifica
                    //cerca i metodi condivisi
                    NotificationSystemServerInterface server = (NotificationSystemServerInterface)registry.lookup("NotificationService");

                    //si registra per la callback
                    //TODO- portarla fuori dal ciclo come le dichiarazioni di prima?
                    NotificationSystemClientInterface stub = (NotificationSystemClientInterface)
                        UnicastRemoteObject.exportObject(callbackObj, 0);
                                            
                    //chiede al server di registrarsi per la callback
                    server.registerForCallback(stub);
                    */
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
                    socket.close(); //TODO- ricoedarsi di metterla nel logout
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

    public static void registerFunction(Scanner cmd_line) throws RemoteException, NotBoundException {

        String result;
        if(logIn_effettuato == true)
            result = "Errore. Effettuare il logout prima di una nuova registrazione";
        else{
            RegistrationInterface registration = (RegistrationInterface) registration_registry.lookup("RegisterUser");
            result = registration.register(cmd_line.next(), cmd_line.next());    
        }
        System.out.println("< "+result);
    }

}
