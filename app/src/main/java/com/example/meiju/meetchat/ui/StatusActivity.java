package com.example.meiju.meetchat.ui;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.meiju.meetchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mSaveButton;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        mToolbar = (Toolbar) findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String nameValue = getIntent().getStringExtra("name_value");

        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mSaveButton = (Button) findViewById(R.id.status_save_button);

        mStatus.getEditText().setText(nameValue);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving");
                mProgress.setMessage("Please wait while we save the changes");
                mProgress.show();

                String status = mStatus.getEditText().getText().toString();

                mStatusDatabase.child("name").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgress.dismiss();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });
    }
}
