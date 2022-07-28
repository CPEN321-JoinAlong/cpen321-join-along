package com.joinalongapp.joinalong;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.adapter.ReportsAdapter;
import com.joinalongapp.controller.PathBuilder;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.controller.ResponseErrorHandler;
import com.joinalongapp.viewmodel.ReportDetails;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

public class ViewReportActivity extends AppCompatActivity {
    private ImageButton backButton;
    private RecyclerView reportRecyclerView;
    private List<ReportDetails> dataset;
    private ReportsAdapter reportsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        initElements();

        reportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportsAdapter = new ReportsAdapter(dataset);
        reportRecyclerView.setAdapter(reportsAdapter);

        try {
            initDataset(this);
        } catch (IOException e) {
            e.printStackTrace();
        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Temp Message");
                finish();
            }
        });


    }

    private void initElements(){
        backButton = findViewById(R.id.reportBackButton);
        reportRecyclerView = findViewById(R.id.reportRecyclerView);
    }

    private void initDataset(Activity activity) throws IOException {
        String token = ((UserApplicationInfo) getApplication()).getUserToken();
        String operation = "Retrieve Reports";

        String path = new PathBuilder()
                .addNode("reports")
                .build();

        new RequestManager().get(path, token, new RequestManager.OnRequestCompleteListener() {
            @Override
            public void onSuccess(Call call, Response response) {

                if (response.isSuccessful()) {
                    List<ReportDetails> outputReports = new ArrayList<>();
                    try{
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        for(int i = 0; i < jsonArray.length(); i++){
                            ReportDetails reportDetails = new ReportDetails();
                            reportDetails.populateDetailsFromJson(jsonArray.get(i).toString());
                            outputReports.add(reportDetails);
                        }
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        reportsAdapter.changeDataset(outputReports);
                                    }
                                });
                            }
                        }, 0);
                    } catch(JSONException | IOException e){
                        FeedbackMessageBuilder.createParseError(e, operation, activity);
                    }
                } else {
                    ResponseErrorHandler.createErrorMessage(response, operation, "report", activity);
                }
            }

            @Override
            public void onError(Call call, IOException e) {
                FeedbackMessageBuilder.createServerConnectionError(e, operation, activity);
            }
        });


    }
}