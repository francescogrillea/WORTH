package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.*;

public class NotificationClass {

    private UsersDB users;
    private final int RMI_CALLBACK_PORT = 4568;

    public NotificationClass(UsersDB u) {
        users = u;
    }

    public void start() {
        try {
            CallbackServiceServer server = new CallbackServiceServer();
            NotificationSystemServerInterface stub = (NotificationSystemServerInterface) UnicastRemoteObject.exportObject(server, 39000);

            LocateRegistry.createRegistry(RMI_CALLBACK_PORT);
            Registry r = LocateRegistry.getRegistry(RMI_CALLBACK_PORT);
            r.bind("NotificationService", stub);

            //invio i valori aggiornati
            server.update(users);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
