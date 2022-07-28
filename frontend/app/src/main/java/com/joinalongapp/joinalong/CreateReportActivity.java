package com.joinalongapp.joinalong;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.ReportDetails;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class CreateReportActivity extends AppCompatActivity {

    private TextView reportingSubtitle;
    private EditText reportReason;
    private EditText reportDescription;
    private TabLayout blockSelectionTab;
    private Button submitButton;
    private ImageButton cancelButton;
    private TextView reportBlockSubtitle;
    private int BLOCK_INDEX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        initElements();

        Bundle info = getIntent().getExtras();
        Boolean reportType = info.getBoolean("REPORT_PERSON");
        String path;
        String reportingId;
        ReportDetails reportDetails = new ReportDetails();
        String token = ((UserApplicationInfo) getApplication()).getUserToken();
        UserProfile user = ((UserApplicationInfo) getApplication()).getProfile();
        String reportEntityName;

        if(reportType){
            UserProfile reportingPerson = (UserProfile) info.getSerializable("REPORTING_PERSON");
            String reportingName = " " + reportingPerson.getFullName();
            reportEntityName = reportingPerson.getFullName();
            reportingSubtitle.append(reportingName);
            reportDetails.setIsEvent(false);
            path = "reportUser"; // TODO: HTTPhmm 200, 500
            reportingId = reportingPerson.getId();
        }
        else{
            Event reportingEvent = (Event) info.getSerializable("REPORTING_EVENT");
            String reportingEventName = " " + reportingEvent.getTitle();
            reportEntityName = reportingEvent.getTitle();
            reportingSubtitle.append(reportingEventName);
            //TODO: warning: get owner name always return empty string
            reportBlockSubtitle.append(" " + reportingEvent.getOwnerName());
            reportDetails.setIsEvent(true);
            path = "reportEvent"; //TODO: HTTPhmm 200, 500
            reportingId = reportingEvent.getEventId();
        }



        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //todo: pls fix these finals
        String finalPath = path;
        String finalReportingId = reportingId;
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportDetails.setReportingName(reportEntityName);
                reportDetails.setReason(reportReason.getText().toString());
                reportDetails.setDescription(reportDescription.getText().toString());
                reportDetails.setBlockStatus(blockSelectionTab.getSelectedTabPosition() == BLOCK_INDEX);
                reportDetails.setReporterId(user.getId());
                reportDetails.setReportedId(reportingId);

                JSONObject json = null;
                try {
                    json = reportDetails.toJson();
                    json.put("token", token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestManager requestManager = new RequestManager();
                try {
                    String path = new PathBuilder()
                            .addUser()
                            .addNode(user.getId())
                            .addNode(finalPath)
                            .addNode(finalReportingId)
                            .build();

                    requestManager.post(path, json.toString(), new RequestManager.OnRequestCompleteListener() {
                        @Override
                        public void onSuccess(Call call, Response response) {
                            // TODO: add success and error messages.
                            System.out.println("Success!");
                        }

                        @Override
                        public void onError(Call call, IOException e) {
                            System.out.println("Error!");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CreateReportActivity.this.finish();
            }
        });
    }

    private void initElements(){
        reportingSubtitle = findViewById(R.id.reportingSubtitleTextView);
        reportReason = findViewById(R.id.reportReasonEditText);
        reportDescription = findViewById(R.id.reportDescriptionEditText);
        blockSelectionTab = findViewById(R.id.eventVisibilitySelection);
        submitButton = findViewById(R.id.submitReportButton);
        cancelButton = findViewById(R.id.reportCancelButton);
        reportBlockSubtitle = findViewById(R.id.reportBlockSubtitle);
    }
}