package client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.*;

public class Chat implements Runnable {

    private String username;                    //salvo l'username del mittente
    private String IP;
    private int PORT;
    private InetAddress ADDRESS;
    private boolean IS_LISTENING;               //flag che indica se l'indirizzo multicast riferito a un progetto e' ancora valido -> e' possibile che il progetto venga eliminato mentre siamo in ascolto, quindi 
    private ArrayList<String> unreadMessages;   //coda dei messaggi non letti
    private MulticastSocket group;

    public Chat(String username, String ip, int port) {
        this.username = username;
        this.IP = ip;
        this.PORT = port;
        this.IS_LISTENING = false;
        this.unreadMessages = new ArrayList<String>();
        
        try{
            this.ADDRESS = InetAddress.getByName(IP);
            group = new MulticastSocket(this.PORT);
            group.joinGroup(ADDRESS);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void close(){
        group.close();
        IS_LISTENING = false;
    }

    /*
        @Overview: indica se il progetto di cui quest'istanza è chat, è stato eliminato o meno
            quindi nel caso in cui il progetto venga eliminato e subito creato uno con lo stesso nome 
            è necessario aggiornare gli indirizzi multicast per la chat (altrimenti si potrebbe verificare
            che un utente stia scrivendo su una chat di un progetto vecchio ed eliminato di cui non fa parte)
    */
    public synchronized boolean isValid() {
        return IS_LISTENING;
    }

    /*
        @Overview: resto in ascolto dei messaggi che arrivano sul gruppo multicast e li salvo in una lista
    */
    @Override
    public void run() {

        //System.out.println("Resto in ascolto: "+IP + " " + PORT);
        IS_LISTENING = true;

        while(IS_LISTENING){

            try{
                DatagramPacket datagram = new DatagramPacket(new byte[512], 512);
                group.receive(datagram);        //ricevo il messaggio dal gruppo multicast

                String message = new String(datagram.getData(), "US-ASCII");
                if(message.startsWith("system: close"))     //nel caso in cui il sistema manda close, vuol dire che un progetto sta per essere eliminato
                    break;                                  //quindi smetto di stare in ascolto
                
                synchronized(unreadMessages){       //accedo in mutua esclusione alla coda di messaggi
                    unreadMessages.add(message.trim());
                }   
            }catch(SocketException e){      //se la socket viene chiusa
                //System.out.println("Finisco di ascoltare");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        IS_LISTENING = false;       //il progetto e' stato cancellato, quindi la chat puo' terminare
    }
    

    /*
        @Overview: leggo i messaggi arrivati sul gruppo multicast dopo l'ultima esecuzione del comando
    */
    public ArrayList<String> getMessages() {
        
        ArrayList<String> out = new ArrayList<>();  //creo una lista d'appoggio
        synchronized(unreadMessages){

            for (String string : unreadMessages)
                out.add(string);        //scrivo nella nuova lista i messaggi non letti
            unreadMessages.clear();           //infine pulisco la lista dei messaggi non letti
        }
        out.add("no more messages");
        return out;
    }

    /*
        @Overview: invia un messaggio al gruppo multicast
    */
    public void sendMessage(String message){

        String newMessage = username +": "+message;
        try{
            DatagramPacket packet = new DatagramPacket(newMessage.getBytes("US-ASCII"), newMessage.length(), ADDRESS, PORT);
            group.send(packet);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
