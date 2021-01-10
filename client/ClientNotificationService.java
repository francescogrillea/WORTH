package client;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import common.*;

public class ClientNotificationService extends RemoteObject implements NotificationSystemClientInterface {

    private static final long serialVersionUID = 1L;
    private UsersDB users;

    public ClientNotificationService(UsersDB localUsersDB)throws RemoteException{
        super();
        users = localUsersDB;
    }


    @Override
    public void notifyEvent(UsersDB newDB) throws RemoteException {
        users.copy(newDB);
        //System.out.println("Il client riceve una notifica dal sistema");
    }

}
