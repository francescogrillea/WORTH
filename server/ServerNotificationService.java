package server;

import java.rmi.*;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import common.*;


public class ServerNotificationService extends RemoteServer implements NotificationSystemServerInterface {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<NotificationSystemClientInterface> clients;

    public ServerNotificationService() throws RemoteException{
        super();
        clients = new ArrayList<NotificationSystemClientInterface>();
    }  

    @Override
    public synchronized void registerForCallback(NotificationSystemClientInterface clientInterface) {
        
        if(!clients.contains(clientInterface)){
            clients.add(clientInterface);
            System.out.println("new client registered");
        }
    }

    @Override
    public synchronized void unregisterForCallback(NotificationSystemClientInterface clientInterface) {
        
        clients.remove(clientInterface);
    }

    public void update(UsersDB newDB)throws RemoteException {
        System.out.println("Update");
        doCallbacks(newDB);
    }

    public synchronized void doCallbacks(UsersDB newDB) throws RemoteException{
        
        Iterator<NotificationSystemClientInterface> i = clients.iterator();
        while(i.hasNext()){
            NotificationSystemClientInterface client = (NotificationSystemClientInterface)i.next();
            client.notifyEvent(newDB);
        }
        System.out.println("Callbacks completed");
    }
    
}
