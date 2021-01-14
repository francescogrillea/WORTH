package client;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import common.*;

public class ClientNotificationService extends RemoteObject implements NotificationSystemClientInterface {

    private static final long serialVersionUID = 1L;
    private UsersDB users;              //un riferimento alla struttura dait locale del client

    public ClientNotificationService(UsersDB localUsersDB)throws RemoteException{
        super();
        users = localUsersDB;
    }


    /*
        @Overview: sostituisco la struttura dati locale del client con
            la struttura dati aggiornata passata dalla callback
    */
    @Override
    public void notifyEvent(UsersDB newDB) throws RemoteException {
        users.copy(newDB);
    }

}
