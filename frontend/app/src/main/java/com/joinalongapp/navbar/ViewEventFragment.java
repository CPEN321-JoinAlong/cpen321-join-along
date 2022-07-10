package com.joinalongapp.navbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewEventFragment extends Fragment {
    private TextView title;
    private ChipGroup tags;
    private TextView description;
    private TextView location;
    private TextView beginDate;
    private TextView endDate;
    private ChipGroup organizer;
    private ChipGroup members;
    private TextView numPeople;
    private ImageButton backButton;

    public ViewEventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewEvent.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewEventFragment newInstance(String param1, String param2) {
        ViewEventFragment fragment = new ViewEventFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_event, container, false);

        initElements(view);

        Bundle bundle = getArguments();

        if (bundle != null) {
            Event event = (Event) bundle.getSerializable("event");
            event = removeMeInitEvent();
            initEventDetails(event);
        }
        //TODO: Remove the next two lines
        Event event = removeMeInitEvent();
        initEventDetails(event);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: this needs fixing to use the native back button
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new HomeFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    private Event removeMeInitEvent() {
        Event event = new Event();
        event.setTitle("exciting event title");
        event.setDescription("desc");
        event.setLocation("2366 Main Mall, Vancouver BC");
        event.setBeginningDate(new Date());
        event.setEndDate(new Date());
        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");
        event.setTags(tags);
        event.setNumberOfPeople(500);
        List<String> members = new ArrayList<>();
        tags.add("person1");
        tags.add("person2");
        event.setFriends(members);
        return event;
    }

    private void initElements(View view) {
        title = view.findViewById(R.id.viewEventTitle);
        tags = view.findViewById(R.id.viewEventAddTags);
        description = view.findViewById(R.id.eventViewDescription);
        location = view.findViewById(R.id.eventViewLocation);
        beginDate = view.findViewById(R.id.eventViewBeginDate);
        endDate = view.findViewById(R.id.eventViewEndDate);
        organizer = view.findViewById(R.id.viewEventOrganizers);
        members = view.findViewById(R.id.viewEventMembers);
        numPeople = view.findViewById(R.id.eventViewNumPeople);
        backButton = view.findViewById(R.id.viewEventBackButton);
    }

    private void initEventDetails(Event event) {
        title.setText(event.getTitle());
        description.setText(event.getDescription());

        for (String tag : event.getTags()) {
            Chip chip = new Chip(getActivity());
            chip.setText(tag);
            tags.addView(chip);
        }

        location.setText(event.getLocation());

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm a");

        beginDate.setText(sdf.format(event.getBeginningDate()));
        endDate.setText(sdf.format(event.getEndDate()));

        Chip ownerChip = new Chip(getActivity());
        ownerChip.setText("owner"/*event.getOwnerName()*/);
        organizer.addView(ownerChip);

        for (String member : event.getFriends()) {
            Chip chip = new Chip(getActivity());
            chip.setText(member);
            members.addView(chip);
        }

        String membersTitleString = "(" + String.valueOf(event.getNumberOfPeople()) + ")";
        numPeople.setText(membersTitleString);


    }
}