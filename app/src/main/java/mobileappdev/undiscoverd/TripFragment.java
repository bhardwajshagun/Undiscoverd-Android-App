package mobileappdev.undiscoverd;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TripFragment extends Fragment {

    private FragmentManager mFragmentManager;

    // default constructor
    public TripFragment(){
    }

    public static TripFragment newFragment(){
        System.out.println("TripFragment constructor");
        return new TripFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("TripFragment - onCreateView");
        View view = inflater.inflate(R.layout.fragment_trip, container, false);

        return view;

    }
}

