package com.joinalongapp.joinalong;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import com.joinalongapp.joinalong.databinding.ActivityMainBinding;
import com.joinalongapp.navbar.EventsFragment;
import com.joinalongapp.navbar.FriendsFragment;
import com.joinalongapp.navbar.HomeFragment;
import com.joinalongapp.navbar.MessagingFragment;
import com.joinalongapp.navbar.ProfileFragment;
import com.joinalongapp.viewmodel.User;

import java.util.UUID;

public class MainActivity extends AppCompatActivity{
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        User u = new User(UUID.randomUUID(), "Ken");
        Bundle bundle = new Bundle();
        bundle.putSerializable("USER", u);


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.message:
                    replaceFragment(new MessagingFragment());
                    break;
                case R.id.event:
                    replaceFragment(new EventsFragment());
                    break;
                case R.id.friends:
                    replaceFragment(new FriendsFragment());
                    break;
                case R.id.profile:
                    ProfileFragment pf = new ProfileFragment();
                    pf.setArguments(bundle);
                    replaceFragment(pf);
                    break;
            }

            return true;
        });


    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}