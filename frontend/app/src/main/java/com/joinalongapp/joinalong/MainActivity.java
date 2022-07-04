package com.joinalongapp.joinalong;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.joinalongapp.joinalong.databinding.ActivityNavigationBinding;
import com.joinalongapp.joinalong.ui.events.EventsFragment;
import com.joinalongapp.joinalong.ui.home.HomeFragment;
import com.joinalongapp.joinalong.ui.managefriends.FriendsFragment;
import com.joinalongapp.joinalong.ui.messaging.MessagingFragment;
import com.joinalongapp.joinalong.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity{

    private ActivityNavigationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.navigation_events:
                    replaceFragment(new EventsFragment());
                    break;
                case R.id.navigation_messaging:
                    replaceFragment(new MessagingFragment());
                    break;
                case R.id.navigation_friends:
                    replaceFragment(new FriendsFragment());
                    break;
                case R.id.navigation_profile:
                    replaceFragment(new ProfileFragment());
                    break;
            }
            return true;
        });

//        BottomNavigationView navView = findViewById(R.id.nav_view);
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_messaging, R.id.navigation_events, R.id.navigation_friends, R.id.navigation_profile)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_navigation);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(binding.navView, navController);
//        setSupportActionBar(new Toolbar(this));
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }


}