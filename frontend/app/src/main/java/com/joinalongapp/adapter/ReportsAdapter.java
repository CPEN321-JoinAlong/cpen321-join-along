package com.joinalongapp.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.ReportDetails;


import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {
    List<ReportDetails> datasetOfReports;

    public ReportsAdapter(List<ReportDetails> inputDataset){
        datasetOfReports = inputDataset;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView individualReportName;
        TextView individualReportType;
        ImageButton reportOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            individualReportName = itemView.findViewById(R.id.individualReportName);
            individualReportType = itemView.findViewById(R.id.individualReportType);
            reportOptions = itemView.findViewById(R.id.reportOptions);
        }

        public TextView getIndividualReportName() {
            return individualReportName;
        }

        public TextView getIndividualReportType() {
            return individualReportType;
        }

        public ImageButton getReportOptions() {
            return reportOptions;
        }
    }

    @NonNull
    @Override
    public ReportsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_list_item, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportsAdapter.ViewHolder holder, int position) {
        holder.getIndividualReportName().setText(datasetOfReports.get(position).getReportingName());
        // Set the type
        holder.getReportOptions().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.getReportOptions());
                popup.inflate(R.menu.report_options_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return false;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        if(datasetOfReports == null){
            return 0;
        }
        else{
            return datasetOfReports.size();
        }
    }



}
