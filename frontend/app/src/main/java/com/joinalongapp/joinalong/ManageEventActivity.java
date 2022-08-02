package com.joinalongapp.joinalong;

import static com.joinalongapp.LocationUtils.getAddressFromString;
import static com.joinalongapp.LocationUtils.standardizeAddress;
import static com.joinalongapp.LocationUtils.validateAddress;
import static com.joinalongapp.TextInputUtils.isValidNameTitle;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandler;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.Tag;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

public class ManageEventActivity extends AppCompatActivity {
    private int PRIVATE_VISIBILITY_INDEX = 0;
    private int PUBLIC_VISIBILITY_INDEX = 1;
    private TextView manageEventTitle;
    private EditText title;
    private EditText location;
    private EditText beginningDate;
    private EditText beginningTime;
    private EditText endDate;
    private EditText endTime;
    private TabLayout eventVisibilityTab;
    private Spinner numberOfPeople;
    private EditText description;
    private Button submitButton;
    private ImageButton cancelButton;
    private ChipGroup chipGroupTags;
    private AutoCompleteTextView autoCompleteChipTags;
    private String TAG = "ManageEventActivity";

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
                Intent home = new Intent(ManageEventActivity.this, MainActivity.class);
                startActivity(home);
                finish();
            }
        });

        beginningDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarOperation(beginningDate);
            }
        });

        beginningTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeOperation(beginningTime);
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarOperation(endDate);
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeOperation(endTime);
            }
        });

        String[] sampleTags = getResources().getStringArray(R.array.sample_tags);
        initAutoCompleteChipGroup(autoCompleteChipTags, chipGroupTags, sampleTags);

        Bundle info = getIntent().getExtras();

        PathBuilder pathBuilder = new PathBuilder();
        pathBuilder.addEvent();

        if (info != null && info.getSerializable("EVENT") != null) {
            // Must append pre-existing text due to editing of Event.
            Event userEvent = (Event) info.getSerializable("EVENT");
            manageEventTitle.setText("Edit Event");
            submitButton.setText("Edit");

            pathBuilder.addNode(userEvent.getEventId());
            pathBuilder.addEdit();

            title.setText(userEvent.getTitle());
            location.setText(userEvent.getLocation());

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            beginningDate.setText(sdfDate.format(userEvent.getBeginningDate()));
            beginningTime.setText(sdfTime.format(userEvent.getBeginningDate()));
            endDate.setText(sdfDate.format(userEvent.getEndDate()));
            endTime.setText(sdfTime.format(userEvent.getEndDate()));

            boolean publicVisibility = userEvent.getPublicVisibility();
            if(publicVisibility){
                eventVisibilityTab.setSelectedTabIndicator(PUBLIC_VISIBILITY_INDEX);
            }
            else {
                eventVisibilityTab.setSelectedTabIndicator(PRIVATE_VISIBILITY_INDEX);
            }

            int position = arrayAdapter.getPosition(String.valueOf(userEvent.getNumberOfPeopleAllowed()));
            numberOfPeople.setSelection(position);

            List<String> existingInterests = userEvent.getStringListOfTags();
            for (String interest : existingInterests) {
                initChipsForChipGroup(chipGroupTags, interest);
            }

            description.setText(userEvent.getDescription());
        } else {
            pathBuilder.addCreate();
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInvalidFields()){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);

                    Date bDate;
                    try {
                        String stringDate = beginningDate.getText().toString() + " " + beginningTime.getText().toString();
                        bDate = sdf.parse(stringDate);
                    } catch (ParseException e) {
                        beginningDate.setError("Invalid date.");
                        return;
                    }

                    Date eDate;
                    try {
                        String stringDate = endDate.getText().toString() + " " + endTime.getText().toString();
                        eDate = sdf.parse(stringDate);
                    } catch (ParseException e) {
                        endDate.setError("Invalid date.");
                        return;
                    }

                    if (!checkDateRange(bDate, eDate)){
                        return;
                    }

                    Event event = new Event();
                    event.setOwnerName(((UserApplicationInfo) getApplication()).getProfile().getFullName());
                    event.setTitle(title.getText().toString());
                    event.setTags(getTagsFromChipGroup());
                    event.setLocation(standardizeAddress(location.getText().toString(), getApplicationContext()));
                    event.setNumberOfPeopleAllowed(Integer.valueOf(numberOfPeople.getSelectedItem().toString()));
                    event.setDescription(description.getText().toString());
                    event.setBeginningDate(bDate);
                    event.setEndDate(eDate);
                    event.setPublicVisibility(eventVisibilityTab.getSelectedTabPosition() == PUBLIC_VISIBILITY_INDEX);

                    //TODO: fix this later
                    if (getIntent().getExtras() != null) {
                        if (getIntent().getExtras().getString("testingId") != null) {
                            event.setEventOwnerId(getIntent().getExtras().getString("testingId"));
                        }
                    } else {
                        String ownerId = ((UserApplicationInfo) getApplication()).getProfile().getId();
                        event.setEventOwnerId(ownerId);
                    }

                    RequestManager requestManager = new RequestManager();
                    try {
                        JSONObject json = event.toJson();
                        String token = ((UserApplicationInfo) getApplication()).getUserToken();
                        if (token == null) {
                            token = getIntent().getExtras().getString("testingToken");
                        }
                        json.put("token", token);

                        if (info != null && info.getSerializable("EVENT") != null) {
                            // EDIT EVENT
                            requestManager.put(pathBuilder.build(), json.toString(), new RequestManager.OnRequestCompleteListener() {
                                @Override
                                public void onSuccess(Call call, Response response) {

                                    if (response.isSuccessful()) {
                                        Intent i = new Intent(v.getContext(), MainActivity.class);

                                        new FeedbackMessageBuilder()
                                                .setTitle("Event Edited!")
                                                .setDescription("The " + title.getText().toString() + " event has been successfully edited.")
                                                .withActivity(ManageEventActivity.this)
                                                .buildAsyncNeutralMessageAndStartActivity(i);
                                    } else {
                                        ResponseErrorHandler.createErrorMessage(response, "Edit Event", "event", ManageEventActivity.this);
                                    }

                                }

                                @Override
                                public void onError(Call call, IOException e) {
                                    FeedbackMessageBuilder.createServerConnectionError(e, "edit event", ManageEventActivity.this);
                                }
                            });

                        } else {
                            // CREATE EVENT
                            requestManager.post(pathBuilder.build(), json.toString(), new RequestManager.OnRequestCompleteListener() {
                                @Override
                                public void onSuccess(Call call, Response response) {

                                    if (response.isSuccessful()) {
                                        Intent i = new Intent(v.getContext(), MainActivity.class);

                                        new FeedbackMessageBuilder()
                                                .setTitle("Event Created!")
                                                .setDescription("The " + title.getText().toString() + " event has been successfully created.")
                                                .withActivity(ManageEventActivity.this)
                                                .buildAsyncNeutralMessageAndStartActivity(i);

                                        new Timer().schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                ManageEventActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        submitButton.setEnabled(false);
                                                        submitButton.setVisibility(View.GONE);
                                                    }
                                                });
                                            }
                                        }, 0);

                                    } else {
                                        ResponseErrorHandler.createErrorMessage(response, "Create Event", "event", ManageEventActivity.this);
                                    }
                                }

                                @Override
                                public void onError(Call call, IOException e) {
                                    FeedbackMessageBuilder.createServerConnectionError(e, "create event", ManageEventActivity.this);
                                }
                            });
                        }
                    } catch (IOException e) {
                        FeedbackMessageBuilder.createServerConnectionError(e, "create event", ManageEventActivity.this);
                    } catch (JSONException e) {
                        FeedbackMessageBuilder.createParseError(e, "create event", ManageEventActivity.this);
                    }
                }
            }
        });
    }

    public void initChipsForChipGroup(ChipGroup chipGroup, String chipText) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_entry_chip, chipGroup, false);
        chip.setText(chipText);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
            }
        });
        chipGroup.addView(chip);
    }

    private boolean checkDateRange(Date bDate, Date eDate) {
        boolean isValid = true;
        if (!bDate.before(eDate) && !bDate.equals(eDate)){
            isValid = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "End date cannot be before beginning date.", Toast.LENGTH_SHORT).show();
            beginningDate.setError("End date cannot be before beginning date.");
            endDate.setError("End date cannot be before beginning date.");
        }

        Date now = new Date();

        if (bDate.before(now)) {
            isValid = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "Beginning date cannot be in the past.", Toast.LENGTH_SHORT).show();
            beginningDate.setError("Beginning date cannot be in the past.");
        }

        if (eDate.before(now)) {
            isValid = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "End date cannot be in the past.", Toast.LENGTH_SHORT).show();
            endDate.setError("End date cannot be in the past.");
        }

        return isValid;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ManageEventActivity.this, MainActivity.class);
        startActivity(i);
    }

    private List<Tag> getTagsFromChipGroup(){
        List<Tag> result = new ArrayList<>();
        for(int i = 0; i < chipGroupTags.getChildCount(); i++){
            Chip chip = (Chip) chipGroupTags.getChildAt(i);
            result.add(new Tag(chip.getText().toString()));
        }
        return result;
    }

    private void initElements(){
        manageEventTitle = findViewById(R.id.eventManagementTitle);
        title = findViewById(R.id.editTextEventManagementTitle);
        location = findViewById(R.id.editTextEventManagementLocation);
        beginningDate = findViewById(R.id.editTextEventManagementBeginningDate);
        beginningTime = findViewById(R.id.editTextEventManagementBeginningTime);
        endDate = findViewById(R.id.editTextEventManagementEndDate);
        endTime = findViewById(R.id.editTextEventManagementEndTime);
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
            title.setError("Empty Title field");
            title.requestFocus();
        }
        if (!isValidNameTitle(title.getText().toString())) {
            title.setError("Title contains invalid character(s).");
            flag = false;
        }
        if(editTextEmpty(location)){
            flag = false;
            location.setError("Empty Location field");
        } else {
            Address address = getAddressFromString(location.getText().toString(), getApplicationContext());
            if(!validateAddress(address)){
                flag = false;
                location.setError("Invalid Address");
            }
        }
        if(editTextEmpty(beginningDate)){
            flag = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "Empty Beginning Date field", Toast.LENGTH_SHORT).show();
            beginningDate.setError("Empty Beginning Date field");
        }
        if(editTextEmpty(beginningTime)){
            flag = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "Empty Beginning Time field", Toast.LENGTH_SHORT).show();
            beginningTime.setError("Empty Beginning Time field");
        }
        if(editTextEmpty(endDate)){
            flag = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "Empty End Date field", Toast.LENGTH_SHORT).show();
            endDate.setError("Empty End Date field");
        }
        if(editTextEmpty(endTime)){
            flag = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "Empty End Time field", Toast.LENGTH_SHORT).show();
            endTime.setError("Empty End Time field");
        }
        if(chipGroupTags.getChildCount() == 0){
            flag = false;
            autoCompleteChipTags.setError("No interest tags selected.");
        }
        if(editTextEmpty(description)){
            flag = false;
            description.setError("Empty Description field");
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

        long now = new Date().getTime();

        datePicker.getDatePicker().setMinDate(now);
        datePicker.show();
    }

    private void timeOperation(EditText editText) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(ManageEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Calendar input = Calendar.getInstance();
                input.set(Calendar.HOUR_OF_DAY, selectedHour);
                input.set(Calendar.MINUTE, selectedMinute);

                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.CANADA);

                String time = sdf.format(input.getTime());
                editText.setText(time.replaceAll("\\.", ""));
            }
        }, hour, minute, false);

        timePicker.show();
    }

    private void initAutoCompleteChipGroup(AutoCompleteTextView autoCompleteTextView, ChipGroup chipGroup, String[] fillArray){
        ArrayAdapter<String> arrayAdapterTags = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, fillArray);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(arrayAdapterTags);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                autoCompleteTextView.setText("");

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_entry_chip, chipGroup, false);
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