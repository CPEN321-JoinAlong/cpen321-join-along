package com.joinalongapp.joinalong;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.joinalongapp.viewmodel.RideshareDetails;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
    private Button bookRideshare;
    private ImageButton close;
    private String TAG = "ManageRideshareActivity";
    private final String ORANGE = "#F44336";
    private final String LIGHT_ORANGE = "#F89790";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_rideshare);

        initElements();
        initSpinner();

        autofillEventDetails();

        RideshareDetails userInputDetails = new RideshareDetails();

        initPickupDateListener(userInputDetails);
        initPickupTimeListener(userInputDetails);
        initShareCostToggleListener(userInputDetails);

        bookRideshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInputDetails.setTitle(titleEdit.getText().toString());
                userInputDetails.setPickupLocation(pickUpLocationEdit.getText().toString());
                userInputDetails.setDestination(destinationEdit.getText().toString());
                userInputDetails.setNumPeople((Integer) numPeople.getSelectedItem());
                userInputDetails.setDescription(descriptionEdit.getText().toString());

                try {
                    String json = userInputDetails.toJsonString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //TODO: post to backend so that backend can call the rideshare api
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initShareCostToggleListener(RideshareDetails userInputDetails) {
        setShareCostToggleColors(R.color.orange_prim, R.color.orange_light);

        shareCostToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.rideshareShareCost) {
                        userInputDetails.setShareCost(true);
                        setShareCostToggleColors(R.color.orange_prim, R.color.orange_light);
                    } else {
                        if (checkedId == R.id.rideshareDontShareCost) {
                            userInputDetails.setShareCost(false);
                            setShareCostToggleColors(R.color.orange_light, R.color.orange_prim);
                        }
                    }
                } else {
                    // This is the default case, which is to share the cost
                    if (group.getCheckedButtonId() == View.NO_ID) {
                        userInputDetails.setShareCost(true);
                        setShareCostToggleColors(R.color.orange_prim, R.color.orange_light);
                    }
                }
            }
        });
    }

    private void setShareCostToggleColors(int shareColor, int noShareColor) {
        shareCostButton.setBackgroundColor(shareColor);
        noShareCostButton.setBackgroundColor(noShareColor);
    }

    private void initPickupTimeListener(RideshareDetails userInputDetails) {
        pickUpTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(ManageRideshareActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar input = Calendar.getInstance();
                        input.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        input.set(Calendar.MINUTE, minute);

                        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.CANADA);
                        pickUpTimeEdit.setText(sdf.format(input.getTime()));

                        userInputDetails.setPickUpTime(input);
                    }
                }, hour, minute, false);
                timePicker.show();
            }
        });
    }

    private void initPickupDateListener(RideshareDetails userInputDetails) {
        pickUpDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar today = Calendar.getInstance();
                int year = today.get(Calendar.YEAR);
                int month = today.get(Calendar.MONTH);
                int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(ManageRideshareActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar input = Calendar.getInstance();
                        input.set(Calendar.YEAR, year);
                        input.set(Calendar.MONTH, month);
                        input.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.CANADA);
                        pickUpDateEdit.setText(sdf.format(input.getTime()));

                        userInputDetails.setPickUpDate(input);
                    }
                }, year, month, dayOfMonth);
                datePicker.show();
            }
        });
    }

    private void autofillEventDetails() {
        if (getIntent().getExtras() != null) {
            RideshareDetails givenDetails = (RideshareDetails) getIntent().getExtras().getSerializable("rideshareDetails");
            if (givenDetails != null) {
                pickUpLocationEdit.setText(givenDetails.getPickupLocation());
                destinationEdit.setText(givenDetails.getDestination());
            }
        }
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
        bookRideshare = findViewById(R.id.rideshare_estimate);
        close = findViewById(R.id.rideshareManageCloseButton);
    }

    private void initSpinner() {
        // The max number of people in an UberX is 6
        Integer[] items = new Integer[]{1,2,3,4,5,6};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        numPeople.setAdapter(adapter);
    }

}
