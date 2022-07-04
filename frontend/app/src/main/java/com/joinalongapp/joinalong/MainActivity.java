package com.joinalongapp.joinalong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.User;

import java.io.Serializable;
import java.sql.Array;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Location l = new Location("");

        Date a = new Date();
        Event u = new Event(UUID.randomUUID(), UUID.randomUUID(), "sample_title", l, a, a, true, 5, "sample_desc");





        Button b = (Button) findViewById(R.id.testButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ReportActivity.class);
                User n = new User(UUID.randomUUID(), "Ken");
                //i.putExtra("EDIT_OPTION", true);
                i.putExtra("REPORTING_PERSON", n);
                startActivity(i);
            }
        });
    }

}