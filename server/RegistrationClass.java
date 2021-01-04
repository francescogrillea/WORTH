package server;

import common.*;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

public class RegistrationClass extends RemoteServer implements RegistrationInterface {

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    private UsersDB users;
    private ServerNotificationService notificationService;

    public RegistrationClass(UsersDB u, ServerNotificationService ns) {
        users = u;
        notificationService = ns;
	}

	//publish remote method
    public void start(){
        int RMI_Port = 4567;
        try {
            //TODO controllare se la porta di stub dev'essere nota
            RegistrationInterface stub = (RegistrationInterface) UnicastRemoteObject.exportObject(this, RMI_Port);
            LocateRegistry.createRegistry(RMI_Port);
            Registry r = LocateRegistry.getRegistry(RMI_Port);
            r.rebind("RegisterUser", stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }   
    }


    @Override
    public synchronized String register(String[] myArgs) throws RemoteException {

        if(myArgs.length != 3)
            return "Error. Use registration username password";

        String nickname = myArgs[1];
        String password = myArgs[2];

        try{
            users.getUser(nickname);
        }catch(NullPointerException e){
            return "Error. User "+nickname+" already registered";
        }

        users.addUser(new User(nickname, password));
        notificationService.update(users);

        try(
            ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(ServerMainClass.RECOVERY_FILE_PATH+"utentiRegistrati.json"));){

            output.writeObject(users);
        }catch(Exception e){
            System.out.println(e);
        }
        return "User "+nickname+" has been registered correctly. Login to continue.";
    }





}
