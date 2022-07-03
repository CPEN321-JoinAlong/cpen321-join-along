package com.joinalongapp.joinalong;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.joinalongapp.viewmodel.RideshareDetails;
import com.lyft.deeplink.RideTypeEnum;
import com.lyft.lyftbutton.LyftButton;
import com.lyft.lyftbutton.LyftStyle;
import com.lyft.lyftbutton.RideParams;
import com.lyft.networking.ApiConfig;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.SessionConfiguration;

import java.io.IOException;
import java.util.List;

public class SelectRideshareActivity extends AppCompatActivity {
    private static final String TAG = "SelectRideshareActivity";
    RideRequestButton uberButton;
    LyftButton lyftButton;
    ImageButton close;

    //TODO: not sure if it is good practice to store this value on the frontend
    private final String UBER_CLIENT_ID = "MRK54yhjV7gLbg0aV7IKrNnmCr9VGdaE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_rideshare);

        initUberButton();
        initLyftButton();

        close = findViewById(R.id.selectRideshareCloseButton);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: change MainActivity.class to the activity that calls ManageRideshareActivity
                Intent intent = new Intent(SelectRideshareActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    private void initUberButton() {
        //TODO: currently set on sandbox so that you can't book an actual ride :)
        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId(UBER_CLIENT_ID)
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();
        UberSdk.initialize(config);

        uberButton = findViewById(R.id.rideshareUberBookingButton);

        loadRideshareParams(config);
    }

    private void loadRideshareParams(SessionConfiguration config) {
        RideshareDetails details = (RideshareDetails) getIntent().getExtras().get("rideshareDetails");

        Address pickupLocation = getPickupLocation(details);
        Address destination = getDestination(details);

        if (pickupLocation != null && destination != null) {
            RideParameters rideParameters = new RideParameters.Builder()
                    .setPickupLocation(pickupLocation.getLatitude(), pickupLocation.getLongitude(), null, pickupLocation.getAddressLine(0))
                    .setDropoffLocation(destination.getLatitude(), destination.getLongitude(), null, destination.getAddressLine(0))
                    .build();

            uberButton.setRideParameters(rideParameters);

            // TODO: I think the following 3 lines of code need server token, but they discontinued those
            //       Replaced with scope tokens, which you need to contact business to access :(
            //       These lines of code display cost and time estimates
            ServerTokenSession session = new ServerTokenSession(config);
            uberButton.setSession(session);
            uberButton.loadRideInformation();
        }
    }

    private void initLyftButton() {
        ApiConfig config = new ApiConfig.Builder()
                .setClientId("JK5Js2s2hjn2")
                //TODO: it appears Lyft also removed client token.
                //      so just like uber, can't send ride parameter data :((
                .setClientToken("...")
                .build();

        lyftButton = (LyftButton) findViewById(R.id.lyft_button);
        lyftButton.setLyftStyle(LyftStyle.HOT_PINK);
        lyftButton.setApiConfig(config);

        loadRideshareParams(config);
    }

    private void loadRideshareParams(ApiConfig config) {
        RideshareDetails details = (RideshareDetails) getIntent().getExtras().get("rideshareDetails");

        Address pickupLocation = getPickupLocation(details);
        Address destination = getDestination(details);

        if (pickupLocation != null && destination != null) {
            RideParams.Builder rideParamsBuilder = new RideParams.Builder()
                    .setPickupLocation(pickupLocation.getLatitude(), pickupLocation.getLongitude())
                    .setDropoffLocation(destination.getLatitude(), destination.getLongitude());
            rideParamsBuilder.setRideTypeEnum(RideTypeEnum.STANDARD);

            lyftButton.setRideParams(rideParamsBuilder.build());
            lyftButton.load();
        }
    }

    private Address getDestination(RideshareDetails details) {
        Address retVal = null;
        try {
            retVal = getAddressFromString(details.getDestination());
        } catch (IOException e) {
            handleInvalidAddress("destination");
        }
        return retVal;
    }

    private Address getPickupLocation(RideshareDetails details) {
        Address retVal = null;
        try {
            retVal = getAddressFromString(details.getPickupLocation());
        } catch (IOException e) {
            handleInvalidAddress("pickup");
        }
        return retVal;
    }

    private void handleInvalidAddress(String theAddressType) {

        new AlertDialog.Builder(this)
                .setTitle("Unable to find address")
                .setMessage("We were unable to find the " + theAddressType + (" address that you entered. " +
                        "Please go back and enter another " + theAddressType + " address, or cancel the rideshare."))
                .setNegativeButton("CANCEL RIDESHARE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: change MainActivity.class to the activity that calls ManageRideshareActivity
                        Intent intent = new Intent(SelectRideshareActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                })
                .setPositiveButton("GO BACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    private Address getAddressFromString(String address) throws IOException {
        Geocoder geocoder = new Geocoder(SelectRideshareActivity.this);
        Address retVal;

        List<Address> searchResultAddresses = geocoder.getFromLocationName(address, 1);
        if (!searchResultAddresses.isEmpty()) {
            retVal = searchResultAddresses.get(0);
        } else {
            throw new IOException("Search for input address yielded no results.");
        }

        return retVal;
    }
}