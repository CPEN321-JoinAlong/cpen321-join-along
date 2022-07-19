package com.joinalongapp;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

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
}
