package mobileappdev.undiscoverd.Social;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mobileappdev.undiscoverd.R;

/**
 * This class represents the fragment that is used when the user is looking at their friends list on
 * the Undiscoverd app.
 */
public class FriendFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private List<User> mFriendsList;
    private List<String> mFriendsIDs;
    private FriendAdapter friendAdapter;

    private FirebaseAuth firebaseAuth;

    private String mUserID;

    // default constructor
    public FriendFragment(){
    }

    public static FriendFragment newFragment(){
        return new FriendFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        //getActivity().findViewById(R.id.main_activity_toolbar).setVisibility(View.INVISIBLE);

        mRecyclerView = view.findViewById(R.id.friends_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mFriendsList = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        mUserID = firebaseAuth.getCurrentUser().getUid();

        // as we set up the Maps, get all of the user's previous trips to add to Map later
        mFriendsList = new ArrayList<>();
        mFriendsIDs = new ArrayList<>();

        // set our Floating Action Button that user clicks to add a new friend
        FloatingActionButton fab = view.findViewById(R.id.fab_add_friend);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findFriend();
            }
        });

        // get the current user's friends and populate the RecyclerView
        getFriendsIDs();

        return view;
    }

    /**
     * This method gets a list of the IDs of the current user's friends.
     */
    private void getFriendsIDs(){
         final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("friends").child(mUserID);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // look through each of the User's friends and save them
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    System.out.println(ds.toString());
                    System.out.println(ds.getValue());
                    if(Objects.equals(ds.getValue(), true)){
                        mFriendsIDs.add(ds.getKey());
                    }
                }
            // once done querying the user ID's of the user's friends, need to each actual User info
                getFriendsInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This method takes the list of IDs and retrieves the actual friend's information.
     */
    private void getFriendsInfo() {

        // so for each ID in the mFriendsIDs, I need to make a call to the database and get
        // their information using the ID
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
         Query query = ref.child("users");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    for (String id : mFriendsIDs) {

                        if(ds.getKey().equals(id)){

                            String username = ds.child("username").getValue(String.class);
                            String firstName = ds.child("firstName").getValue(String.class);
                            String lastName = ds.child("lastName").getValue(String.class);
                            String emailAddress = ds.child("emailAddress").getValue(String.class);

                            mFriendsList.add(new User(username, firstName, lastName, emailAddress));
                        }
                    }
                }
                // now populate the RecyclerView to display all of our friends
                friendAdapter = new FriendAdapter(mFriendsList, getContext());
                mRecyclerView.setAdapter(friendAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * When the user wants to add a new friend, this opens a new fragment where they can search
     * for a username of a friend they want to add.
     */
    private void findFriend(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        final View view = getLayoutInflater().inflate(R.layout.fragment_add_friend, null);
        builder.setView(view);

        Button searchForUser = view.findViewById(R.id.button_search_for_friend);
        EditText userInput = view.findViewById(R.id.add_friend_edit_text);
        TextView username = view.findViewById(R.id.add_friend_text_view);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        searchForUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // now we search the database for a user with that username
                if(searchForUsername(userInput.getText().toString())){
                    // there exists a valid user with that username
                    username.setText(userInput.getText().toString());
                    username.setVisibility(View.VISIBLE);
                    // and finally add them as a friend if the user clicks Add
                }
            }
        });

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(username.getVisibility() == View.VISIBLE)
                addFriend(username.getText().toString());
            }
        });
        builder.show();
    }

    /**
     * Searches the Firebase database for a User with a specific username.
     */
    private boolean searchForUsername(String username){

        final int[] validUser = {1};

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        Query query = root.child("users").orderByChild("username").equalTo(username);

        // now add an EventListener to determine if that user was found or not
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    // found a user with that username, so now add them as a friend
                    Toast.makeText(getContext(), "User found!", Toast.LENGTH_LONG).show();
                    validUser[0] = 1;

                } else {
                    Toast.makeText(getContext(), "No users found", Toast.LENGTH_LONG).show();
                    validUser[0] = 0;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return validUser[0] == 1; // return true if a user with that username was found
    }

    /**
     * Adds two users as friends in the Firebase database.
     */
    private void addFriend(String username){

        // so we know that the desired user is an existing user
        mUserID = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        Query friend = root.child("users").orderByChild("username").equalTo(username);
        friend.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    //Log.d("test0", ds.getKey().toString());
                    becomeFriends(mUserID, ds.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Adds friendship between two User objects to the Firebase database.
     */
    private void becomeFriends(String myUserID, String friendID){

        // set the other User as my friend
        DatabaseReference myDB = FirebaseDatabase.getInstance().getReference().child("friends")
                .child(myUserID);
        myDB.child(friendID).setValue(true);

        // and set myself as a friend to the other User
        DatabaseReference friendDB = FirebaseDatabase.getInstance().getReference().child("friends")
                .child(friendID);
        friendDB.child(myUserID).setValue(true);
    }
}
