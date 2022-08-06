package com.joinalongapp.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.joinalong.R;
import com.joinalongapp.navbar.ViewReportFragment;
import com.joinalongapp.viewmodel.ReportDetails;

import org.json.JSONException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {
    List<ReportDetails> datasetOfReports;

    public ReportsAdapter(List<ReportDetails> inputDataset){
        datasetOfReports = inputDataset;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView individualReportedName;
        TextView individualReportType;
        TextView individualReporterName;
        Button reportOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            individualReporterName = itemView.findViewById(R.id.individualReporterName);
            individualReportedName = itemView.findViewById(R.id.individualReportName);
            individualReportType = itemView.findViewById(R.id.individualReportType);
            reportOptions = itemView.findViewById(R.id.reportOptions);
        }

        public TextView getIndividualReportedName() {
            return individualReportedName;
        }

        public TextView getIndividualReportType() {
            return individualReportType;
        }

        public Button getReportOptions() {
            return reportOptions;
        }

        public TextView getIndividualReporterName() {
            return individualReporterName;
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
        holder.getIndividualReportedName().setText(datasetOfReports.get(position).getReportedName());
        holder.getIndividualReportType().setText("Type: " + datasetOfReports.get(position).getReportType());
        holder.getIndividualReporterName().setText("By: " + datasetOfReports.get(position).getReporterName());
        holder.getReportOptions().setVisibility(View.INVISIBLE);
        holder.getReportOptions().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.getReportOptions());
                popup.inflate(R.menu.report_options_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.menu1:
                                try{
                                    deleteReport(datasetOfReports.get(holder.getBindingAdapterPosition()).getId());
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewReportFragment viewReportFragment = new ViewReportFragment();
                Bundle info = new Bundle();
                info.putSerializable("REPORT_DETAILS", datasetOfReports.get(holder.getBindingAdapterPosition()));
                viewReportFragment.setArguments(info);
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.constraintLayoutProfile, viewReportFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
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

    private void deleteReport(String uuid) throws JSONException, IOException {
        for (Iterator<ReportDetails> iterator = datasetOfReports.iterator(); iterator.hasNext(); ) {
            ReportDetails value = iterator.next();
            if (value.getId() == uuid) {
                iterator.remove();
            }
        }
        notifyDataSetChanged();

    }

    public void changeDataset(List<ReportDetails> reports){
        datasetOfReports = reports;
        notifyDataSetChanged();
    }


}
