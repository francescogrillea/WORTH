package common;

import java.rmi.*;

public interface NotificationSystemClientInterface extends Remote{
    
    /*
        @Overview: il server notifica al client il verificarsi di un evento
    */
    public void notifyEvent(UsersDB newDB) throws RemoteException;
        
}
