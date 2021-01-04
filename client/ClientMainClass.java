package client;

import common.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

public class ClientMainClass {

    private static final int RMI_Port = 4567;
    private static final int RMI_CALLBACK_Port = 4568;
    private static final int TCP_Port = 4569;
    private static UsersDB localUsersDB;        //stuttura dati degli utenti (locale)
    private static boolean logIn_effettuato;    //flag per eseguire i contolli di sicurezza nelle chiamate dei metodi
    private static Registry registration_registry;
    private static RegistrationInterface registration;
    
    public static void main(String[] args) {
        
        logIn_effettuato = false;
        localUsersDB = new UsersDB();
        Socket socket;
        BufferedReader reader;
        BufferedWriter writer;

        System.out.println("Benvenuto in WORTH");
        printMenu();

        String message = null;
        String result = null;


        try{
            //RMI
            registration_registry = LocateRegistry.getRegistry(RMI_Port);
            registration = (RegistrationInterface) registration_registry.lookup("RegisterUser");
            
            //Callbacks
            NotificationSystemClientInterface callbackObj = new ClientNotificationService(localUsersDB);
            Registry registry = LocateRegistry.getRegistry(RMI_CALLBACK_Port);
            NotificationSystemServerInterface server = (NotificationSystemServerInterface)registry.lookup("NotificationService");
            NotificationSystemClientInterface stub = (NotificationSystemClientInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
    
            //TCP Connection
            socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), TCP_Port));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    
            //cmd line
            BufferedReader cmd_line = new BufferedReader(new InputStreamReader(System.in));

            do{
                System.out.printf("> ");
                
                try{
                    message = cmd_line.readLine();
                    String[] myArgs = message.split(" ");
    
                    if(message.startsWith("register")){         //REGISTER- utente si registra alla piattaforma
                        registerFunction(myArgs);
                        continue;
                    }
                    else if(message.startsWith("listUsers")){   //LISTUSERS- visualizza lista utenti
                        listUsersFunction();
                        continue;
                    }
                    else if(message.startsWith("listOnlineUsers")){     //LISTONLINEUSERS- visualizza lista utenti online
                        listOnlineUsersFunction();
                        continue;
                    }
                    else if(message.startsWith("login")){       //LOGIN- utente accede alla piattaforma                 
                        //TODO capire se va messo in un if come per il logout. Piu' invocazioni di login possono registratlo piu' volte?
                        server.registerForCallback(stub);   //chiede al server di registrarsi per la callback                                    
                    }
                    else if(message.startsWith("logout")){      //LOGOUT- utente si disconnette dalla piattaforma
                        if(logIn_effettuato)
                            server.unregisterForCallback(stub);    //chiede al server di disiscriversi dal servizio di notifica                       
                    }
                    //tutte le altre operazioni vengono gestite direttamente dal server
                    writer.write(message+"\r\n");   //send command to server
                    writer.flush();
                    while(!(result = reader.readLine()).equals("")) //read response from server
                        System.out.println("< "+result);

                }catch(Exception e){
                    System.out.println("An error occurred.");
                    printMenu();
                    e.printStackTrace();
                }
                
            }while(!message.equals("close"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public static void printMenu() {
        System.out.println("Operazioni disponibili:");
        System.out.println("\t\"register username password\" -to register a user");
        System.out.println("\t\"login username password\" -to login");
        System.out.println("\t\"logout username\" -to logout");
        System.out.println("\t\"listUsers\" -to show all registered users");
        System.out.println("\t\"listOnlineUsers\" -to show online users");
        System.out.println("\t\"createProject projectName\" -to create a new project");
        System.out.println("\t\"listProjects\" -to show all projects of a user");
        System.out.println("\t\"showMembers projectName\" -to show all users in a project");
        System.out.println("\t\"addMember projectName username\" -add a user to a project");

    }

    public static void registerFunction(String[] myArgs) throws RemoteException, NotBoundException {
        
        String result;

        if(logIn_effettuato == true)
            result = "Error. Log out before new registration";
        else
            result = registration.register(myArgs);    
        
        System.out.println("< "+result);
    }

    public static void listUsersFunction(){
        String result = "List of all users";
        System.out.println("< "+result);

        for (User u : localUsersDB.listUser())
            System.out.println("\t"+u.getUsername() + " - "+ u.getStatus());
    }

    private static void listOnlineUsersFunction() {
        String result = "List of all online users";
        System.out.println("< "+result);

        for (User u : localUsersDB.listUser())
            if(u.getStatus().equals("Online"))
                System.out.println("\t"+u.getUsername() + " - "+ u.getStatus());
    }

}
