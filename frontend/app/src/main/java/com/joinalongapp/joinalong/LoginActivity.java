package com.joinalongapp.joinalong;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.joinalongapp.Constants;
import com.joinalongapp.controller.RequestManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1;
    private SignInButton signInButton;

    private static final String SCHEME = "http";
    private static final String BASE_URL = "20.9.17.127";
    private final int PORT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
//                .requestIdToken("693671271113-iii855v6rtbcv26d21pdif7ljgi1canc.apps.googleusercontent.com")
//                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
//                .requestServerAuthCode("693671271113-iii855v6rtbcv26d21pdif7ljgi1canc.apps.googleusercontent.com")
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

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
//        if (account != null) {
//            //startMainActivity();
//        }

        signInButton = findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
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
            String idToken = account.getIdToken();

            UserApplicationInfo applicationInfo = new UserApplicationInfo();
            applicationInfo.setUserToken(idToken);

            try {
                String jsonBody = applicationInfo.tokenToJsonStringForLogin();

                RequestManager requestManager = new RequestManager();
                requestManager.post("login", jsonBody, new RequestManager.OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(Call call, Response response) {
                        final int responseCode = response.code();

                        switch (responseCode) {
                            // Successful login, loading user data
                            case Constants.STATUS_HTTP_200:
                                try {
                                    UserApplicationInfo profileOnLogin = new UserApplicationInfo();
                                    String jsonBody = response.body().string();
                                    profileOnLogin.populateDetailsFromJson(jsonBody);
                                    ((UserApplicationInfo) getApplication()).updateApplicaitonInfo(profileOnLogin);
                                    startMainActivity();
                                } catch (IOException | JSONException e) {
                                    createParseError(e);
                                }
                                break;

                            // User not found, must be a new user
                            case Constants.STATUS_HTTP_404:
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
                                break;

                            // User token was invalid
                            case Constants.STATUS_HTTP_406:
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

            } catch (JSONException | IOException e) {
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

    private void createBackendAuthError(Exception e) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Login Failed")
                                .setMessage("Failed to authenticate with backend server. \n Please try again later.")
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                });
            }
        }, 0);
        Log.e(TAG, "Failed to authenticate with backend server: " + e.getMessage());
    }

    private void createBadTokenError() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Login Failed")
                                .setMessage("Failure to authenticate with user token. \n Please try again later.")
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                });
            }
        }, 0);
        Log.e(TAG, "Failure to authenticate with user token.");
    }

    private void createParseError(Exception e) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Login Failed")
                                .setMessage("Unable to parse user data into app.\n Please try again later.")
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                });
            }
        }, 0);
        Log.e(TAG, "Unable to parse user data into app: " + e.getMessage());
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            Log.d(TAG, "There is no user signed in!");
        } else {
            Log.d(TAG, account.getGivenName() + account.getFamilyName() + " has successfully sign in.");
        }
    }

    private void startMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }
}