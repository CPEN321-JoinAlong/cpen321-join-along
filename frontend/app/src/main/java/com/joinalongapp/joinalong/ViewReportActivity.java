package com.joinalongapp.joinalong;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class ViewReportActivity extends AppCompatActivity {
    private ImageButton backButton;
    private RecyclerView reportRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);
        initDataset();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Temp Message");
            }
        });


    }

    private void initDataset(){
        backButton = findViewById(R.id.reportBackButton);
        reportRecyclerView = findViewById(R.id.reportRecyclerView);
    }
}