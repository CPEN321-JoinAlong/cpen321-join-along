package com.joinalongapp.joinalong;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.joinalongapp.viewmodel.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ManageEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        TextView manageEventTitle = findViewById(R.id.eventManagementTitle);
        EditText title = findViewById(R.id.editTextEventManagementTitle);
        EditText location = findViewById(R.id.editTextEventManagementLocation);
        EditText beginningDate = findViewById(R.id.editTextEventManagementBeginningDate);
        EditText endDate = findViewById(R.id.editTextEventManagementEndDate);
        TabLayout blockSelectionTab = findViewById(R.id.reportVisibilitySelection);
        Spinner numberOfPeople = findViewById(R.id.eventManagementNumberOfPeopleSpinner);

        EditText description = findViewById(R.id.eventManagementEditTextDescription);

        String[] numOfPeople = getResources().getStringArray(R.array.number_of_people_array);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, numOfPeople);
        numberOfPeople.setAdapter(arrayAdapter);

        ImageButton cancelButton = findViewById(R.id.cancelButton);
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






        Bundle info = getIntent().getExtras();
        Boolean manageOption = info.getBoolean("EDIT_OPTION");

        if (manageOption) {
            Event userEvent = (Event) info.getSerializable("EVENT");
            manageEventTitle.setText("Edit Event");

            title.setText(userEvent.getTitle());
            //location.setText(userEvent.getLocation().toString());
            beginningDate.setText(userEvent.getBeginningDate().toString());
            endDate.setText(userEvent.getEndDate().toString());
            int position = arrayAdapter.getPosition(String.valueOf(userEvent.getNumberOfPeople()));
            numberOfPeople.setSelection(position);
            description.setText(userEvent.getDescription());

        }
        else{

        }
    }

    private void createChatOption(){

    }

    private void editChatOption(){

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
}