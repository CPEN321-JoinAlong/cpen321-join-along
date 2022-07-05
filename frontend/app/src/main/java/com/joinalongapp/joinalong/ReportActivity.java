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
import com.joinalongapp.viewmodel.ReportDetails;
import com.joinalongapp.viewmodel.User;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportActivity extends AppCompatActivity {

    private TextView reportingSubtitle;
    private EditText reportReason;
    private EditText reportDescription;
    private TabLayout blockSelectionTab;
    private Button submitButton;
    private ImageButton cancelButton;
    private int BLOCK_INDEX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initElements();

        Bundle info = getIntent().getExtras();
        User reportingPerson = (User) info.getSerializable("REPORTING_PERSON");

        String reportingName = " " + reportingPerson.getName();
        reportingSubtitle.append(reportingName);

        ReportDetails reportDetails = new ReportDetails();


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportDetails.setReportingName(reportingName.trim());
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
    }
}