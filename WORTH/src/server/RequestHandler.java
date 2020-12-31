package server;

import java.io.*;
import java.net.Socket;
import common.*;

public class RequestHandler implements Runnable {

    private Socket clientSocket;
    private UsersDB users;
    private User user;


    public RequestHandler(Socket c, UsersDB u) throws IOException {
        clientSocket = c;
        users = u;
    }

    @Override
    public void run() {
        executeRequest();
    }

    public void executeRequest() {

        String request;
        String reply;

        while (true) {

            System.out.println("Waiting for clients");

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));){

                while((request= reader.readLine() )!=null){
                    System.out.println("Server: ricevo "+ request);

                    if(request.equals("login")){
                        //TODO- fare i controlli per l'accesso
                        //instanziare l'User

                        reply = "login effettuato";
                        writer.write(reply+"\r\n");
                        writer.flush();
                    }

                }


            }catch(Exception e){
                e.printStackTrace();
            }
        }


    }
    

}
