package com.joinalongapp.navbar;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.ReportDetails;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewReportFragment extends Fragment {
    TextView reportType;
    TextView reportTarget;
    TextView reportReason;
    TextView reportDescription;
    ImageButton backButton;
    ReportDetails reportDetails;

    public ViewReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewReportFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewReportFragment newInstance(String param1, String param2) {
        ViewReportFragment fragment = new ViewReportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reportDetails = (ReportDetails) getArguments().getSerializable("REPORT_DETAILS");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_report, container, false);
        initElements(view);

        if(reportDetails.getIsEvent()){
            reportType.setText("Event");
        }
        else{
            reportType.setText("User");
        }

        reportTarget.setText(reportDetails.getReportingName());
        reportReason.setText(reportDetails.getReason());
        reportDescription.setText(reportDetails.getDescription());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    private void initElements(View view){
        reportType = view.findViewById(R.id.reportType);
        reportTarget = view.findViewById(R.id.reportTarget);
        reportReason = view.findViewById(R.id.viewReportReason);
        reportDescription = view.findViewById(R.id.viewReportDescription);
        backButton = view.findViewById(R.id.viewReportBackButton);
    }
}