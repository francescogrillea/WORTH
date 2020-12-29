package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationSystemServerInterface extends Remote {
    
    /*
        @Overview: il server registra il client al servizio di callback
    */
    public void registerForCallback(NotificationSystemClientInterface clientInterface) throws RemoteException;

    /*
        @Overview: il server cancella la registrazione del client al servizio di callback
    */
    public void unregisterForCallback(NotificationSystemClientInterface clientInterface) throws RemoteException;

}
