package com.pratikgurung.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextInputEditText userNameText, emailText, passwordText, confirmPasswordText;
    CheckBox checkBox;
    Button registerButton;
    TextView loginNowTextbtn;
    ProgressBar progressBar;
    View dimBackground;

    // Check if user is signed in (non-null) and update UI accordingly.
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            /*if user is already signedin then redirecting to mainactivity*/
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //defining setWindowFlag method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

              mAuth = FirebaseAuth.getInstance(); /*Initialize firebase auth*/
        userNameText = findViewById(R.id.registrationUserName);
        emailText = findViewById(R.id.registrationEmail);
        passwordText = findViewById(R.id.registrationPassword);
        confirmPasswordText = findViewById(R.id.registrationCPassword);
        checkBox = findViewById(R.id.registrationCheckBox);
        registerButton = findViewById(R.id.registerbtn);
        loginNowTextbtn = findViewById(R.id.registerlgntv);
        progressBar = findViewById(R.id.registrationProgressBar);
        dimBackground = findViewById(R.id.registrationiDimBackground);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dimBackground.setVisibility(View.VISIBLE);/*setting dimbackground visibitlity to show*/
                progressBar.setVisibility(View.VISIBLE); /*Setting progress bar visibility to show*/
                String userName, email, password, cPassword;
                userName = userNameText.getText().toString();
                email = emailText.getText().toString();
                password = passwordText.getText().toString();
                cPassword = passwordText.getText().toString();

                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterActivity.this, "Enter User Name", Toast.LENGTH_SHORT).show();
                    dimBackground.setVisibility(View.GONE);/*setting dim background bar visibility gone*/
                    progressBar.setVisibility(View.GONE);/*setting progress bar visibility to hide*/
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RegisterActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    dimBackground.setVisibility(View.GONE);/*setting dim background bar visibility gone*/
                    progressBar.setVisibility(View.GONE);/*setting progress bar visibility to hide*/
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    dimBackground.setVisibility(View.GONE);/*setting dim background bar visibility gone*/
                    progressBar.setVisibility(View.GONE);/*setting progress bar visibility to hide*/
                    return;
                }
                if(TextUtils.isEmpty(cPassword)){
                    Toast.makeText(RegisterActivity.this, "Enter Confirm Password", Toast.LENGTH_SHORT).show();
                    dimBackground.setVisibility(View.GONE);/*setting dim background bar visibility gone*/
                    progressBar.setVisibility(View.GONE);/*setting progress bar visibility to hide*/
                    return;
                }

                if(!checkBox.isChecked()){
                    Toast.makeText(RegisterActivity.this, "You must accept the terms and services", Toast.LENGTH_SHORT).show();
                    dimBackground.setVisibility(View.GONE);/*setting dim background bar visibility gone*/
                    progressBar.setVisibility(View.GONE);/*setting progress bar visibility to hide*/
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                dimBackground.setVisibility(View.GONE);/*setting dim background bar visibility gone*/
                                progressBar.setVisibility(View.GONE);/*setting progress bar visibility to hide*/
                                if (task.isSuccessful()) {

                                    FirebaseUser newUser = mAuth.getCurrentUser();
                                    if(newUser != null){
                                        //create a new user document in firebase
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        DocumentReference userRef = db.collection("users").document(newUser.getUid());

                                        //Preparing user datas to store
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("Email", email);
                                        userData.put("User Name", userName);

                                        //set the data in firebase document
                                        userRef.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(RegisterActivity.this, "Account Created Successfully.",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(intent);
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegisterActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }


                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

        loginNowTextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

}