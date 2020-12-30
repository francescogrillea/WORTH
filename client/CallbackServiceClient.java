package client;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import common.*;

public class CallbackServiceClient extends RemoteObject implements NotificationSystemClientInterface {

    private static final long serialVersionUID = 1L;
    private UsersDB users;

    public CallbackServiceClient(UsersDB localUsersDB)throws RemoteException{
        super();
        users = localUsersDB;
    }


    @Override
    public void notifyEvent(UsersDB newDB) throws RemoteException {

        //TODO- trovare un modo per rendere la modifica disponibile subito al client senza l'utilizzo di un altro metodo
        users.copy(newDB);
        System.out.println("Il client riceve una notifica dal sistema");
    }

}
