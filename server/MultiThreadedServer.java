package server;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import common.UsersDB;

public class MultiThreadedServer {

    private UsersDB users;
    private HashMap<String, Project> projects;
    private final int TCP_Port = 4569;
    private ServerNotificationService notificationService;

    public MultiThreadedServer(UsersDB u, ServerNotificationService ns, HashMap<String, Project> p) {
        users = u;
        notificationService = ns;
        projects = p;
    }

    public void start() {

        try (ServerSocket listeningSocket = new ServerSocket()){
            listeningSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), TCP_Port));      //il server resta in ascolto sulla porta 4569
            
            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();   //creo un threadpool
            while(true){
                Socket socket = listeningSocket.accept();       //accetto le richieste di connessione da parte degli utenti
                System.out.println("System: un utente si e' connesso al sistema");  
                threadPool.execute(new RequestHandler(socket, users, notificationService, projects));   //gestisco le loro richieste
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        

	}

	

}
