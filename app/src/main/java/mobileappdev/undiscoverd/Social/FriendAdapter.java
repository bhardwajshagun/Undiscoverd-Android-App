package mobileappdev.undiscoverd.Social;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mobileappdev.undiscoverd.R;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> implements
        View.OnClickListener {

    private List<User> mFriends;
    private Context mContext;

    FriendAdapter(List<User> friends, Context context){
        this.mFriends = friends;
        this.mContext = context;
    }

    // inflate the layout for one friend item in the Recycler View
    @Override
    public FriendAdapter.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        System.out.println("Friend Adapter - onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item, parent, false);

        return new FriendViewHolder(view);
    }

    // bind the data to the Text View in the corresponding friend item in the list
    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.FriendViewHolder holder, int position) {
        System.out.println("Friend Adapter - onBindViewHolder");
        String name = mFriends.get(position).getFirstName() + mFriends.get(position).getLastName();
        String username = mFriends.get(position).getUsername();
        holder.friendName.setText(name);
        holder.friendUsername.setText(username);

        holder.myFriends.setOnClickListener(v -> {
            Intent intent = FriendProfileActivity.launchFriendProfile(mContext, mFriends.get(position).getFirstName(), mFriends.get(position).getLastName());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    @Override
    public void onClick(View v) {
        System.out.println("testing onCLick");
    }

    /**
     * Custom ViewHolder class that handles views for our RecyclerView.
     */
    public static class FriendViewHolder extends RecyclerView.ViewHolder{

        TextView friendName;
        TextView friendUsername;
        LinearLayout myFriends;

        public FriendViewHolder(View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.recycler_view_one_friend_name);
            friendUsername = itemView.findViewById(R.id.recycler_view_one_friend_username);
            myFriends = itemView.findViewById(R.id.friend_item_root);
        }

       // public void setName(String firstName, String lastName){
        //    System.out.println("FriendsViewHolder - setName");
        //    TextView userFirstName = view.findViewById(R.id.recycler_view_one_friend_name);
        //    userFirstName.setText(firstName + " " + lastName);
       // }

       // public void setUsername(String username){
       //     System.out.println("FriendsViewHolder - setUsername");
            //TextView userUsername = view.findViewById(R.id.recycler_view_one_friend_username);
            //userUsername.setText(username);
       // }
    }
}
