package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.*;

public class NotificationClass {

    private final int RMI_CALLBACK_PORT = 4568;

    
    //pubblico i metodi remoti per l'iscrizione al servizio di notifica
    public ServerNotificationService start() {
        try {
            ServerNotificationService server = new ServerNotificationService();
            NotificationSystemServerInterface stub = (NotificationSystemServerInterface) UnicastRemoteObject.exportObject(server, 39000);

            LocateRegistry.createRegistry(RMI_CALLBACK_PORT);
            Registry r = LocateRegistry.getRegistry(RMI_CALLBACK_PORT);
            r.bind("NotificationService", stub);

            return server;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
