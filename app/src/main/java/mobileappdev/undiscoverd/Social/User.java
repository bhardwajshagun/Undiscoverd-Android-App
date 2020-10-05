package mobileappdev.undiscoverd.Social;

/**
 * Class represents one user's profile on Undiscoverd. Primary Key on our DB table.
 */
public class User {

    private String username;
    private String firstName;
    private String lastName;
    private String emailAddress;

    public User(String username, String firstName, String lastName, String emailAddress){
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
    }

    public String getUsername(){
        return username;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getEmailAddress() { return emailAddress; }

}
