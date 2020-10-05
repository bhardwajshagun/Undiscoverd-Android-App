package mobileappdev.undiscoverd.Badges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import mobileappdev.undiscoverd.R;
import mobileappdev.undiscoverd.maps.MapsLocation;

public class BadgesFragment extends Fragment {

    private FragmentManager mFragmentManager;

    List<String> imageTitles; // list of image titles
    List<Integer> images; // list of images
    Adapter adapter; // adapter

    private RecyclerView imagesList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseDatabase;
    private String mUserID;

    private List<MapsLocation> mPreviousTrips;

    // default constructor
    public BadgesFragment(){
    }

    public static BadgesFragment newFragment(){
        return new BadgesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_badges, container, false);
        imagesList = (RecyclerView) view.findViewById(R.id.imagesList);

        firebaseAuth = FirebaseAuth.getInstance();
        mUserID = firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        // as we set up the Maps, get all of the user's previous trips to add to Map later
        mPreviousTrips = new ArrayList<>();
        getUserTrips();
        return view;

    }

    private void getUserTrips() {
        Query query = FirebaseDatabase.getInstance().getReference().child("Trips").child(mUserID);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Trips").child(mUserID);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // now loop through each of the returned Trips and save them to a list of Trips
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    double latitude = (double) ds.child("latitude").getValue();
                    double longitude = (double) ds.child("longitude").getValue();
                    String city = ds.child("city").getValue().toString();
                    String state = ds.child("state").getValue().toString();
                    String country = ds.child("country").getValue().toString();

                    mPreviousTrips.add(new MapsLocation(latitude, longitude, city, state, country));
                }
                addTrips();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addTrips() {

        imageTitles = new ArrayList<>(); // stores the imageTitles in arrayLlist
        images = new ArrayList<>(); // stores the images in arrayList

        imageTitles.add("San Francisco");
        imageTitles.add("Cape Town");
        imageTitles.add("Honolulu");
        imageTitles.add("London");
        imageTitles.add("Winnipeg");
        imageTitles.add("Stanley");
        imageTitles.add("Cairo");
        imageTitles.add("Agra");
        imageTitles.add("Madrid");
        imageTitles.add("Las Vegas");

        images.add(R.drawable.image1);
        images.add(R.drawable.image2);
        images.add(R.drawable.image3);
        images.add(R.drawable.image4);
        images.add(R.drawable.image5);
        images.add(R.drawable.image6);
        images.add(R.drawable.image7);
        images.add(R.drawable.image8);
        images.add(R.drawable.image9);
        images.add(R.drawable.image10);

        Map<String, Integer> locationsMap = new HashMap<String, Integer>() {{
            put("San Francisco", 0);
            put("Cape Town", 1);
            put("Honolulu", 2);
            put("London", 3);
            put("Winnipeg", 4);
            put("Stanley", 5);
            put("Cairo", 6);
            put("Agra", 7);
            put("Madrid", 8);
            put("Las Vegas", 9);
        }};

        List<Integer> visitedBadges = new ArrayList<Integer>();

        for (MapsLocation visitedLocation: mPreviousTrips){
            String city = visitedLocation.getCity();
            if (locationsMap.containsKey(city)) {
                visitedBadges.add(locationsMap.get(city));
            }
        }

        adapter = new Adapter(getActivity().getApplicationContext(), imageTitles, images, visitedBadges);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2,GridLayoutManager.VERTICAL,false);
        imagesList.setLayoutManager(gridLayoutManager);
        imagesList.setAdapter(adapter);

    }

}
