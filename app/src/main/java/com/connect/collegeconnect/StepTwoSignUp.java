package com.connect.collegeconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.connect.collegeconnect.datamodels.SaveSharedPreference;
import com.connect.collegeconnect.datamodels.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class StepTwoSignUp extends AppCompatActivity {

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_PREV = "previous";
    private static String TAG = "Step Two Sign Up";
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private TextInputLayout rollno, branchanme, collegeName;
    private FirebaseAuth mAuth;
    private Button signup;
    private Spinner collegeSpinner;
    private String receivedPRev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_two_sign_up);

        mAuth = FirebaseAuth.getInstance();
        rollno = findViewById(R.id.textuser);
        branchanme = findViewById(R.id.textbranch);
        collegeName = findViewById(R.id.otherCollege);
        signup = findViewById(R.id.stepTwoButton);
        collegeSpinner = findViewById(R.id.collegeSpinner);

        final ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Other");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(StepTwoSignUp.this, android.R.layout.simple_spinner_item, arrayList);

        collegeSpinner.setAdapter(spinnerArrayAdapter);

        databaseReference = firebaseDatabase.getReference("Colleges");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> arrayList2 = (ArrayList<String>) dataSnapshot.getValue();
                arrayList.addAll(arrayList2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Intent intent = getIntent();
        final String receivedName = intent.getStringExtra(EXTRA_NAME);
        final String receivedEmail = intent.getStringExtra(EXTRA_EMAIL);
        final String receivedPassword = intent.getStringExtra(EXTRA_PASSWORD);
        receivedPRev = intent.getStringExtra(EXTRA_PREV);

        collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    collegeName.setVisibility(View.VISIBLE);
                else
                    collegeName.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null)
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                final String roll = rollno.getEditText().getText().toString();
                final String branch = branchanme.getEditText().getText().toString();

                if (collegeSpinner.getSelectedItem().toString().equals("Other")) {
                    if (collegeName.getEditText().getText().toString().isEmpty())
                        collegeName.setError("Enter College Name");
                    else {
                        if (roll.isEmpty() && branch.isEmpty()) {
                            rollno.setError("Enter Roll Number");
                            branchanme.setError("Enter Branch Name");
                        } else if (branch.isEmpty()) {
                            branchanme.setError("Enter Branch Name");
                        } else if (roll.isEmpty()) {
                            rollno.setError("Enter Roll Number");
                        } else {

                            databaseReference = firebaseDatabase.getReference("CollegesInputFromUsers");
                            databaseReference.setValue(collegeSpinner.getSelectedItem().toString());
                            String college;
                            if (collegeSpinner.getSelectedItem().toString().equals("Other")) {
                                college = collegeName.getEditText().getText().toString();
                            } else
                                college = collegeSpinner.getSelectedItem().toString();

                            if (receivedPRev == null) {//google

                                User.addUser(roll, mAuth.getCurrentUser().getEmail(), mAuth.getCurrentUser().getDisplayName(), null, branch, college);
                                SaveSharedPreference.setUserName(getApplicationContext(), mAuth.getCurrentUser().getEmail());
                                startActivity(new Intent(getApplicationContext(), navigation.class));
                                finish();
                            } else {//email

                                User.addUser(roll, receivedEmail, receivedName, receivedPassword, branch, college);
                                mAuth.createUserWithEmailAndPassword(receivedEmail, receivedPassword).addOnCompleteListener(StepTwoSignUp.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                        Toast.makeText(StepTwoSignUp.this, "Registered! Email Verification sent", Toast.LENGTH_LONG).show();
                                                    else
                                                        Toast.makeText(StepTwoSignUp.this, task.getException().getMessage(),
                                                                Toast.LENGTH_SHORT);
                                                }
                                            });
                                            Log.d(TAG, "createUserWithEmail:success");
                                            Intent intent = new Intent(StepTwoSignUp.this, MainActivity.class);
                                            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }

                collegeName.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        collegeName.setError(null);
                    }
                });

                rollno.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        rollno.setError(null);
                    }
                });

                branchanme.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        branchanme.setError(null);
                    }
                });
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (receivedPRev != null)
            super.onBackPressed();
        else
            Toast.makeText(this, "Please fill in the details and click submit!", Toast.LENGTH_SHORT).show();
    }
}

