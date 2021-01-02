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
    private String PATH;
    private ServerNotificationService notificationService;

    public RegistrationClass(UsersDB u, String fp, ServerNotificationService ns) {
        users = u;
        notificationService = ns;
        PATH = fp;
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
    public String register(String nickname, String password) throws RemoteException {

        if(nickname.equals(null) || password.equals(null))
            return "Inserire username e password validi";
        
        for (User u : users.listUser()) {
            if(u.getUsername().equals(nickname))
                return "Utente "+nickname+" gia' registrato.";
        }
        users.addUser(new User(nickname, password));
        notificationService.update(users);

        //scrivi nel file json l'aggiornamento
        /*
            TODO
                1. necessaria la scrittura ad ogni nuovo iscritto?
                2. se si, penso sia necessario farla in mutua esclusione, piu' utenti possono registrarsi contemporaneamente e scrivere sul file
        
        */
        try(
            ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(PATH+"utentiRegistrati.json"));){

            output.writeObject(users);
        }catch(Exception e){
            System.out.println(e);
        }
        return "Utente "+nickname+" inserito correttamente. Effettua il login";
    }





}
