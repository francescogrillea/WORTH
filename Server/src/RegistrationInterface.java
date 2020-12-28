import java.rmi.*;

public interface RegistrationInterface extends Remote {


    /*
        Il server permette ad un utente di registrarsi
        Restituisce un messaggio di esito

        TODO- fare i commenti dell'interfaccia LeviStyle
    */
    public String register(String nickname, String password) throws RemoteException;

}
