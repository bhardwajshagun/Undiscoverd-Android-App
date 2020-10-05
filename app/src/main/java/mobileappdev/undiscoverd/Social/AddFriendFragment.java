package mobileappdev.undiscoverd.Social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import mobileappdev.undiscoverd.R;

public class AddFriendFragment extends Fragment {
    private FragmentManager mFragmentManager;

    private RecyclerView mResults;

    private FirebaseAuth firebaseAuth;

    private FirebaseRecyclerAdapter adapter;
    private DatabaseReference rootDB;

    private String mUserID;
    private String mFriend;
    private Button searchButton;
    private EditText usernameToFind;

    // default constructor
    public AddFriendFragment(){
    }

    public static AddFriendFragment newFragment(){
        System.out.println("AddFriendFragment constructor");
        return new AddFriendFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("AddFriendFragment - onCreateView");
        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);

        //Fragment previousFrag = getFragmentManager().findFragmentByTag("FRIEND_FRAGMENT");
        //previousFrag.isHidden();

        firebaseAuth = FirebaseAuth.getInstance();
        mUserID = firebaseAuth.getCurrentUser().getUid();

        // RecyclerView that will display results when we search for a user to add as a friend
        mResults.setHasFixedSize(true);
        mResults.setLayoutManager(new LinearLayoutManager(getContext()));

        searchButton = view.findViewById(R.id.button_search_for_friend);
        usernameToFind = view.findViewById(R.id.add_friend_edit_text);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForFriend();
            }
        });

        return view;
    }


    /**
     * Searches the Firebase database for a User object related to the username that was searched.
     */
    private void searchForFriend(){
        System.out.println("AddFriendFragment - searchForFriend");

        String user = usernameToFind.getText().toString();
        System.out.println("user is...." + user);


        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        System.out.println(root.toString());
        Query query = root.child("users").orderByChild("username").equalTo(user);

        // now add an EventListener to determine if that user was found or not
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("AddFriendFragment - query AddValueEvent onDataChange");
                // if there is a user found with that username, display it in the RecyclerView
                if(dataSnapshot.exists()){
                    // found a user with that username!
                    System.out.println("FOUND USERNAME!");
                    Toast.makeText(getContext(), "User found!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "No users found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("AddFriendFragment - query AddValueEvent onCancelled");
            }
        });


    }



}
