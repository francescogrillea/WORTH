package server;

import java.rmi.*;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import common.*;


public class CallbackServiceServer extends RemoteServer implements NotificationSystemServerInterface {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<NotificationSystemClientInterface> clients;

    public CallbackServiceServer() throws RemoteException{
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
        
        if(clients.remove(clientInterface))
            System.out.println("cliente rimosso");
        else
            System.out.println("errore occurred");
    }

    public void update(UsersDB newDB)throws RemoteException, InterruptedException {
        Thread.sleep(10000);

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
