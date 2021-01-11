package server;

import common.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

public class RegistrationClass extends RemoteServer implements RegistrationInterface {

    private static final long serialVersionUID = 1L;
    private final int RMI_Port = 4567;
    private UsersDB users;
    private ServerNotificationService notificationService;

    public RegistrationClass(UsersDB u, ServerNotificationService ns) {
        users = u;
        notificationService = ns;
	}

	//pubblico il riferimento all'oggetto remoto
    public void start(){
        try {
            RegistrationInterface stub = (RegistrationInterface) UnicastRemoteObject.exportObject(this, 0);  //creo l'oggetto da esportare
            LocateRegistry.createRegistry(RMI_Port);            //creo un registry sulla porta RMI
            Registry r = LocateRegistry.getRegistry(RMI_Port);  //recupero il registry appena creato
            r.rebind("RegisterUser", stub);                     //pubblico il riferimento sotto il nome di RegisterUser
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
            users.getUser(nickname);        //TODO da controllare sto controllo
        }catch(NullPointerException e){
            return "Error. User "+nickname+" already registered";
        }
        synchronized(users){

            users.addUser(new User(nickname, password));
            notificationService.update(users);
            ServerMainClass.saveFile("utentiRegistrati.json", users, UsersDB.class);
        }
        return "User "+nickname+" has been registered correctly. Login to continue.";
    }





}
