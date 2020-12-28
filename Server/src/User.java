import java.io.Serializable;

public class User implements Serializable {
    //TODO- posso togliere il Serializable?

    private static final long serialVersionUID = 1L;    //TODO- vedere che vuol dire
    private String username;
    //private String password;

    public User(String u){
        username = u;
    }

    public String getUsername(){
        return username;
    }
}
