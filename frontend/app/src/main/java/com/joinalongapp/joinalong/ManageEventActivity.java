package com.joinalongapp.joinalong;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.joinalongapp.viewmodel.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ManageEventActivity extends AppCompatActivity {
    private int PRIVATE_VISIBILITY_INDEX = 0;
    private int PUBLIC_VISIBILITY_INDEX = 1;
    private TextView manageEventTitle;
    private EditText title;
    private EditText location;
    private EditText beginningDate;
    private EditText endDate;
    private TabLayout eventVisibilityTab;
    private Spinner numberOfPeople;
    private EditText description;
    private Button submitButton;
    private ImageButton cancelButton;
    private ChipGroup chipGroupTags;
    private AutoCompleteTextView autoCompleteChipTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        initElements();

        String[] numOfPeople = getResources().getStringArray(R.array.number_of_people_array);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, numOfPeople);
        numberOfPeople.setAdapter(arrayAdapter);


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        beginningDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarOperation(beginningDate);
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarOperation(endDate);
            }
        });

        String[] sampleTags = getResources().getStringArray(R.array.sample_tags);
        initAutoCompleteChipGroup(autoCompleteChipTags, chipGroupTags, sampleTags);

        Bundle info = getIntent().getExtras();
        Boolean manageOption = info.getBoolean("EDIT_OPTION");

        if (manageOption) {
            // Must append pre-existing text due to editing of Event.
            Event userEvent = (Event) info.getSerializable("EVENT");
            manageEventTitle.setText("Edit Event");

            title.setText(userEvent.getTitle());
            //location.setText(userEvent.getLocation().toString());
            beginningDate.setText(userEvent.getBeginningDate().toString());
            endDate.setText(userEvent.getEndDate().toString());
            boolean publicVisibility = userEvent.getPublicVisibility();
            if(publicVisibility){
                eventVisibilityTab.setSelectedTabIndicator(PUBLIC_VISIBILITY_INDEX);
            }
            else {
                eventVisibilityTab.setSelectedTabIndicator(PRIVATE_VISIBILITY_INDEX);
            }

            int position = arrayAdapter.getPosition(String.valueOf(userEvent.getNumberOfPeople()));
            numberOfPeople.setSelection(position);
            description.setText(userEvent.getDescription());
        }


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInvalidFields()){
                    // Build req, send to server
                }

            }
        });


    }

    private void initElements(){
        manageEventTitle = findViewById(R.id.eventManagementTitle);
        title = findViewById(R.id.editTextEventManagementTitle);
        location = findViewById(R.id.editTextEventManagementLocation);
        beginningDate = findViewById(R.id.editTextEventManagementBeginningDate);
        endDate = findViewById(R.id.editTextEventManagementEndDate);
        eventVisibilityTab = findViewById(R.id.eventVisibilitySelection);
        numberOfPeople = findViewById(R.id.eventManagementNumberOfPeopleSpinner);
        description = findViewById(R.id.eventManagementEditTextDescription);
        submitButton = findViewById(R.id.submitManageEventButton);
        cancelButton = findViewById(R.id.cancelButton);
        chipGroupTags = findViewById(R.id.eventManagementTagChipGroup);
        autoCompleteChipTags = findViewById(R.id.autoCompleteChipTextView);
    }

    private Boolean checkInvalidFields(){
        Boolean flag = true;

        if(editTextEmpty(title)){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty Title field", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(editTextEmpty(location)){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty Location field", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(editTextEmpty(beginningDate)){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty Beginning Date field", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(editTextEmpty(endDate)){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty End Date field", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(chipGroupTags.getChildCount() == 0){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty Tag field", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(editTextEmpty(description)){
            flag = false;
            Toast toast = Toast.makeText(this, "Empty Description field", Toast.LENGTH_SHORT);
            toast.show();
        }

        return flag;
    }

    private Boolean editTextEmpty(EditText input){
        return input.getText().toString().trim().length() == 0;
    }

    private void calendarOperation(EditText editText){
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(ManageEventActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar input = Calendar.getInstance();
                input.set(Calendar.YEAR, year);
                input.set(Calendar.MONTH, month);
                input.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.CANADA);
                editText.setText(sdf.format(input.getTime()));

            }

        }, year, month, dayOfMonth);
        datePicker.show();
    }

    private void initAutoCompleteChipGroup(AutoCompleteTextView autoCompleteTextView, ChipGroup chipGroup, String[] fillArray){
        ArrayAdapter<String> arrayAdapterTags = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, fillArray);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(arrayAdapterTags);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                autoCompleteTextView.setText("");

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_chip, chipGroup, false);
                chip.setText((String) parent.getItemAtPosition(position));
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chipGroup.removeView(chip);
                    }
                });
                chipGroup.addView(chip);
            }
        });
    }
}