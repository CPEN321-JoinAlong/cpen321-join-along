package com.joinalongapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

//TODO: I don't like this name, but I can't come up with a better one
//TODO: add to this class new message types as needed
public class FeedbackMessageBuilder {
    private String title;
    private String description;
    private Activity activity;

    public FeedbackMessageBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public FeedbackMessageBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public FeedbackMessageBuilder withActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public void buildAsyncNeutralMessage() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(activity)
                                .setTitle(title)
                                .setMessage(description)
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
    }

    public void buildAsyncNeutralMessageAndStartActivity(Intent i) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(activity)
                                .setTitle(title)
                                .setMessage(description)
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        activity.startActivity(i);
                                        activity.finish();
                                    }
                                })
                                .create()
                                .show();
                    }
                });
            }
        }, 0);
    }

    public static void createDefaultNeutralError(String description, Activity activity, String operation) {
        new FeedbackMessageBuilder()
                .setTitle("Unable to " + operation)
                .setDescription(description)
                .withActivity(activity)
                .buildAsyncNeutralMessage();
    }

    public static void createServerInternalError(String operation, Activity activity) {
        createDefaultNeutralError("Server was unable to " + operation + "." + "\nPlease try again later.", activity, operation);
        Log.e(operation, "Server returned code 500.");
    }

    public static void createServerConnectionError(Exception e, String operation, Activity activity) {
        createDefaultNeutralError("Unable to connect with the backend server.\nPlease try again later.", activity, operation);
        Log.e(operation, "Request yielded error: " + e.getMessage());
    }

    public static void createParseError(Exception e, String operation, Activity activity) {
        createDefaultNeutralError("Unable to parse data during " + operation + "." + "\nPlease try again later.", activity, operation);
        Log.e(operation, "Create profile error. Unable to parse user input data: " + e.getMessage());
    }

    public static void createDefaultNeutralSuccessOnHttp200(String operation, Activity activity) {
        new FeedbackMessageBuilder()
                .setTitle("Successfully " + operation)
                .setDescription("Successfully " + operation)
                .withActivity(activity)
                .buildAsyncNeutralMessage();
        Log.i(operation, new Date().toString());
    }

    /**
     *
     * @param operation the operation in present tense
     * @param object the object action is performed on (user, event...)
     * @param activity activity context
     */
    public static void createDefaultNeutralNotFoundErrorOnHttp404(String operation, String object, Activity activity) {
        new FeedbackMessageBuilder()
                .setTitle("Not Found during " + operation)
                .setDescription("Unable to " + operation + ".\n" + object + " was not found.\n")
                .withActivity(activity)
                .buildAsyncNeutralMessage();
        Log.d(operation, new Date().toString());
    }

    /**
     *
     * @param operation the operation in present tense
     * @param activity activity context
     */
    public static void createDefaultNeutralInvalidErrorOnHttp422(String operation, Activity activity) {
        new FeedbackMessageBuilder()
                .setTitle("Invalid Request")
                .setDescription("Unable to " + operation)
                .withActivity(activity)
                .buildAsyncNeutralMessage();
        Log.d(operation, new Date().toString());
    }
}
