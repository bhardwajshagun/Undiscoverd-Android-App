package mobileappdev.undiscoverd.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobileappdev.undiscoverd.MainActivity;
import mobileappdev.undiscoverd.R;
import mobileappdev.undiscoverd.Social.User;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener, LoginView {

    // our login related widgets
    EditText loginEmail;
    EditText loginPassword;
    Button login;

    // and our sign-up related widgets
    Button signUp;
    EditText signUpEmail;
    EditText signUpPassword;
    EditText signUpConfirmPassword;
    EditText signUpFirstName;
    EditText signUpLastName;
    EditText signUpUsername;

    FirebaseAuth firebaseAuth;
    DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance(); // Firebase instance for validating credentials
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        loginEmail = findViewById(R.id.user_inputted_email_for_login);
        loginPassword = findViewById(R.id.user_inputted_password_for_login);

        login = findViewById(R.id.login_button);

        signUpEmail = findViewById(R.id.signUp_email);
        signUpPassword = findViewById(R.id.signUp_password);
        signUpConfirmPassword = findViewById(R.id.signUp_confirm_password);
        signUpFirstName = findViewById(R.id.signUp_first_name);
        signUpLastName = findViewById(R.id.signUp_last_name);
        signUpUsername = findViewById(R.id.signUp_username);

        signUp = findViewById(R.id.signUp_button);

        login.setOnClickListener(this);
        signUp.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        System.out.println("LoginActivity - onClick (actual one)");

        switch (view.getId()) {

            case R.id.signUp_button:
                System.out.println("LoginActivity - sign up");
                signUpUser();
                break;

            case R.id.login_button:
                System.out.println("LoginActivity - log in");
                loginUser();
                break;
        }
    }

    /**
     * This is the real login I am working on implementing...more functions below as well
     * Fix all these comments before submitting this!!!
     */
    public void loginUser(){

        //String email = loginEmail.getText().toString();
        //String password = loginPassword.getText().toString();

        String email = "shagun@gmail.com";
        String password = "test123";

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(getApplicationContext(),
                    "Please enter email and password.", Toast.LENGTH_LONG).show();
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // check if the login was successful or not
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Invalid email or password.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    /**
     * Sign-up function for first time users.
     */
    public void signUpUser(){

        String email = signUpEmail.getText().toString();
        String password = signUpPassword.getText().toString();
        String confirmPassword = signUpConfirmPassword.getText().toString();
        String firstName = signUpFirstName.getText().toString();
        String lastName = signUpLastName.getText().toString();
        String username = signUpUsername.getText().toString();

        // test to make sure the password that was confirmed correctly by the user
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(), "Passwords do not match.",
                    Toast.LENGTH_LONG).show();
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // check if the sign up was valid
                            if (task.isSuccessful()) {
                                System.out.println("sign up successful");

                                // before starting main activity, we need to save the user in our DB
                                storeNewUser(email, firstName, lastName, username);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Sign-up failed.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    /**
     * Following a successful user sign up, stores that user and his/her information in the
     * Firebase database.
     * @param email user's email address.
     * @param firstName user's first name.
     * @param lastName user's last name.
     */
    public void storeNewUser(final String email, String firstName, String lastName,
                             String username){
        String userID = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference db = firebaseDatabase;

        User user = new User(username, firstName, lastName, email);
        db.child("users").child(userID).setValue(user);
    }

    /**
     * Checks if what the user inputted is a valid email address.
     * @param email what the user entered
     * @return true if valid email, false if not valid email
     */
    public boolean isValidEmail(String email){
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Checks if the user inputted password fits all the Undiscoverd password criteria.
     * @param password what the user entered
     * @return true if valid password, false if invalid password
     */
    public boolean isValidPassword(String password){

        // not sure how we want to test password, for now just made it easy (longer than 8 chars)

        return password.length() >= 8;
    }

    /**
     * Checks if the user inputted both a first name and a last name.
     * @param firstName what the user entered for first name.
     * @param lastName what the user entered for last name.
     * @return true if neither first or last name is blank, false otherwise.
     */
    public boolean isValidName(String firstName, String lastName){
        return (!firstName.isEmpty() && !lastName.isEmpty());
    }


    @Override
    public void openSignUp() {

    }

    @Override
    public void openLogin() {

    }

    @Override
    public void setLoginEmail() {

    }

    @Override
    public void displayMessage() {

    }


}
