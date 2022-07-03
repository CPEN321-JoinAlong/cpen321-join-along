package com.joinalongapp.joinalong;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SelectRideshareActivity extends AppCompatActivity {
//    RideRequestButton uberButton;

    //TODO: not sure if it is good practice to store this value on the frontend
    private final String UBER_CLIENT_ID = "MRK54yhjV7gLbg0aV7IKrNnmCr9VGdaE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_rideshare);

//        SessionConfiguration config = new SessionConfiguration.Builder()
//                .setClientId(UBER_CLIENT_ID)
//                .setRedirectUri("<REDIRECT_URI>")
//                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
//                .build();
//        UberSdk.initialize(config);
//
//        uberButton = findViewById(R.id.rideshareUberBookingButton);
//
//        RideshareDetails details = (RideshareDetails) getIntent().getExtras().get("rideshareDetails");
//        Address pickupLocation = details.getPickupLocation();
//        Address destination = details.getDestination();
//
//        //TODO: check the address lat, long, and address lines
//        RideParameters rideParameters = new RideParameters.Builder()
//                .setPickupLocation(pickupLocation.getLatitude(), pickupLocation.getLongitude(), null, pickupLocation.getAddressLine(0))
//                .setDropoffLocation(destination.getLatitude(), destination.getLongitude(), null, destination.getAddressLine(0))
//                .build();
//
//        uberButton.setRideParameters(rideParameters);
//
//        ServerTokenSession session = new ServerTokenSession(config);
//        uberButton.setSession(session);
//        uberButton.loadRideInformation();
    }

    private void initUberSession() {

    }
}