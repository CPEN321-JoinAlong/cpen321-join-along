package com.joinalongapp.joinalong;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.joinalongapp.joinalong.databinding.ActivityMainBinding;
import com.joinalongapp.navbar.FriendsFragment;
import com.joinalongapp.navbar.HomeFragment;
import com.joinalongapp.navbar.MessagingFragment;
import com.joinalongapp.navbar.ProfileFragment;

public class MainActivity extends AppCompatActivity{
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        SharedPreferences sharedPreferences = this.getSharedPreferences("darkModePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean darkMode = sharedPreferences.getBoolean("dark_mode_toggle", false);
        boolean changedMode = sharedPreferences.getBoolean("changed", false);

        //if(changedMode){
        //    editor.putBoolean("changed", false);
        //    replaceFragment(new ProfileFragment());
        //}

        //if(darkMode){
        //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        //}
       // else{
        //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        //}


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


    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}