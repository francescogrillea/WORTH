import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.rmi.*;
import java.rmi.server.RemoteServer;

public class RegistrationClass extends RemoteServer implements RegistrationInterface {

    private UsersDB users;
    private String PATH;

    public RegistrationClass(UsersDB u, String p) {
        users = u;
        PATH = p;
    }

    @Override
    public String register(String nickname, String password) throws RemoteException {

        if(nickname.equals(null) || password.equals(null))
            return "Inserire username e password validi";
        
        for (User u : users.listUser()) {
            if(u.getUsername().equals(nickname))
                return "Utente "+nickname+" gia' registrato.";
        }
        users.addUser(new User(nickname));

        //scrivi nel file json l'aggiornamento
        /*
            TODO
                1. necessaria la scrittura ad ogni nuovo iscritto?
                2. se si, penso sia necessario farla in mutua esclusione, piu' utenti possono registrarsi contemporaneamente e scrivere sul file
        */
        try(
            ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(PATH+"utentiRegistrati.json"));){

            output.writeObject(users);
        }catch(Exception e){
            System.out.println(e);
        }
        return "Utente "+nickname+" inserito correttamente. Effettua il login";
    }



}
