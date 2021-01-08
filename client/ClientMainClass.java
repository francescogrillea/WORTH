package client;

import common.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/*
TODO

*/

public class ClientMainClass {

    //porte usate per la comunicazione 
    private static final int RMI_Port = 4567;
    private static final int RMI_CALLBACK_Port = 4568;
    private static final int TCP_Port = 4569;

    private static UsersDB localUsersDB;            //stuttura dati degli utenti aggiornata tramite callbacks
    private static HashMap<String, Chat> chats;     //struttura dati che tiene la corrispondenza Progetti-Chat
    private static boolean logIn_effettuato;        //flag di controllo per verificare se l'utente e' loggato

    private static Registry registration_registry;  //TODO aggiungere commenti e renderle piu' pulite
    private static RegistrationInterface registration;

    public static void main(String[] args) {
        
        localUsersDB = new UsersDB();
        chats = new HashMap<String, Chat>();
        
        logIn_effettuato = false;
        Socket socket;
        BufferedReader reader;
        BufferedWriter writer;

        System.out.println("Welcome in WORTH");
        System.out.println("Please login or register to proceed");

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
                    else if(message.startsWith("sendChat")){
                        result = sendChatHandler(myArgs);
                        System.out.println("< "+result);
                        continue;
                    }
                    else if(message.startsWith("readChat")){
                        for (String msg : readChatHandler(myArgs)) 
                            System.out.println("< "+ msg);
                        
                        continue;
                    }


                    //tutte le altre operazioni vengono gestite direttamente dal server
                    writer.write(message+"\r\n");   //send command to server
                    writer.flush();
                    while(!(result = reader.readLine()).equals("")){

                        if(message.startsWith("joinChat") && !result.startsWith("Error"))
                            result = joinChat(result);
                        
                        else if(message.startsWith("logout") && !result.startsWith("Error")){
                            logIn_effettuato = false;
                            chats.clear();
                        }
                        System.out.println("< "+result);
                    } 
                    
                }catch(Exception e){
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
                
            }while(!message.equals("close"));
            //TODO ? fare la socket.close();

            
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    private static ArrayList<String> readChatHandler(String[] myArgs){

        ArrayList<String> out = new ArrayList<String>();
        if(myArgs.length != 2){
            out.add("Error. Use readChat projectName");
            return out;
        }
            

        String projectName = myArgs[1];
        Chat chat = chats.get(projectName);

        try{
            chat.equals(null);
        }catch(NullPointerException e){
            out.add("Error. You have not joined this chat");
            return out;
        }

        out = (ArrayList<String>)chat.getMessages();
        return out;
    }


    private static String sendChatHandler(String[] myArgs) {
        
        if(myArgs.length < 3)
            return "Error. Use sendChat projectName message";

        String projectName = myArgs[1];

        Chat chat = chats.get(projectName);
        try{
            chat.equals(null);
        }catch(NullPointerException e){
            return "Error. You have not joined this chat";
        }

        String message = "";

        for (int i = 2; i < myArgs.length; i++) 
            message = message + myArgs[i]+" ";
        
        chat.sendMessage(message);
        return "Message sent";
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

    /*
        @Overview: controllo se l'utente e' gia' unito alla chat del progetto
    */
    private static boolean alreadyJoined(String projectName){
        try{
            Chat c = chats.get(projectName);        //controllo se ho già salvato 
            if(c.isValid())                         //controllo che la chat non sia di un progetto precedentemente eliminato
                return true;
        }catch(NullPointerException e){ }            //la corrispondenza non esiste

        return false;
    }


    /*
        @Overview: collego l'utente alla chat del progetto
    */
    private static String joinChat(String result){

        String[] data = result.split(" ");
        String username = data[0];
        String projectName = data[1];

        if(alreadyJoined(projectName))
            return "Error. User already joined this chat";

        String ip = data[2];
        int port = Integer.parseInt(data[3]);
        
        Chat chat =  new Chat(username, ip, port);
        new Thread(chat).start();
        chats.put(projectName, chat);
        return "User joined project's chat";
    }

}
