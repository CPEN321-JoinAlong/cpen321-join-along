package com.joinalongapp.navbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.adapter.EventAdapter;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeEventListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeEventListFragment extends Fragment implements EventAdapter.ItemClickListener {
    private RecyclerView eventRecycler;
    private List<Event> eventList = new ArrayList<>();
    private EventAdapter eventAdapter;
    private static final String TAG = "HomeEventListFragment";

    public HomeEventListFragment() {
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
    public static HomeEventListFragment newInstance(String param1, String param2) {
        HomeEventListFragment fragment = new HomeEventListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_event_list, container, false);

        initElements(view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        eventRecycler.setLayoutManager(linearLayoutManager);

        eventList = (List<Event>) getArguments().getSerializable("eventsList");
        setEventCards();

        return view;
    }

    private void setEventCards() {
        eventAdapter = new EventAdapter(getActivity(), eventList);
        eventAdapter.setClickListener(this);
        eventRecycler.setAdapter(eventAdapter);
    }

    private void initElements(View view) {
        eventRecycler = view.findViewById(R.id.eventListRecyclerView);
    }

    /**
     * On clicking an event card, open view event page
     */
    @Override
    public void onItemClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", eventList.get(position));
        bundle.putString("theFrom", "home");

        ViewEventFragment fragment = new ViewEventFragment();
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}