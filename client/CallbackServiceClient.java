package client;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import common.*;

public class CallbackServiceClient extends RemoteObject implements NotificationSystemClientInterface {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private UsersDB users;

    //TODO- forse posso passare il DB come parametro per tenerlo sempre aggioranto

    // creazione di un sistema callback verso il client
    public CallbackServiceClient(UsersDB localUsersDB)throws RemoteException{
        super();
        users = localUsersDB;
    }


    @Override
    public void notifyEvent(UsersDB newDB) throws RemoteException {

        System.out.println("All'interno della funzione di notifica");
        users = newDB;
        
        for (User u : users.listUser())
            System.out.println(u.getUsername() + " - "+ u.getStatus());

        for (User u : newDB.listUser())
            System.out.println(u.getUsername() + " - "+ u.getStatus());

        System.out.println("Termino la funzione di notifica");

    }
    
}
