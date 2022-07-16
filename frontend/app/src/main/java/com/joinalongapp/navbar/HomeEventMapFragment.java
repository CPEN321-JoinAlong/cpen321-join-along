package com.joinalongapp.navbar;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.joinalongapp.MapClusterItem;
import com.joinalongapp.MapInfoWindowAdapter;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.EventList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeEventMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeEventMapFragment extends Fragment {
    private static final String TAG = "HomeEventMapFragment";
    private MapView mapView;
    private GoogleMap map;
    private List<Event> eventList = new ArrayList<>();
    private ClusterManager<MapClusterItem> clusterManager;

    public HomeEventMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeEventMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeEventMapFragment newInstance(String param1, String param2) {
        HomeEventMapFragment fragment = new HomeEventMapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_event_map, container, false);

        mapView = view.findViewById(R.id.eventMap);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        String filter = getArguments().getString("filter");

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                map = googleMap;
                initMapCamera();
                initClusterManager();

                eventList = ((EventList) getArguments().getSerializable("eventsList")).eventList;
                addEventsToMap();
                clusterManager.cluster();
                clusterManager.getMarkerCollection().setInfoWindowAdapter(new MapInfoWindowAdapter(inflater));
                map.setInfoWindowAdapter(clusterManager.getMarkerManager());

                clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MapClusterItem>() {
                    @Override
                    public void onClusterItemInfoWindowClick(MapClusterItem item) {

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", item.getEvent());
                        bundle.putString("theFrom", "map");
                        ViewEventFragment fragment = new ViewEventFragment();
                        fragment.setArguments(bundle);

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                    }
                });

                clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MapClusterItem>() {
                    @Override
                    public boolean onClusterClick(Cluster<MapClusterItem> cluster) {
                        double latitude = cluster.getPosition().latitude;
                        double longitude = cluster.getPosition().longitude;
                        float zoom = map.getCameraPosition().zoom + 2;
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
                        return true;
                    }
                });

            }
        });

        return view;
    }



    private void initClusterManager() {
        clusterManager = new ClusterManager<>(getActivity(), map);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        ((DefaultClusterRenderer<MapClusterItem>) clusterManager.getRenderer()).setMinClusterSize(2);
    }

    private void addEventsToMap() {
        for (Event event : eventList) {
            String eventLocation = event.getLocation();
            Address address = getAddressFromString(eventLocation);
            if (address != null) {
                LatLng eventLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                MapClusterItem item = new MapClusterItem(eventLatLng.latitude, eventLatLng.longitude, event);
                clusterManager.addItem(item);
            }
        }
    }

    private void initMapCamera() {
        UserApplicationInfo userInfo = ((UserApplicationInfo) getActivity().getApplication());
        String userLocation = userInfo.getProfile().getLocation();

        Address address = getAddressFromString(userLocation);
        if (address != null) {
            LatLng userLatLng = new LatLng(address.getLatitude(), address.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10F));
            return;
        }

        // default camera view
        //TODO: edit this
        LatLng defaultView = new LatLng(0, 0);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultView, 10F));
    }

    private void removeMe_PopulateEventList() {
        Event e1 = new Event();
        e1.setEventId("1");
        e1.setTitle("UBC");
        e1.setDescription("description1");
        e1.setNumberOfPeopleAllowed(1);
        e1.setLocation("2366 Main Mall, Vancouver BC");
        eventList.add(e1);
        Event e2 = new Event();
        e2.setEventId("2");
        e2.setTitle("UoT");
        e2.setDescription("description2");
        e2.setNumberOfPeopleAllowed(2);
        e2.setLocation("27 Kings College Cir, Toronto ON");
        eventList.add(e2);
        Event e3 = new Event();
        e3.setEventId("3");
        e3.setTitle("SFU");
        e3.setDescription("description3");
        e3.setNumberOfPeopleAllowed(3);
        e3.setLocation("8888 University Dr, Burnaby BC");
        eventList.add(e3);
    }

    private Address getAddressFromString(String address) {
        Geocoder geocoder = new Geocoder(getActivity());
        Address retVal = null;
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                retVal = addresses.get(0);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to set location with error: " + e.getMessage());
        }
        return retVal;
    }

}