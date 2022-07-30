package com.joinalongapp.navbar;

import static com.joinalongapp.LocationUtils.getAddressFromString;

import android.location.Address;
import android.os.Bundle;
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
import com.joinalongapp.joinalong.R;
import com.joinalongapp.joinalong.UserApplicationInfo;
import com.joinalongapp.maputils.MapClusterItem;
import com.joinalongapp.maputils.MapInfoWindowAdapter;
import com.joinalongapp.viewmodel.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    private static final float DEFAULT_ZOOM = 11F;

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

        initElements(view);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                map = googleMap;
                initMapCamera();
                initClusterManager();

                eventList = (List<Event>) getArguments().getSerializable("eventsList");
                addEventsToMap();

                clusterManager.cluster();
                clusterManager.getMarkerCollection().setInfoWindowAdapter(new MapInfoWindowAdapter(inflater));
                map.setInfoWindowAdapter(clusterManager.getMarkerManager());

                initMarkerInfoWindowClickListerner();
                initMarkerClusterClickListener();

            }
        });

        return view;
    }

    private void initElements(View view) {
        mapView = view.findViewById(R.id.eventMap);
    }

    private void initMarkerClusterClickListener() {
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

    private void initMarkerInfoWindowClickListerner() {
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
    }

    private void initClusterManager() {
        clusterManager = new ClusterManager<>(getActivity(), map);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        ((DefaultClusterRenderer<MapClusterItem>) clusterManager.getRenderer()).setMinClusterSize(2);
    }

    private void addEventsToMap() {
        List<LatLng> latLngList = new ArrayList<>();

        for (Event event : eventList) {
            String eventLocation = event.getLocation();
            Address address = getAddressFromString(eventLocation, getActivity().getApplicationContext());

            double addressLat = roundToFiveDecimalPlaces(address.getLatitude());
            double addressLong = roundToFiveDecimalPlaces(address.getLongitude());

            LatLng eventLatLng = new LatLng(addressLat, addressLong);

            while (latLngList.contains(eventLatLng)) {
                addressLat += randomOffset();
                addressLong += randomOffset();

                eventLatLng = new LatLng(addressLat, addressLong);
            }

            addMapMarker(event, eventLatLng);

            latLngList.add(eventLatLng);
        }
    }

    private double randomOffset() {
        List<Double> list = Arrays.asList(-1e-4, 0.0, 1e-4);
        Random random = new Random();

        return list.get(random.nextInt(list.size()));
    }

    private double roundToFiveDecimalPlaces(double toRound) {
        return Math.round(toRound * 1e5) / 1e5;
    }

    private void addMapMarker(Event event, LatLng eventLatLng) {
        MapClusterItem item = new MapClusterItem(eventLatLng.latitude, eventLatLng.longitude, event);
        clusterManager.addItem(item);
    }

    private void initMapCamera() {
        UserApplicationInfo userInfo = ((UserApplicationInfo) getActivity().getApplication());
        String userLocation = userInfo.getProfile().getLocation();

        Address address = getAddressFromString(userLocation, getActivity().getApplicationContext());
        if (address != null) {
            LatLng userLatLng = new LatLng(address.getLatitude(), address.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM));
            return;
        }

        // default camera view
        LatLng defaultView = new LatLng(0, 0);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultView, DEFAULT_ZOOM));
    }

}