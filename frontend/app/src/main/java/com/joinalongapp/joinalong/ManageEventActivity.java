package com.joinalongapp.joinalong;

import static com.joinalongapp.LocationUtils.getAddressFromString;
import static com.joinalongapp.LocationUtils.getCoordsFromAddress;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandlerUtils;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.Tag;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
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
    private int PRIVATE_VISIBILITY_INDEX = 1;
    private int PUBLIC_VISIBILITY_INDEX = 0;
    private TextView manageEventTitle;
    private EditText title;
    private EditText location;
    private EditText beginningDate;
    private EditText beginningTime;
    private EditText endDate;
    private EditText endTime;
    private TabLayout eventVisibilityTab;
    private EditText numberOfPeople;
    private EditText description;
    private Button submitButton;
    private ImageButton cancelButton;
    private ChipGroup chipGroupTags;
    private AutoCompleteTextView autoCompleteChipTags;
    private int numPeopleInEventOnEdit = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Trace myTrace = FirebasePerformance.getInstance().newTrace("ManageEventActivityUIComponents");
        myTrace.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        initElements();

        String[] numOfPeople = getResources().getStringArray(R.array.number_of_people_array);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(ManageEventActivity.this, MainActivity.class);
                startActivity(home);
                finish();
            }
        });

        setupDateElements();

        String[] sampleTags = getResources().getStringArray(R.array.sample_tags);
        initAutoCompleteChipGroup(autoCompleteChipTags, chipGroupTags, sampleTags);

        Bundle info = getIntent().getExtras();

        PathBuilder pathBuilder = new PathBuilder();
        pathBuilder.addEvent();

        if (info != null && info.getSerializable("EVENT") != null) {
            setupPageForEdit(info, pathBuilder);
        } else {
            pathBuilder.addCreate();
        }

        initSubmitButton(info, pathBuilder);
        myTrace.stop();
    }

    private void setupDateElements() {
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
    }

    private void setupPageForEdit(Bundle info, PathBuilder pathBuilder) {
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
            eventVisibilityTab.selectTab(eventVisibilityTab.getTabAt(PUBLIC_VISIBILITY_INDEX));
        }
        else {
            eventVisibilityTab.selectTab(eventVisibilityTab.getTabAt(PRIVATE_VISIBILITY_INDEX));
        }

        int numPeopleAllowed = userEvent.getNumberOfPeopleAllowed();
        if (numPeopleAllowed == Integer.MAX_VALUE) {
            numberOfPeople.setText("");
        } else {
            numberOfPeople.setText(String.valueOf(numPeopleAllowed));
        }

        numPeopleInEventOnEdit = userEvent.getCurrentNumPeopleRegistered();

        List<String> existingInterests = userEvent.getStringListOfTags();
        for (String interest : existingInterests) {
            initChipsForChipGroup(chipGroupTags, interest);
        }

        description.setText(userEvent.getDescription());
    }

    private void initSubmitButton(Bundle info, PathBuilder pathBuilder) {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInvalidFields()){
                    Event event = getUserInput();
                    if (event == null) return;

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
                            executeEditEvent(v, requestManager, json, pathBuilder);

                        } else {
                            executeCreateEvent(v, requestManager, json, pathBuilder);
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

    @Nullable
    private Event getUserInput() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);

        Date bDate;
        try {
            String stringDate = beginningDate.getText().toString() + " " + beginningTime.getText().toString();
            bDate = sdf.parse(stringDate);
        } catch (ParseException e) {
            beginningDate.setError("Invalid date.");
            return null;
        }

        Date eDate;
        try {
            String stringDate = endDate.getText().toString() + " " + endTime.getText().toString();
            eDate = sdf.parse(stringDate);
        } catch (ParseException e) {
            endDate.setError("Invalid date.");
            return null;
        }

        if (!checkDateRange(bDate, eDate)){
            return null;
        }

        Event event = new Event();
        event.setOwnerName(((UserApplicationInfo) getApplication()).getProfile().getFullName());
        event.setTitle(title.getText().toString());
        event.setTags(getTagsFromChipGroup());

        Address address = getAddressFromString(location.getText().toString(), getApplicationContext());
        event.setLocation(standardizeAddress(address));
        event.setCoordinates(getCoordsFromAddress(address));

        int numPeople = Integer.MAX_VALUE;
        if (!editTextEmpty(numberOfPeople)) {
            numPeople = Integer.parseInt(numberOfPeople.getText().toString());
        }
        event.setNumberOfPeopleAllowed(numPeople);

        event.setDescription(description.getText().toString());
        event.setBeginningDate(bDate);
        event.setEndDate(eDate);
        event.setPublicVisibility(eventVisibilityTab.getSelectedTabPosition() == PUBLIC_VISIBILITY_INDEX);
        return event;
    }

    private void executeEditEvent(View v, RequestManager requestManager, JSONObject json, PathBuilder pathBuilder) throws IOException {
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
                    ResponseErrorHandlerUtils.createErrorMessage(response, "Edit Event", "event", ManageEventActivity.this);
                }

            }

            @Override
            public void onError(Call call, IOException e) {
                FeedbackMessageBuilder.createServerConnectionError(e, "edit event", ManageEventActivity.this);
            }
        });
    }

    private void executeCreateEvent(View v, RequestManager requestManager, JSONObject json, PathBuilder pathBuilder) throws IOException {
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
                    ResponseErrorHandlerUtils.createErrorMessage(response, "Create Event", "event", ManageEventActivity.this);
                }
            }

            @Override
            public void onError(Call call, IOException e) {
                FeedbackMessageBuilder.createServerConnectionError(e, "create event", ManageEventActivity.this);
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
        numberOfPeople = findViewById(R.id.eventManagementNumberOfPeople);
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

        flag = checkValidLocation(flag);
        flag = checkValidDates(flag);
        flag = checkValidNumberOfPeople(flag);

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

    private Boolean checkValidNumberOfPeople(Boolean flag) {
        boolean retVal = flag;
        if (!editTextEmpty(numberOfPeople)) {
            BigInteger bigInteger = new BigInteger(numberOfPeople.getText().toString());
            if (bigInteger.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) == 1) {
                retVal = false;
                numberOfPeople.setText(String.valueOf(Integer.MAX_VALUE));
                numberOfPeople.setError("Maximum number of people is " + Integer.MAX_VALUE);
            }
        }

        if (!editTextEmpty(numberOfPeople)) {
            String numPeopleString = numberOfPeople.getText().toString();
            int numPeopleInt = Integer.parseInt(numPeopleString);
            if (numPeopleInt < 1) {
                retVal = false;
                numberOfPeople.setError("Number of people must be at least 1.");
            } else if (numPeopleInt < numPeopleInEventOnEdit) {
                retVal = false;
                numberOfPeople.setError("Number of people registered in event (" + numPeopleInEventOnEdit + ") exceeds new value.");
            }
        }
        return retVal;
    }

    private Boolean checkValidLocation(Boolean flag) {
        boolean retVal = flag;
        if(editTextEmpty(location)){
            retVal = false;
            location.setError("Empty Location field");
        } else {
            Address address = getAddressFromString(location.getText().toString(), getApplicationContext());
            if(!validateAddress(address)){
                retVal = false;
                location.setError("Invalid Address");
            }
        }
        return retVal;
    }

    private Boolean checkValidDates(Boolean flag) {
        boolean retVal = flag;
        if(editTextEmpty(beginningDate)){
            retVal = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "Empty Beginning Date field", Toast.LENGTH_SHORT).show();
            beginningDate.setError("Empty Beginning Date field");
        }
        if(editTextEmpty(beginningTime)){
            retVal = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "Empty Beginning Time field", Toast.LENGTH_SHORT).show();
            beginningTime.setError("Empty Beginning Time field");
        }
        if(editTextEmpty(endDate)){
            retVal = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "Empty End Date field", Toast.LENGTH_SHORT).show();
            endDate.setError("Empty End Date field");
        }
        if(editTextEmpty(endTime)){
            retVal = false;
            //Due to limitations with EditText, these error messages have to be toast.
            Toast.makeText(this, "Empty End Time field", Toast.LENGTH_SHORT).show();
            endTime.setError("Empty End Time field");
        }
        return retVal;
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

                String tagName = (String) parent.getItemAtPosition(position);

                for (Tag tag : getTagsFromChipGroup()) {
                    if (tag.getName().equals(tagName)) {
                        return;
                    }
                }

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.individual_entry_chip, chipGroup, false);
                chip.setText(tagName);
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