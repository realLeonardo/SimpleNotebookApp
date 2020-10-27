package com.leeeshuang.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WriteNoteActivity extends AppCompatActivity {

    private int sourceViewID;
    private String title;
    private String content;
    private TextView titleInput;
    private TextView contentInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_note);

        Intent intent = getIntent();

        // Default value is -1, means from create button
        sourceViewID = intent.getIntExtra(HomeActivity.EXTRA_ID, -1);
        title = intent.getStringExtra(HomeActivity.EXTRA_TITLE);
        content = intent.getStringExtra(HomeActivity.EXTRA_CONTENT);

        titleInput = findViewById(R.id.titleText);
        titleInput.setText(title);

        contentInput = findViewById(R.id.contentText);
        contentInput.setText(content);

        // NOTE: change button text dynamically
        Button confirmButton = findViewById(R.id.confirmButton);
        if(title == null || content == null){
            confirmButton.setText("Create Note");
        } else {
            confirmButton.setText("Confirm Changes");
        }
    }

    public void confirmButtonClickHandler(View view) {
        title = titleInput.getText().toString();
        content = contentInput.getText().toString();

        Intent intent = new Intent();

        intent.putExtra(HomeActivity.EXTRA_ID, sourceViewID);
        intent.putExtra(HomeActivity.EXTRA_TITLE, title);
        intent.putExtra(HomeActivity.EXTRA_CONTENT, content);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // NOTE: back button clicked
    public void backButtonClickHandler(View view) {
        finish();
    }
}