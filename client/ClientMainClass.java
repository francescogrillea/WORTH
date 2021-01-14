package client;

import common.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ClientMainClass {

    // porte usate per la comunicazione
    private static final int RMI_Port = 4567;
    private static final int RMI_CALLBACK_Port = 4568;
    private static final int TCP_Port = 4569;

    private static UsersDB localUsersDB; // stuttura dati degli utenti aggiornata tramite callbacks
    private static HashMap<String, Chat> chats; // struttura dati che tiene la corrispondenza Progetti-Chat
    private static boolean logIn_effettuato; // flag di controllo per verificare se l'utente e' loggato

    private static Registry registration_registry;
    private static RegistrationInterface registration;

    public static void main(String[] args) {

        localUsersDB = new UsersDB();
        chats = new HashMap<String, Chat>();

        logIn_effettuato = false;
        // socket per la connessione TCP
        BufferedReader reader; // stream dal server TCP al client
        BufferedWriter writer; // stream dal client TCP al server

        System.out.println("Welcome in WORTH");
        System.out.println("Please login or register to proceed. If you need help send \"help\"");

        String message = null;
        String result = null;

        try (Socket socket = new Socket();
            BufferedReader cmd_line = new BufferedReader(new InputStreamReader(System.in));) {

            // RMI- ottengo un riferimento all'oggetto remoto in modo da utilizzare i suoi metodi
            registration_registry = LocateRegistry.getRegistry(RMI_Port); // recupero la registry sulla porta RMI
            registration = (RegistrationInterface) registration_registry.lookup("RegisterUser"); // richiedo l'oggetto
                                                                                                 // dal nome pubblico

            // Callbacks
            NotificationSystemClientInterface callbackObj = new ClientNotificationService(localUsersDB);
            Registry registry = LocateRegistry.getRegistry(RMI_CALLBACK_Port);
            NotificationSystemServerInterface server = (NotificationSystemServerInterface) registry.lookup("NotificationService");
            NotificationSystemClientInterface stub = (NotificationSystemClientInterface) UnicastRemoteObject.exportObject(callbackObj, 0);

            // TCP Connection
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), TCP_Port));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            do {
                System.out.println();
                System.out.printf("> ");

                try {
                    message = cmd_line.readLine();
                    String[] myArgs = message.split(" ");

                    if (message.startsWith("close")) {
                        break;
                    } else if (message.startsWith("login")) {   // LOGIN - utente accede alla piattaforma 
                        server.registerForCallback(stub); // chiede al server di registrarsi per la callback
                    } else if (message.startsWith("register")) { // REGISTER- utente si registra alla piattaforma
                        registerFunction(myArgs);
                        continue;
                    } else if (message.startsWith("listUsers")) { // LISTUSERS- visualizza lista utenti
                        listUsersFunction();
                        continue;
                    } else if (message.startsWith("listOnlineUsers")) { // LISTONLINEUSERS- visualizza lista utenti online
                        listOnlineUsersFunction();
                        continue;
                    } else if (message.startsWith("sendChat")) {        // SEND CHAT - invia un messaggio sulla chat
                        result = sendChatHandler(myArgs);
                        System.out.println("< " + result);
                        continue;
                    } else if (message.startsWith("readChat")) {        //READ CHAT - legge i messaggi dalla chat
                        ArrayList<String> messages = readChatHandler(myArgs);
                        System.out.println("< You have " + (messages.size()-1) + " unread messages: ");
                        for (String msg : messages)
                            System.out.println("\t" + msg);
                        continue;
                    }

                    writer.write(message + "\r\n");                     // invio la richiesta al server
                    writer.flush();
                    while (!(result = reader.readLine()).equals("")) {  // leggo la risposta del server

                        if (message.startsWith("joinChat") && !result.startsWith("Error"))
                            result = joinChat(result);
                        else if (message.startsWith("login") && !result.startsWith("Error")) { // gestisco lo stato a seguito del login
                            logIn_effettuato = true;
                        } else if (message.startsWith("logout") && !result.startsWith("Error")) { // gestisco lo stato a seguito del logout
                            server.unregisterForCallback(stub); // mi disiscrivo dal servizio di notifica
                            logIn_effettuato = false;
                            chats.clear();                      //resetto le strutture dati per una nuova sessione
                            localUsersDB.clear();
                        }
                        System.out.println("< " + result);
                    }

                } catch (Exception e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }

            } while (!message.equals("close"));
            
            //il client termina in maniera pulita
            UnicastRemoteObject.unexportObject(callbackObj, false); 
            if (server != null)
                server.unregisterForCallback(stub); // mi disiscrivo dalle callback

            for (Chat c : chats.values())   //termino l'ascolto su tutte le chat
                c.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /*
        @Overview: funzione di registazione alla piattaforma, dove vengono invocati i metodi remoti
    */
    public static void registerFunction(String[] myArgs) throws RemoteException, NotBoundException {
        
        String result;

        if(logIn_effettuato == true)
            result = "Error. Log out before new registration";
        else
            result = registration.register(myArgs);     //RMI- invoco il metodo remoto
        
        System.out.println("< "+result);
    }


    /*
        @Overview: funzione che esamina la lista locale degli utenti
    */
    public static void listUsersFunction(){
        String result = "List of all users";
        System.out.println("< "+result);

        for (User u : localUsersDB.listUser())
            System.out.println("\t"+u.getUsername() + " - "+ u.getStatus());
    }

    /*
        @Overview: funzione che esamina la lista locale degli utenti online
    */
    private static void listOnlineUsersFunction() {
        String result = "Online users";
        System.out.println("< "+result);

        for (User u : localUsersDB.listUser())
            if(u.getStatus().equals("Online"))
                System.out.println("\t"+u.getUsername() + " - "+ u.getStatus());
    }

    /*
        @Overview: collego l'utente alla chat del progetto
    */
    private static String joinChat(String result){

        String[] data = result.split(" ");
        String username = data[0];
        String projectName = data[1];
        String ip = data[2];
        int port = Integer.parseInt(data[3]);

        Chat c = chats.get(projectName);
        try{
            c.equals(null);
            if(c.isValid())
                return "Error. You have already joined this chat";
            return "Error. This chat has been deleted";
        }catch(NullPointerException e){ }

        Chat chat =  new Chat(username, ip, port);
        new Thread(chat).start();
        chats.put(projectName, chat);
        return "User joined project's chat";
    }

    /*
        @Overview: leggo dal 'buffer' della chat che contiene i messaggi non letti
    */
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

        if(!chat.isValid()){            //se sto provando a leggere da una chat, di un progetto eliminato
            chats.remove(projectName);  //rimuovo la entry
            out.add("Error. You're trying to read the chat from a deleted project");
        }
        else
            out = (ArrayList<String>)chat.getMessages();    //altrimenti eseguo la funzione
        return out;
    }


    /*
        @Overview: invio un messaggio nella chat
    */
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

        if(!chat.isValid()){                //se sto provando a leggere da una chat, di un progetto eliminato
            chats.remove(projectName);      //rimuovo la entry
            return "Error. You're trying to write in the chat of a deleted project";
        }
                                            //altrimenti ci scrivo
        String message = "";
        for (int i = 2; i < myArgs.length; i++) 
            message = message + myArgs[i]+" ";
        
        chat.sendMessage(message);
        return "Message sent";
    }

}
