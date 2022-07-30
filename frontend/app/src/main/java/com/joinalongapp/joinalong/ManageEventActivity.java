package com.joinalongapp.joinalong;

import static com.joinalongapp.LocationUtils.getAddressFromString;
import static com.joinalongapp.LocationUtils.standardizeAddress;
import static com.joinalongapp.LocationUtils.validateAddress;
import static com.joinalongapp.TextInputUtils.isValidNameTitle;

import android.app.DatePickerDialog;
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
    private EditText endDate;
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

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.CANADA);
            beginningDate.setText(sdf.format(userEvent.getBeginningDate()));
            endDate.setText(sdf.format(userEvent.getEndDate()));

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
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.CANADA);

                    Date bDate;
                    try {
                        bDate = sdf.parse(beginningDate.getText().toString());
                    } catch (ParseException e) {
                        beginningDate.setError("Invalid date.");
                        return;
                    }

                    Date eDate;
                    try {
                        eDate = sdf.parse(endDate.getText().toString());
                    } catch (ParseException e) {
                        endDate.setError("Invalid date.");
                        return;
                    }

                    if (!checkDateRange(bDate, eDate)){
                        return;
                    }

                    Event event = new Event();
                    event.setTitle(title.getText().toString());
                    event.setTags(getTagsFromChipGroup());
                    event.setLocation(standardizeAddress(location.getText().toString(), getApplicationContext()));
                    event.setNumberOfPeopleAllowed(Integer.valueOf(numberOfPeople.getSelectedItem().toString()));
                    event.setDescription(description.getText().toString());
                    event.setBeginningDate(bDate);
                    event.setEndDate(eDate);
                    event.setPublicVisibility(eventVisibilityTab.getSelectedTabPosition() == PUBLIC_VISIBILITY_INDEX);

                    //TODO: fix this later
                    if (getIntent().getExtras().getString("testingId") != null) {
                        event.setEventOwnerId(getIntent().getExtras().getString("testingId"));
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
                                                .setDescription("The " + title.getText().toString() + " has been successfully edited.")
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

                                        if (getIntent().getStringExtra("testingToken") != null) {
                                            try {
                                                JSONObject json = new JSONObject(response.body().string());
                                                Event eventForTest = new Event();
                                                eventForTest.populateDetailsFromJson(json.toString());
                                                getIntent().putExtra("createdTestEvent", eventForTest);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

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
            beginningDate.setError("End date cannot be before beginning date.");
            endDate.setError("End date cannot be before beginning date.");
        }

        //TODO: fix this when we add time start and end dates

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.CANADA);
        Date now;
        try {
            now = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            System.out.println("invalid date");
            return false;
        }

        if (bDate.before(now)) {
            isValid = false;
            beginningDate.setError("Beginning date cannot be in the past.");
        }

        if (eDate.before(now)) {
            isValid = false;
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
            title.setError("Empty Title field");
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
            beginningDate.setError("Empty Beginning Date field");
        }
        if(editTextEmpty(endDate)){
            flag = false;
            endDate.setError("Empty End Date field");
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