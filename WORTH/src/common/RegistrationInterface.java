package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrationInterface extends Remote {

    /*
        @Overview: il server permette ad un utente di registrarsi a WORTH
        @Return: un messaggio che indica l'esito dell'operazione
    */
    public String register(String nickname, String password) throws RemoteException;
}
