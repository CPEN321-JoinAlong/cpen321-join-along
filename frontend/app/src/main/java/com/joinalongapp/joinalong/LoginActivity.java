package com.joinalongapp.joinalong;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.joinalongapp.FeedbackMessageBuilder;
import com.joinalongapp.HttpStatusConstants;
import com.joinalongapp.controller.RequestManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 1;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Trace myTrace = FirebasePerformance.getInstance().newTrace("LoginActivityUIComponents");
        myTrace.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.dark_mode_prefs), Context.MODE_PRIVATE);
        boolean darkMode = sharedPreferences.getBoolean(getString(R.string.dark_mode_prefs), false);


        if(darkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.silentSignIn()
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        handleSignInResult(task);
                    }
                });

        initElements();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        myTrace.stop();

    }

    private void initElements() {
        signInButton = findViewById(R.id.sign_in_button);
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            try {
                String jsonBody = getTokenForBackendAuth(account.getIdToken());

                RequestManager requestManager = new RequestManager();
                requestManager.post("login", jsonBody, new RequestManager.OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(Call call, Response response) {
                        final int responseCode = response.code();

                        switch (responseCode) {
                            // Successful login, loading user data
                            case HttpStatusConstants.STATUS_HTTP_200:
                                try {
                                    populateUserDetailsOnLogin(response);
                                    startMainActivity();
                                } catch (IOException | JSONException e) {
                                    createParseError(e);
                                }
                                break;

                            // User not found, must be a new user
                            case HttpStatusConstants.STATUS_HTTP_404:
                                startCreateProfileActivity(response, account);
                                break;

                            // User token was invalid
                            case HttpStatusConstants.STATUS_HTTP_406:
                            default:
                                createBadTokenError();
                                break;
                        }
                    }

                    @Override
                    public void onError(Call call, IOException e) {
                        createBackendAuthError(e);
                    }
                });

            } catch (JSONException e) {
                createTokenParseError(e);
            } catch (IOException e) {
                createBackendAuthError(e);
            }

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode() + " Message: " + e.getMessage());
            updateUI(null);
        }
    }

    private void startCreateProfileActivity(Response response, GoogleSignInAccount account) {
        Intent i = new Intent(LoginActivity.this, ManageProfileActivity.class);
        i.putExtra("firstName", account.getGivenName());

        try {
            JSONObject responseBody = new JSONObject(response.body().string());
            i.putExtra("userToken", responseBody.getString("token"));
        } catch (IOException | JSONException e) {
            createParseError(e);
        }

        i.putExtra("lastName", account.getFamilyName());
        i.putExtra("profilePic", account.getPhotoUrl().toString());
        i.putExtra("MODE", ManageProfileActivity.ManageProfileMode.PROFILE_CREATE);
        startActivity(i);
        finish();
    }

    private void populateUserDetailsOnLogin(Response response) throws IOException, JSONException {
        UserApplicationInfo profileOnLogin = new UserApplicationInfo();
        String jsonBody = response.body().string();
        profileOnLogin.populateDetailsFromJson(jsonBody);
        ((UserApplicationInfo) getApplication()).updateApplicationInfo(profileOnLogin);
    }

    private String getTokenForBackendAuth(String idToken) throws JSONException {
        UserApplicationInfo preAuthDetails = new UserApplicationInfo();
        preAuthDetails.setUserToken(idToken);
        return preAuthDetails.tokenToJsonStringForLogin();
    }

    private void createBadLoginError(String description) {
        new FeedbackMessageBuilder()
                .setTitle("Login Failed")
                .setDescription(description)
                .withActivity(LoginActivity.this)
                .buildAsyncNeutralMessage();
    }

    private void createTokenParseError(JSONException e) {
        createBadLoginError("Failed to encode data for backend authentication.\nPlease try again later.");
        Log.e(TAG, "Failed to authenticate with backend server: " + e.getMessage());
    }

    private void createBackendAuthError(Exception e) {
        createBadLoginError("Failed to authenticate with backend server.\nPlease try again later.");
        Log.e(TAG, "Failed to authenticate with backend server: " + e.getMessage());
    }

    private void createBadTokenError() {
        createBadLoginError("Failure to authenticate with user token.\nPlease try again later.");
        Log.e(TAG, "Failure to authenticate with user token.");
    }

    private void createParseError(Exception e) {
        createBadLoginError("Unable to parse user data into app.\nPlease try again later.");
        Log.e(TAG, "Unable to parse user data into app: " + e.getMessage());
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            Log.d(TAG, "There is no user signed in!");
        } else {
            Log.i(TAG, account.getGivenName() + account.getFamilyName() + " has successfully sign in with Google.");
        }
    }

    private void startMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}