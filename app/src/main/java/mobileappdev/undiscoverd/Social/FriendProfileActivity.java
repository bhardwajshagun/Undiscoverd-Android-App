package mobileappdev.undiscoverd.Social;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import mobileappdev.undiscoverd.R;

public class FriendProfileActivity extends AppCompatActivity {

    private String friendName;
    private String friendHometown;

    // this is how we would implement a list of their cities visited using Recycler View

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("FriendProfileActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        TextView friendName = findViewById(R.id.friend_profile_full_name);
        TextView friendLocation = findViewById(R.id.friend_profile_location);
        ImageView friendProfilePicture = findViewById(R.id.friend_profile_picture);

    }

    /**
     * This will allow us to open this friend's profile activity from the previous fragment.
     */
    public static Intent launchFriendProfile(Context context, String name, String location){
        return new Intent(context, FriendProfileActivity.class);

    }

}
