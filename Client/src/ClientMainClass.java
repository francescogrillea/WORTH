import java.rmi.*;
import java.rmi.registry.*;
import java.util.Scanner;

public class ClientMainClass {
    public static void main(String[] args) {

        System.out.println("Benvenuto in WORTH");
        System.out.println("Operazioni disponibili:");
        System.out.println("\t1. register username password");

        // prendi la linea intera e parsala
        Scanner cmd_line = new Scanner(System.in);
        String function;

        do{
            function = cmd_line.next();
            
            // registra l'utente
            if (function.equals("register")) {
                
                try {
                    Registry r = LocateRegistry.getRegistry(4567);
                    RegistrationInterface registration = (RegistrationInterface) r.lookup("RegisterUser");
                    
                    String result = registration.register(cmd_line.next(), cmd_line.next());
                    System.out.println("< "+result);
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
            }
            
        }while(!function.equals("close"));

        cmd_line.close();

    }

}
