package mobileappdev.undiscoverd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;


import mobileappdev.undiscoverd.Badges.BadgesFragment;
import mobileappdev.undiscoverd.Camera.CameraFragment;

import mobileappdev.undiscoverd.Photos.PhotoFragment;

import mobileappdev.undiscoverd.Social.FriendFragment;

import mobileappdev.undiscoverd.maps.MapsFragment;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * Activity where the user will first enter when the app launches. DEFAULT WITH MAP SHOWING???
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String KEY_SELECTED_NAV_MENU = "KEY_SELECTED_NAV_MENU";
    private final int REQUEST_LOCATION_PERMISSION_CODE = 9001;

    private DrawerLayout mDrawer;
    private int mPrevMenuItem;
    private SharedPreferences mSharedPreferences;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Toolbar toolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Fragment fragment; // this is our initial fragment SHOULD BE MAP AT THE END
        FragmentManager fragmentManager = getSupportFragmentManager();

        // if the app was running already, we need to check to see what fragment was up for the user
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SELECTED_NAV_MENU)) {
            mPrevMenuItem = savedInstanceState.getInt(KEY_SELECTED_NAV_MENU);
            fragment = getFragmentFromMenuSelection(mPrevMenuItem);
            displayFragment(mPrevMenuItem);
        } else { // otherwise this is first time  app is being opened so display default fragment
            fragment = MainFragment.newFragment();
            displayFragment(R.id.menu_main);
            mPrevMenuItem = R.id.menu_main;
        }

        fragmentManager.beginTransaction().replace(R.id.menu_main_layout, fragment).commit();

        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                mDrawer,
                toolbar,
                R.string.open_navigation_drawer,
                R.string.close_navigation_drawer);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // Request location permission to use Google Maps API
        EasyPermissions.requestPermissions(this, "Please grant the location permission",
                REQUEST_LOCATION_PERMISSION_CODE, Manifest.permission.ACCESS_FINE_LOCATION);


        // when this is done, we will pass the user's email as the string so it can link
        // and grab all the necessary information depending on what window we are on.
        // and the image might be their profile picture or something like that
        String test = "";
        setNavigationView(test, null);
    }

    /**
     * Display the fragment determined by the id passed to this function.
     *
     * @param id of the menu option that corresponds to the fragment we want.
     */
    void displayFragment(int id) {
        mNavigationView = findViewById(R.id.menu_navigation_view);
        Menu menu = mNavigationView.getMenu();
        MenuItem menuItem = menu.findItem(id); // set the nav view to whichever fragment corresponds
        System.out.println(menuItem);
        menuItem.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Populate the NavigationView so the menu is ready when the user clicks on a menu option.
     */
    private void setNavigationView(String id, String imageURL) {
        System.out.println("MainActivity - setNavigationView");
        NavigationView navigationView = findViewById(R.id.menu_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeader = navigationView.getHeaderView(0);
        TextView emailTextView = navigationHeader.findViewById(R.id.main_menu);
        emailTextView.setText(id);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        System.out.println("MainActivity = onNavigationItemSelected");
        // first check if the user is already on the option that they clicked
        // in which case, just close the Navigation Drawer
        if (menuItem.getItemId() == mPrevMenuItem) {
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        int id = menuItem.getItemId();
        mPrevMenuItem = menuItem.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();

        // create the correct fragment based on what the user selected from the menu
        Fragment fragment = getFragmentFromMenuSelection(id);

        if (fragment != null) {
            fragmentManager.beginTransaction().replace(R.id.menu_main_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        // and finally close the Navigation Drawer
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Displays the correct Fragment based on which menu item the user clicked on.
     */
    private Fragment getFragmentFromMenuSelection(int id) {
        Fragment fragment = null;

        switch (id) {
            case R.id.menu_main:
                fragment = MainFragment.newFragment();
                break;

            case R.id.menu_map:
                // Open Maps if user has granted location permission
                if (hasLocationPermission()) {
                    fragment = MapsFragment.newFragment();
                } else {
                    Toast.makeText(this, "Map requires location permission", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.menu_my_photos:
                fragment = PhotoFragment.newFragment();
                break;

            case R.id.menu_my_friends:
                fragment = FriendFragment.newFragment();
                break;

            case R.id.menu_my_badges:
                fragment = BadgesFragment.newFragment();
                break;

            case R.id.menu_camera:
                fragment = CameraFragment.newFragment();
                break;
        }
        return fragment;
    }

    private Boolean hasLocationPermission() {
        // Check if user allowed location permission
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

}
