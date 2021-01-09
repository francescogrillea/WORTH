package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.util.*;

import common.*;

public class RequestHandler implements Runnable {

    private ServerNotificationService notificationService;
    private Socket clientSocket;
    private UsersDB users;
    private HashMap<String, Project> projects;
    private User user;
    private boolean logIn_effettuato;

    public RequestHandler(Socket c, UsersDB u, ServerNotificationService ns,  HashMap<String, Project> p) throws IOException {
        clientSocket = c;
        users = u;
        user = null;
        logIn_effettuato = false;
        notificationService = ns;
        projects = p;
    }

    @Override
    public void run() {
        executeRequest();
    }

    public void executeRequest() {

        String request;
        String reply;

        while (true) {

            System.out.println("In while");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(clientSocket.getOutputStream()));) {

                while ((request = reader.readLine()) != null) {

                    System.out.println("Server receives: " + request);
                    String[] myArgs = request.split(" ");

                    if (myArgs[0].equals("login"))  // LOGIN
                        reply = loginHandler(myArgs);

                    else if (myArgs[0].equals("logout"))  // LOGOUT
                        reply = logoutHandler(myArgs);

                    else if (myArgs[0].equals("createProject"))  // CREATE PROJECT
                        reply = createProjectHandler(myArgs);

                    else if(myArgs[0].equals("cancelProject"))    //DELETE A PROJECT
                        reply = cancelProjectHandler(myArgs);

                    else if (myArgs[0].equals("listProjects"))  // LIST USER's PROJECTS
                        reply = listProjectsHandler(myArgs);
                    
                    else if(myArgs[0].equals("showMembers"))   //SHOW THE MEMERS OF A PROJECT
                        reply = showMembersHandler(myArgs);
                    
                    else if(myArgs[0].equals("addMember"))     //ADD A MEMBER TO A PROJECT
                        reply = addMemberHandler(myArgs);
                    
                    else if(myArgs[0].equals("addCard"))        //ADD A CARD TO A PROJECT
                        reply = addCardHandler(myArgs);

                    else if(myArgs[0].equals("showCards"))      //SHOW ALL THE CARDS OF A PROJECT
                        reply = showCardsHandler(myArgs);
                    
                    else if(myArgs[0].equals("showCard"))       //FIND A CARD IN A PROJECT
                        reply = showCardHandler(myArgs);
                    
                    else if(myArgs[0].equals("moveCard"))       //MOVE A CARD
                        reply = moveCardHandler(myArgs);
                    
                    else if(myArgs[0].equals("getCardHistory")) //GET CARD HISTORY
                        reply = getCardHistoryHandler(myArgs);
                    
                    else if(myArgs[0].equals("joinChat"))
                        reply = joinChatHandler(myArgs);
                    else                                        //INVALID OPTION
                        reply = invalidOptionHandler();
                    
                    writer.write(reply + "\n\r\n");
                    writer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    //joinChat(projectName)
    private String joinChatHandler(String[] myArgs) {
        
        if(myArgs.length != 2)
            return "Error. Use joinChat projectName";
        
        if(!logIn_effettuato)
            return "Error. Login before do this";    
        
        String projectName = myArgs[1];
        String ip;
        int port;

        Project project;
        synchronized(projects){             //gestisco la concorrenza nell'accesso ai vari progetti
            project = projects.get(projectName);
        }

        try{
            project.equals(null);
        }catch(NullPointerException e){
            return "Error. Project "+ projectName +" not found";
        }

        synchronized(project){              //gestisco la concorrenza all'interno del singolo progetto
            if(!(project.hasMember(this.user.getUsername())))
                return "Error. User "+this.user.getUsername() +" isn't a member of the project.";

            ip = project.getIP();
            port = project.getPort();
        }
        return this.user.getUsername() +" "+projectName + " " + ip + " " + port;
    }


    //getCardHistory(projectName, cardName)
    private String getCardHistoryHandler(String[] myArgs) {

        if(myArgs.length != 3)
            return "Error. Use getCardHistory projectName cardName";

        if(!logIn_effettuato)
            return "Error. Login before do this";    
        
        String projectName = myArgs[1];
        String cardName = myArgs[2];
        String reply;
        Project project;

        synchronized(projects){         //gestisco la concorrenza nell'accesso ai vari progetti
            project = projects.get(projectName);
        }

        try{
            project.equals(null);
        }catch(NullPointerException e){
            return "Error. Project "+projectName+" not found";
        }

        synchronized(project){        //gestisco la concorrenza all'interno del singolo progetto

            if(!(project.hasMember(this.user.getUsername())))
                return "Error. User "+this.user.getUsername() +" isn't a member of the project.";
            
            Card card;
            card = project.getCard(cardName.toLowerCase());
            try{
                card.equals(null);
            }catch(NullPointerException e){
                return "Error. Card "+cardName+" not found";
            }
            reply = cardName+ " history: ";
            for (String s : card.getHistory())
                reply = reply + s + ", ";    

        }
        
        return reply;
    }



    //moveCard(projectName, cardName, listSrc, listDest)
    private String moveCardHandler(String[] myArgs) {
        
        if(myArgs.length != 5)
            return "Error. Use moveCard projectName cardName srcList destList";
        
        if(!logIn_effettuato)
            return "Error. Login before do this";

        String projectName = myArgs[1];
        String cardName = myArgs[2];
        String srcList = myArgs[3];
        String destList = myArgs[4];
        Project project;

        synchronized(projects){     //gestisco la concorrenza nell'accesso ai vari progetti
            project = projects.get(projectName);
        }

        try{
            project.equals(null); 
        }catch(NullPointerException e) {
            return "Error. Project" + projectName +" not found";
        }

        synchronized(project){        //gestisco la concorrenza all'interno del singolo progetto
            
            if(!(project.hasMember(this.user.getUsername())))
                return "Error. You are not a member of the project.";
            try{
                Card card = project.moveCard(cardName, srcList.toLowerCase(), destList.toLowerCase());
                String msg = "User "+this.user.getUsername() + " moved card " + cardName +" from " + srcList + " to " + destList;
                project.sendMessage(msg);
                ServerMainClass.saveFile(project.getName() + "/"+card.getName()+".json", card, Card.class);
            }catch(NullPointerException e){
                return "Error. Card " + cardName + " not found";
            }catch(Exception e){
                return "Error. "+e.getMessage();
            }
        }

        return "Card moved from "+srcList +" to "+destList;
    }

    // showCard(projectName, cardName)
    private String showCardHandler(String[] myArgs) {
        
        if(myArgs.length != 3)
            return "Error. Use showCard projectName cardName";

        if(!logIn_effettuato)
            return "Error. Login before do this";
        
        String projectName = myArgs[1];
        String cardName = myArgs[2];
        String reply = "Result for card "+cardName+": ";
        Project project;

        synchronized(projects){     //gestisco la concorrenza nell'accesso ai vari progetti
            project = projects.get(projectName);
        }

        try{
            project.equals(null);
        }catch(NullPointerException e){
            return "Error. Project "+ projectName +" not found.";
        }

        synchronized(project){    //gestisco la concorrenza all'interno del singolo progetto
            
            if(!(project.hasMember(this.user.getUsername())))
                return "Error. You are not a member of the project.";
            Card card = project.getCard(cardName);   
            reply = reply + "\n\t"+card.getName() + " | " + card.getListName() +" | "+ card.getDescription();
        }
        return reply;
    }

    //addCard(projectName, cardName, description)
    private String addCardHandler(String[] myArgs) {
        
        if(myArgs.length != 4)
            return "Error. Use: addCard projectName cardName description";

        if(!logIn_effettuato)
            return "Error. Login before do this";

        String projectName = myArgs[1];
        String cardName = myArgs[2];
        String description = myArgs[3];

        String reply;
        Project project;
        
        synchronized(projects){                 //gestisco la concorrenza nell'accesso ai vari progetti
            project = projects.get(projectName);      
        }

        try{
            project.equals(null);
        }catch(NullPointerException e){
            return "Error. Project "+projectName +" not found";
        }

        synchronized(project){        //gestisco la concorrenza all'interno del singolo progetto

            if(!(project.hasMember(this.user.getUsername())))
                return "Error. You are not a member of the project.";
            
            Card card = project.addCard(cardName, description);
            try{
                card.equals(null);
            }catch(NullPointerException e){
                return "Error. Card "+cardName +" already exists";
            }
            reply = "Card "+ cardName+ " is now in the project";
            ServerMainClass.saveFile(project.getName() + "/"+cardName+".json", card, Card.class);
        }

        return reply;
    }

    //showCards(projectName)
    private String showCardsHandler(String[] myArgs){

        if(myArgs.length != 2)
            return "Error. Use: showCards projectName";

        if(!logIn_effettuato)
            return "Error. Login before do this";

        String projectName = myArgs[1];
        String reply;
        Project project;
        synchronized(projects){         //gestisco la concorrenza nell'accesso ai vari progetti
            project = projects.get(projectName);
        }

        try{
            project.equals(null);
        }catch(NullPointerException e){
            return "Error. Projet "+projectName+" not found";
        }

        synchronized(project){    //gestisco la concorrenza all'interno del singolo progetto

            if(!project.hasMember(this.user.getUsername()))
                reply = "Error. You are not a member of the project.";
            else{            
                reply = "Project "+projectName+"'s cards:";
                reply = reply +"\n" + project.getCards();
            }
        }
        return reply;
    }


    //addMember(projectName, userName)
    private String addMemberHandler(String[] myArgs) {
        
        if(myArgs.length != 3)
        return "Error. Use: addMember projectName newMember";
        
        if(!logIn_effettuato)
            return "Error. Login before do this";
        
        String reply = null;
        String projectName = myArgs[1];
        String name = myArgs[2];

        User u;
        Project project;

        synchronized(users){    //gestisco la concorrenza nell'accesso al 'database' degli utenti
            try{
                u = users.getUser(name);
                u.equals(null);       
            }catch(NullPointerException e){
                return "Error. User "+name+" not found";
            }
        }

        synchronized(projects){         //gestisco la concorrenza nell'accesso ai vari progetti
            try{
                project = projects.get(projectName);
                project.equals(null);
            }catch(NullPointerException e){
                return "Error. Project "+projectName +" not found";
            }
        }

        synchronized(project){    //gestisco la concorrenza all'interno del singolo progetto
            
            if(!(project.hasMember(this.user.getUsername())))
                reply = "Error. You are not a member of the project.";
            else if(project.addMember(u)){
                ServerMainClass.saveFile(project.getName() + "/members.json", project.getUsers(), UsersDB.class);
                reply = "User "+ u.getUsername() +" is now member of the project";
            }
            else
                reply = "Error. User "+name+" is already member"; 
        }

        return reply;
    }

    //showMembers(projectName)
    private String showMembersHandler(String[] myArgs) {

        if(myArgs.length != 2)
            return "Error. Use: createProject projectName";
        
        if(!logIn_effettuato)
            return "Error. Login before do this";

        String projectName = myArgs[1];
        String reply = projectName + " members:";
        Project project;

        synchronized(projects){             //gestisco la concorrenza nell'accesso ai vari progetti
            project = projects.get(projectName);
        }

        try{
            project.equals(null);
        }catch(NullPointerException e){
            return "Error. Project "+ projectName +" not found";
        }

        synchronized(project){
            if(!(project.hasMember(this.user.getUsername())))
                return "Error. You are not a member of the project.";
    
            for (User u : project.getMembers())
                reply = reply + "\n\t"+ u.getUsername();
        }
        return reply;
    }


    //listProjects()
    private String listProjectsHandler(String[] myArgs) {

        if (myArgs.length != 1)
            return "Error. Use: listProjects";

        if(!logIn_effettuato)
            return "Error. Login before do this";        

        String username = user.getUsername();
        String reply = "User "+username+" is member of: ";

        synchronized(users){    //gestisco la concorrenza nell'accesso al 'database' degli utenti
            
            if(!(users.containsUser(username))) //se l'utente non esiste
                reply = "Error. User not found";
            else if(!(username.equals(user.getUsername()))) //se si vuole visualizzare la lista di un altro utente
                reply = "Error. You don't have the permission to see " + username +"'s projects";
            else{
                synchronized(projects){         //gestisco la concorrenza nell'accesso ai vari progetti
                    for (Project project : projects.values()) {
                        synchronized(project){        //gestisco la concorrenza all'interno del singolo progetto
                            if(project.getUsers().containsUser(username))
                                reply = reply + "\n\t" + project.getName();
                        }
                    }
                }
            }
        }
        return reply;
    }


    //cancelProject(projectName)
    private String cancelProjectHandler(String[] myArgs) {
    
        if(myArgs.length != 2)
            return "Error. Use: cancelProject projectName";
    
        if(logIn_effettuato == false)
            return "Error. Login before do this";
    
        String reply = null;
        String projectName = myArgs[1];

        Project project;
        
        
        synchronized(projects){             //gestisco la concorrenza nell'accesso ai vari progetti
            try{
                project = projects.get(projectName);
                project.equals(null);
            }catch(NullPointerException e){
                return "Error. Project "+ projectName +" not found";
            }
            synchronized(project){          //gestisco la concorrenza all'interno del singolo progetto

                if(!(project.hasMember(this.user.getUsername())))     //se l'utente non e' membro del progetto non puo' eliminarlo
                    return "Error. You are not a member of the project.";
                if(!project.canDelete())
                    return "Error. Before deleting the project, all cards must be done";
                project.sendMessage("close");
            }
    
            projects.remove(projectName);
            File directory = new File(ServerMainClass.RECOVERY_FILE_PATH+projectName);
    
            for (File file : directory.listFiles()) 
                file.delete();
            directory.delete();
            reply = "Project "+projectName +" removed";
        }
        return reply;
    }

    
    //createProject(projectName)
    private String createProjectHandler(String[] myArgs) {

        if (myArgs.length != 2)
            return "Error. Use: createProject projectName";
        
        if(!logIn_effettuato)
            return "Error. Login before do this";

        String projectName = myArgs[1];
        String reply = null;
        
        
        synchronized (projects) {

            try{
                projects.get(projectName).equals(null);     //se esiste un progetto con lo stesso nome
                return "Error. Project "+projectName+" already exists";
            }catch(Exception e){ }

            Project project = new Project(projectName, user);
            project.setIP(ServerMainClass.generateIP());
            project.setPort(ServerMainClass.getPort());
            projects.put(projectName, project);    //aggiorno la struttura dati

            try {
                Files.createDirectories(Paths.get(ServerMainClass.RECOVERY_FILE_PATH + projectName + "/"));
                ServerMainClass.saveFile(project.getName() + "/members.json", project.getUsers(), UsersDB.class);
                reply = "New project " + projectName +" created";
                
            } catch (IOException e) {
                reply = "Error. Cannot save project";
            }
        }
    
        return reply;
    }


    //login(username, password)
    private String loginHandler(String[] myArgs) {

        if(myArgs.length != 3)
            return "Error. Use: login username password";
        
        if (logIn_effettuato == true)        //se ci si prova a collegare contemporaneamente da due account
            return "Error. User " + user.getUsername() + " currently logged in";
        
        String name = myArgs[1];
        String pass = myArgs[2];
        String reply = null;

        synchronized(users){     //gestisco la concorrenza nell'accesso al 'database' degli utenti
            
            User currentUser = users.getUser(name);
    
            try {
                currentUser.equals(null);         // se l'utente non esiste
            } catch (Exception e) {
                return "Error. User not found";
            }
    
            if (!(currentUser.getPassword().equals(pass)))        // se la password non matcha
                reply = "Error. Invalid password.";
            else if (currentUser.getStatus().equals("Online"))        // se l'utente ha gia' effettuato l'accesso
                reply = "Error. " + currentUser.getUsername() + " already logged in";
            else {
                reply = "User " + name + " logged in";
                this.user = currentUser;
                this.user.setOnline();
                logIn_effettuato = true;
                try {
                    notificationService.update(this.users);            //notifico la modifica
                } catch (RemoteException e) {
                    System.out.println("System_error: cannot do callback");
                }
            }
        }
        return reply;
    }
    

    //logout(username)
    private String logoutHandler(String[] myArgs) {

        if(myArgs.length != 2)
            return "Error. Use: logout username";
    
        if(!logIn_effettuato)        //se l'utente vuole eseguire il logout prima di eseguire il login
            return "Error. Login before you can logout";
        
            String name = myArgs[1];
        String reply = null;

        synchronized(users){         //gestisco la concorrenza nell'accesso al 'database' degli utenti
            
            User currentUser = users.getUser(name);
    
            try {
                currentUser.equals(null);        // se l'utente non esiste
            } catch (Exception e) {
                return "Error. User not found";
            }
    
            if (!(user.getUsername().equals(name)))        //se un utente vuole disconnettere altri utenti
                reply = "Error. You don't have the permission to logout other users";
            else {
                user.setOffline();
                reply = name + " logged out";
                logIn_effettuato = false;
                
                try {
                    notificationService.update(this.users);            //notifico la modifica
                } catch (RemoteException e) {
                    System.out.println("System_error: cannot do callback");
                }
            }
        }

        return reply;
    }


    //invalid option
    private String invalidOptionHandler() {
        String reply = "Select operation:";
    
        reply = reply + "\n\t\"register username password\" -to register a user";
        reply = reply + "\n\t\"login username password\" -to login";
        reply = reply + "\n\t\"logout username\" -to logout";
        reply = reply + "\n\t\"listUsers\" -to show all registered users";
        reply = reply + "\n\t\"listOnlineUsers\" -to show online users";
        reply = reply + "\n\t\"createProject projectName\" -to create a new project";
        reply = reply + "\n\t\"cancelProject projectName\" -to remove an existing project";
        reply = reply + "\n\t\"listProjects\" -to show all projects of a user";
        reply = reply + "\n\t\"addMember projectName username\" -add a user to a project";
        reply = reply + "\n\t\"showMembers projectName\" -to show all users in a project";
        reply = reply + "\n\t\"addCard projectName cardName description\"- to add a new card to a project";
        reply = reply + "\n\t\"showCards projectName\"- to show all cards of a project";
        reply = reply + "\n\t\"showCard projectName cardName\"- get a card of a project";
        reply = reply + "\n\t\"moveCard projectName cardName startList destinationList\"- to move a card from a list to another";
        reply = reply + "\n\t\"getCardHistory projectName cardName\"- to show all the past states of a card";
        reply = reply + "\n\t\"joinChat projectName\"- to join the project's chat";
        reply = reply + "\n\t\"sendChat projectName message\"- to send a message to the project's chat";
        reply = reply + "\n\t\"readChat projectName\"- to read all message in the project's chat";
        return reply;
    }
}
