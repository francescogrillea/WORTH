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

    /*
        @Overview: iscrizione al servizio di notifica
    */
    @Override
    public synchronized void registerForCallback(NotificationSystemClientInterface clientInterface) {
        
        if(!clients.contains(clientInterface)){
            clients.add(clientInterface);
            System.out.println("System: new client registered to notification system");
        }
    }

    /*
        @Overview: annullamento all'iscrizione del servizio di notifica
    */
    @Override
    public synchronized void unregisterForCallback(NotificationSystemClientInterface clientInterface) {
        
        clients.remove(clientInterface);
    }

    
    /*
        @Overview: invio della notfica
    */
    public void update(UsersDB newDB)throws RemoteException {
        doCallbacks(newDB);
    }

    
    /*
        @Overview: per ogni cliente chiamo la sua funzione di aggiornamento
    */
    public synchronized void doCallbacks(UsersDB newDB) throws RemoteException{
        
        Iterator<NotificationSystemClientInterface> i = clients.iterator();
        while(i.hasNext()){ 
            NotificationSystemClientInterface client = (NotificationSystemClientInterface)i.next();
            client.notifyEvent(newDB);
        }
        System.out.println("System: update sent");
    }
    
}
