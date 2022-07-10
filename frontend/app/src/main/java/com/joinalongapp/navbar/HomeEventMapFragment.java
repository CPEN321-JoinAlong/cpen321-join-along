package com.joinalongapp.navbar;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.joinalongapp.controller.RequestManager;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.MapClusterItem;
import com.joinalongapp.viewmodel.Event;
import com.joinalongapp.viewmodel.UserProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//
//        try {
//            MapsInitializer.initialize(getActivity().getApplicationContext());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                //TODO: implement map search
                //TODO: might need map API key

                map = googleMap;
                initMapCamera();
                removeMe_PopulateEventList();
                clusterManager = new ClusterManager<>(getActivity(), map);
                //TODO: search for all events


//                addEventsToMap();

//                thought searching and loading dynamically as map moves would be possible but a click on a marker count as map being not idle
//                so the message window on marker gets destroyed upon clicking
//                map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
//                    @Override
//                    public void onCameraIdle() {
//                        getEventsInMapBounds();
//                        map.clear();
//                        addEventsToMap();
//                    }
//                });

//                map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
//                    @Override
//                    public void onCameraIdle() {
//                        clusterManager.cluster();
//                    }
//                });
                map.setOnCameraIdleListener(clusterManager);
                map.setOnMarkerClickListener(clusterManager);
                ((DefaultClusterRenderer<MapClusterItem>) clusterManager.getRenderer()).setMinClusterSize(2);
                //addItems();
                addEventsToMap();




//                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                    @Override
//                    public void onInfoWindowClick(@NonNull Marker marker) {
//                        Toast.makeText(getActivity(), "uwu", Toast.LENGTH_SHORT).show();
//                    }
//                });

                clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MapClusterItem>() {
                    @Override
                    public void onClusterItemInfoWindowClick(MapClusterItem item) {
                        Toast.makeText(getActivity(), "uwu", Toast.LENGTH_SHORT).show();
                    }
                });

                clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MapClusterItem>() {
                    @Override
                    public boolean onClusterClick(Cluster<MapClusterItem> cluster) {
                        return false;
                    }
                });
            }
        });

        return view;
    }

    private void addItems() {

        // Set some lat/lng coordinates to start with.
        double lat = 51.5145160;
        double lng = -0.1270060;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 4; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            MapClusterItem offsetItem = new MapClusterItem(lat, lng, "Title " + i, "Snippet " + i);
            clusterManager.addItem(offsetItem);
        }
    }

    private void addEventsToMap() {
        for (Event event : eventList) {
            String eventLocation = event.getLocation();
            Address address = getAddressFromString(eventLocation);
            if (address != null) {
                LatLng eventLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                MapClusterItem item = new MapClusterItem(eventLatLng.latitude, eventLatLng.longitude, event.getTitle(), event.getDescription());
                clusterManager.addItem(item);
            }
        }
    }

    private void addEventsToMap2() {
        for (Event event : eventList) {
            String eventLocation = event.getLocation();
            Address address = getAddressFromString(eventLocation);
            if (address != null) {
                LatLng eventLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                map.addMarker(new MarkerOptions()
                        .position(eventLatLng)
                        .title(event.getTitle()));
            }
        }
    }

    private void initMapCamera() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            UserProfile profile = (UserProfile) bundle.get("Profile");
            if (profile != null) {
                String userLocation = profile.getLocation();
                Address address = getAddressFromString(userLocation);
                if (address != null) {
                    LatLng userLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                    return;
                }
            }
        }
        // default camera view
        //TODO: edit this
        LatLng sydney = new LatLng(-34, 151);
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void getEventsInMapBounds() {
        LatLngBounds bounds = getLatLngBounds();
        Map<String, String> params = buildSearchParams(bounds);

        eventList.clear();

        //TODO: GET REQ, using test method instead
        removeMe_PopulateEventList();
    }

    private void removeMe_PopulateEventList() {
        Event e1 = new Event();
        e1.setTitle("UBC");
        e1.setDescription("description1");
        e1.setNumberOfPeople(1);
        e1.setLocation("2366 Main Mall, Vancouver BC");
        eventList.add(e1);
        Event e2 = new Event();
        e2.setTitle("UoT");
        e2.setDescription("description2");
        e2.setNumberOfPeople(2);
        e2.setLocation("27 Kings College Cir, Toronto ON");
        eventList.add(e2);
        Event e3 = new Event();
        e3.setTitle("SFU");
        e3.setDescription("description3");
        e3.setNumberOfPeople(3);
        e3.setLocation("8888 University Dr, Burnaby BC");
        eventList.add(e3);
    }

    private Map<String,String> buildSearchParams(LatLngBounds bounds) {
        String neLat = String.valueOf(bounds.northeast.latitude);
        String neLng = String.valueOf(bounds.northeast.longitude);
        String swLat = String.valueOf(bounds.southwest.latitude);
        String swLng = String.valueOf(bounds.southwest.longitude);

        String latParam = RequestManager.Range.GREATER_THAN + swLat + RequestManager.Range.LESS_THAN + neLat;
        String lngParam = RequestManager.Range.GREATER_THAN + swLng + RequestManager.Range.LESS_THAN + neLng;

        Map<String, String> params = new HashMap<>();
        params.put("latitude", latParam);
        params.put("longitude", lngParam);

        return params;
    }

    @NonNull
    private LatLngBounds getLatLngBounds() {
        return map.getProjection().getVisibleRegion().latLngBounds;
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