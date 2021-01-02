package server;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

import common.UsersDB;

public class MultiThreadedServer {

    private UsersDB users;
    private final int TCP_Port = 4569;
    private ServerNotificationService notificationService;
    private Object lock;


    public MultiThreadedServer(UsersDB u, ServerNotificationService ns) {
        users = u;
        notificationService = ns;
        lock = new Object();
    }

    public void start() {

        try (ServerSocket listeningSocket = new ServerSocket()){
            listeningSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), TCP_Port));
            
            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            while(true){
                Socket socket = listeningSocket.accept();
                System.out.println("Un utente si e' connesso al sistema");
                threadPool.execute(new RequestHandler(socket, users, notificationService, lock));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        

	}

	

}
