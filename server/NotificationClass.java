package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.*;

public class NotificationClass {

    private final int RMI_CALLBACK_PORT = 4568;
    /*
    private UsersDB users;

    public NotificationClass(UsersDB u) {
        users = u;
    }
    */

    public CallbackServiceServer start() {
        try {
            CallbackServiceServer server = new CallbackServiceServer();
            NotificationSystemServerInterface stub = (NotificationSystemServerInterface) UnicastRemoteObject.exportObject(server, 39000);

            LocateRegistry.createRegistry(RMI_CALLBACK_PORT);
            Registry r = LocateRegistry.getRegistry(RMI_CALLBACK_PORT);
            r.bind("NotificationService", stub);

            //invio i valori aggiornati
            //server.update(users);
            return server;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
