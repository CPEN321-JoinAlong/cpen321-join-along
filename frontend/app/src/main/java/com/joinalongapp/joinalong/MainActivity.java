package com.joinalongapp.joinalong;


import android.content.Intent;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.joinalongapp.joinalong.databinding.ActivityMainBinding;
import com.joinalongapp.navbar.FriendsFragment;
import com.joinalongapp.navbar.HomeFragment;
import com.joinalongapp.navbar.MessagingFragment;
import com.joinalongapp.navbar.ProfileFragment;

public class MainActivity extends AppCompatActivity{
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Trace myTrace = FirebasePerformance.getInstance().newTrace("MainActivityUIComponents");
        myTrace.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());




        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.message:
                    replaceFragment(new MessagingFragment());
                    break;
                case R.id.event:
                    Intent i = new Intent(MainActivity.this, ManageEventActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.bottom_slide_down, R.anim.no_animation);
                    finish();
                    break;
                case R.id.friends:
                    replaceFragment(new FriendsFragment());
                    break;
                case R.id.profile:
                    replaceFragment(new ProfileFragment());
                    break;
                default:
                    throw new IllegalStateException("Unknown tab");
            }

            return true;
        });
        myTrace.stop();

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}