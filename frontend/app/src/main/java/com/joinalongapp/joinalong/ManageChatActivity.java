package com.joinalongapp.joinalong;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.DrawableMarginSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.chip.ChipDrawable;


public class ManageChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_chat);

        // TODO: Whether this is create or edit, it will be sent from previous activity using bundles
        // Check if bundle is create or edit
        // If edit, then details should be sent in bundle
        // Else create,

        EditText title = findViewById(R.id.manageChatEditTextTitle);
        EditText description = findViewById(R.id.manageChatEditTextDescription);

        ImageButton cancelButton = findViewById(R.id.manageChatCancelButton);
        Button submitButton = findViewById(R.id.submitManageChatButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}