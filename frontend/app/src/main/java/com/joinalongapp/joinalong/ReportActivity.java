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
import com.joinalongapp.viewmodel.User;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Bundle info = getIntent().getExtras();
        User reportingPerson = (User) info.getSerializable("REPORTING_PERSON");

        String reportingName = " " + reportingPerson.getName();
        int BLOCK_INDEX = 0;

        TextView reportingSubtitle = findViewById(R.id.reportingSubtitleTextView);
        EditText reportReason = findViewById(R.id.reportReasonEditText);
        EditText reportDescription = findViewById(R.id.reportDescriptionEditText);
        TabLayout blockSelectionTab = findViewById(R.id.reportVisibilitySelection);

        Button submitButton = findViewById(R.id.submitReportButton);
        ImageButton cancelButton = findViewById(R.id.reportCancelButton);

        reportingSubtitle.append(reportingName);


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject request = new JSONObject();
                Boolean blockStatus = false;
                if(blockSelectionTab.getSelectedTabPosition() == BLOCK_INDEX){
                    blockStatus = true;
                }
                try {
                    request.put("reason", reportReason.getText().toString());
                    request.put("description", reportDescription.getText().toString());
                    request.put("block", blockStatus);
                    Log.d("ReportActivity", request.toString());
                    finish();
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}