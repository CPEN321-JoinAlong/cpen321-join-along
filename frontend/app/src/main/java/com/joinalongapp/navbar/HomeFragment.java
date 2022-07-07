package com.joinalongapp.navbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.adapter.EventAdapter;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements EventAdapter.ItemClickListener {
    private RecyclerView eventRecycler;
    private List<Event> eventList = new ArrayList<>();
    private EventAdapter eventAdapter;

    private Spinner eventFilterSpinner;
    private List<String> eventFilterList = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: change this later, this is for testing
        //      this should be a post request for events that match the filter
        removeMe_PopulateEventList();
    }

    private void removeMe_PopulateEventList() {
        Event e1 = new Event();
        e1.setTitle("Test1");
        e1.setDescription("description1");
        e1.setNumberOfPeople(1);
        eventList.add(e1);
        Event e2 = new Event();
        e2.setTitle("Test2");
        e2.setDescription("description2");
        e2.setNumberOfPeople(2);
        eventList.add(e2);
        Event e3 = new Event();
        e3.setTitle("Test3");
        e3.setDescription("description3");
        e3.setNumberOfPeople(3);
        eventList.add(e3);
        Event e4 = new Event();
        e4.setTitle("Test4");
        e4.setDescription("description4");
        e4.setNumberOfPeople(4);
        eventList.add(e4);
        Event e5 = new Event();
        e5.setTitle("Test5");
        e5.setDescription("description5");
        e5.setNumberOfPeople(5);
        eventList.add(e5);
        Event e6 = new Event();
        e6.setTitle("Test6");
        e6.setDescription("description6");
        e6.setNumberOfPeople(6);
        eventList.add(e6);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initElements(view);
        initSpinner();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        eventRecycler.setLayoutManager(linearLayoutManager);
        eventAdapter = new EventAdapter(getActivity(), eventList);
        eventAdapter.setClickListener(this);
        eventRecycler.setAdapter(eventAdapter);



        return view;
    }

    private void initSpinner() {
        eventFilterList.add("Recommended");
        eventFilterList.add("My Events");

        //TODO: change this to a post request for user interests?
        List<String> userInterests = new ArrayList<>();

        eventFilterList.addAll(userInterests);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, eventFilterList);
        eventFilterSpinner.setAdapter(adapter);
    }

    private void initElements(View view) {
        eventRecycler = view.findViewById(R.id.eventListRecyclerView);
        eventFilterSpinner = view.findViewById(R.id.homepageEventsFilter);
    }

    @Override
    public void onItemClick(View view, int position) {
        //TODO: this should be changed to going to view event activity
        Toast.makeText(getActivity(), "Num people in event you clicked on: " + eventList.get(position).getNumberOfPeople(), Toast.LENGTH_LONG).show();
    }
}