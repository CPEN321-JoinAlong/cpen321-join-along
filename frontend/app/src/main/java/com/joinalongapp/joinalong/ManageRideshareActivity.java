package com.joinalongapp.joinalong;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.joinalongapp.viewmodel.RideshareDetails;

//TODO: it might be nice to extract common methods between activities such as address and stuff into another utility like interface
public class ManageRideshareActivity extends AppCompatActivity {
    private EditText titleEdit;
    private EditText pickUpLocationEdit;
    private EditText destinationEdit;
    private EditText pickUpDateEdit;
    private EditText pickUpTimeEdit;
    private Spinner numPeople;
    private MaterialButtonToggleGroup shareCostToggle;
    private Button shareCostButton;
    private Button noShareCostButton;
    private EditText descriptionEdit;
    private Button getEstimate;
    private ImageButton close;
    private String TAG = "ManageRideshareActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_rideshare);

        initElements();
        initSpinner();

        autofillEventDetails();

        RideshareDetails userInputDetails = new RideshareDetails();

        shareCostToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.rideshareShareCost) {
                        userInputDetails.setShareCost(true);
                        shareCostButton.setBackgroundColor(Color.parseColor("#F44336"));
                        noShareCostButton.setBackgroundColor(Color.parseColor("#F89790"));
                    } else {
                        if (checkedId == R.id.rideshareDontShareCost) {
                            userInputDetails.setShareCost(false);
                            shareCostButton.setBackgroundColor(Color.parseColor("#F89790"));
                            noShareCostButton.setBackgroundColor(Color.parseColor("#F44336"));
                        }
                    }
                } else {
                    // This is the default case, which is to share the cost
                    if (group.getCheckedButtonId() == View.NO_ID) {
                        userInputDetails.setShareCost(true);
                        shareCostButton.setBackgroundColor(Color.parseColor("#F44336"));
                        noShareCostButton.setBackgroundColor(Color.parseColor("#F89790"));
                    }
                }
            }
        });

        getEstimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInputDetails.setTitle(titleEdit.getText().toString());
                userInputDetails.setPickupLocation(pickUpLocationEdit.getText().toString());
                userInputDetails.setDestination(destinationEdit.getText().toString());

                //TODO: this needs to be changed, the pickupdateedit and pickuptimeedit might not be in the correct format
                //      tbh this might be useless bc of UBER api limited functionality
//                LocalDateTime date = LocalDateTime.parse(pickUpDateEdit.getText().toString() + pickUpTimeEdit.getText().toString());
//                userInputDetails.setPickUpDate(date);

                userInputDetails.setNumPeople((Integer) numPeople.getSelectedItem());
                userInputDetails.setDescription(descriptionEdit.getText().toString());

                Intent selectRideShare = new Intent(ManageRideshareActivity.this, SelectRideshareActivity.class);
                selectRideShare.putExtra("rideshareDetails", userInputDetails);
                startActivity(selectRideShare);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void autofillEventDetails() {
        RideshareDetails givenDetails = (RideshareDetails) getIntent().getExtras().get("rideshareDetails");
        pickUpLocationEdit.setText(givenDetails.getPickupLocation());
        destinationEdit.setText(givenDetails.getDestination());
    }

    private void initElements() {
        titleEdit = findViewById(R.id.rideshareTitleEdit);
        pickUpLocationEdit = findViewById(R.id.ridesharePickUpLocation);
        destinationEdit = findViewById(R.id.rideshareDestination);
        pickUpDateEdit = findViewById(R.id.ridesharePickupDate);
        pickUpTimeEdit = findViewById(R.id.ridesharePickupTime);
        numPeople = findViewById(R.id.rideshareNumberPeopleSpinner);
        shareCostToggle = findViewById(R.id.rideshareShareCostToggle);
        shareCostButton = findViewById(R.id.rideshareShareCost);
        noShareCostButton = findViewById(R.id.rideshareDontShareCost);
        descriptionEdit = findViewById(R.id.rideshareDescription);
        getEstimate = findViewById(R.id.rideshare_estimate);
        close = findViewById(R.id.rideshareManageCloseButton);
    }

    private void initSpinner() {
        // The max number of people in an UberX is 6
        Integer[] items = new Integer[]{1,2,3,4,5,6};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        numPeople.setAdapter(adapter);
    }

}
