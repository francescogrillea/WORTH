package server;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;

import common.*;

public class RequestHandler implements Runnable {

    private ServerNotificationService notificationService;
    private Socket clientSocket;
    private UsersDB users;
    private User user;
    private boolean logIn_effettuato;
    private Object lock;    //accedo in mutua esclusione quando vado ad effettuare modifiche

    public RequestHandler(Socket c, UsersDB u, ServerNotificationService ns, Object l) throws IOException {
        clientSocket = c;
        users = u;
        user = null;
        logIn_effettuato = false;
        notificationService = ns;
        lock = l;
    }

    @Override
    public void run() {
        executeRequest();
    }

    public void executeRequest() {

        String request;
        String reply;

        while (true) {

            System.out.println("In while");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(clientSocket.getOutputStream()));) {

                while ((request = reader.readLine()) != null) {

                    System.out.println("Server receives: " + request);
                    String[] myArgs = request.split(" ");

                    if (myArgs[0].equals("login")) {    //LOGIN
                        reply = loginHandler(myArgs);
                        writer.write(reply + "\r\n");
                        writer.flush();

                    } 
                    else if (myArgs[0].equals("logout")) {    //LOGOUT
                        reply = logoutHandler(myArgs);
                        writer.write(reply + "\r\n");
                        writer.flush();

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    private String loginHandler(String[] myArgs) {

        if(myArgs.length != 3)
            return "Error. Use: login username password";
        
        String name = myArgs[1];
        String pass = myArgs[2];
        String reply = null;

        synchronized(lock){     //accedo in mutua esclusione alla struttura condivisa (users), eventualmente modificando il campo Status di un utente
            
            User currentUser = users.getUser(name);
    
            try {
                currentUser.equals(null);         // se l'utente non esiste
            } catch (Exception e) {
                return "Error. User not found";
            }
    
            if (logIn_effettuato == true)        //se ci si prova a collegare contemporaneamente da due utenti
                reply = "Error. User " + user.getUsername() + " currently logged in";
            else if (!(currentUser.getPassword().equals(pass)))        // se la password non matcha
                reply = "Error. Invalid password.";
            else if (currentUser.getStatus().equals("Online"))        // se l'utente ha gia' effettuato l'accesso
                reply = "Error. " + currentUser.getUsername() + " already logged in";
            else {
                reply = "User " + name + " logged in";
                user = currentUser;
                user.setOnline();
                logIn_effettuato = true;
                try {
                    notificationService.update(this.users);            //notifico la modifica
                } catch (RemoteException e) {
                    System.out.println("Cannot do callbacks");
                }
            }
        }
        return reply;
    }
    
    private String logoutHandler(String[] myArgs) {

        if(myArgs.length != 2)
            return "Error. Use: logout username";
    
        String name = myArgs[1];
        String reply = null;

        synchronized(lock){         //accedo in mutua esclusione alla struttura condivisa (users), eventualmente modificando il campo Status di un utente
            
            User currentUser = users.getUser(name);
    
            try {
                currentUser.equals(null);        // se l'utente non esiste
            } catch (Exception e) {
                return "Error. User not found";
            }
    
            if(!logIn_effettuato)        //se l'utente vuole eseguire il logout prima di eseguire il login
                reply = "Error. Login before you can logout";
            else if (!(user.getUsername().equals(name)))        //se un utente vuole disconnettere altri utenti
                reply = "Error. You don't have the permission to logout other users";
            else {
                user.setOffline();
                reply = name + " logged out";
                logIn_effettuato = false;
                
                try {
                    notificationService.update(this.users);            //notifico la modifica
                } catch (RemoteException e) {
                    System.out.println("Cannot do callbacks");
                }
            }
        }

        return reply;
    }


}
