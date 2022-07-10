package com.joinalongapp.joinalong;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.ReportDetails;
import com.joinalongapp.viewmodel.UserProfile;

import org.json.JSONException;

public class ReportActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_report);

        initElements();

        Bundle info = getIntent().getExtras();
        Boolean reportType = info.getBoolean("REPORT_PERSON");

        ReportDetails reportDetails = new ReportDetails();
        String reportEntityName;
        if(reportType){
            UserProfile reportingPerson = (UserProfile) info.getSerializable("REPORTING_USER");
            String reportingName = " " + reportingPerson.getName();
            reportEntityName = reportingPerson.getName();
            reportingSubtitle.append(reportingName);
            reportDetails.setReportPerson(true);
        }
        else{
            Event reportingEvent = (Event) info.getSerializable("REPORTING_EVENT");
            String reportingEventName = " " + reportingEvent.getTitle();
            reportEntityName = reportingEvent.getTitle();
            reportingSubtitle.append(reportingEventName);
            reportBlockSubtitle.append(" " + reportingEvent.getOwnerName());
            reportDetails.setReportPerson(false);
        }






        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportDetails.setReportingName(reportEntityName);
                reportDetails.setReason(reportReason.getText().toString());
                reportDetails.setDescription(reportDescription.getText().toString());
                reportDetails.setBlockStatus(blockSelectionTab.getSelectedTabPosition() == BLOCK_INDEX);

                try {
                    String request = reportDetails.toJsonString();
                    Log.d("ReportActivity", request);
                    finish();
                } catch(JSONException e) {
                    e.printStackTrace();
                }

                //TODO: post to backend
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