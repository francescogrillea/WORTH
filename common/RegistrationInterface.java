package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrationInterface extends Remote {

    /*
        @Overview: il server pubblica questo metodo che permette ad un utente di registrarsi alla piattaforma
        @Return: un messaggio che indica l'esito dell'operazione
    */
    public String register(String[] myArgs) throws RemoteException;
}
