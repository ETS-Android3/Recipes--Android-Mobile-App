package com.example.recipes.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.recipes.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class RegisterActivity extends AppCompatActivity {

    AppCompatButton registerBtn;
    AppCompatEditText nameEt, emailEt, passwordEt;
    ProgressDialog progressDialog;
    AppCompatTextView loginTv;
    private FirebaseAuth mAuth;
    ImageView foodImages_iv;
    Timer timer;
    Handler handler = new Handler();
    int currentFoodImage =1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.createAccount);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        nameEt = findViewById(R.id.registerName_et);
        emailEt = findViewById(R.id.registerEmail_et);
        passwordEt = findViewById(R.id.registerPassword_et);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("cooking user");
        registerBtn = findViewById(R.id.register_btn);
        loginTv = findViewById(R.id.login_tv);
        foodImages_iv = findViewById(R.id.images1_iv);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString();
                String email = emailEt.getText().toString();
                String password = passwordEt.getText().toString();
                if(name.length() < 2){
                    nameEt.setError(getResources().getString(R.string.invalidName));
                    nameEt.setFocusable(true);
                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailEt.setError(getResources().getString(R.string.invalidEmail));
                    emailEt.setFocusable(true);
                }
                else if (password.length() < 6){
                    passwordEt.setError(getResources().getString(R.string.passwordMinLength));
                    emailEt.setFocusable(true);
                }
                else {
                    registerUser(name, email,password);
                }
            }
        });

        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser(String name, String email, String password) {

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();

                    FirebaseUser user = mAuth.getCurrentUser();
                    String email = user.getEmail();
                    String uid = user.getUid();

                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("uid", uid);
                    hashMap.put("name", name);
                    hashMap.put("email", email);
                    hashMap.put("speciality", "");
                    hashMap.put("profile", "");
                    hashMap.put("cover", "");
                    hashMap.put("onlineStatus", "online");
                    hashMap.put("typeTo", "none");

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = database.getReference("Users");
                    databaseReference.child(uid).setValue(hashMap);

                    Toast.makeText(RegisterActivity.this, R.string.userCreatedSuccessfully, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                    intent.putExtra("first_time", true);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(RegisterActivity.this, R.string.pleaseTryAgain, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        currentFoodImage++;
                        if (currentFoodImage==6){
                            currentFoodImage=1;
                        }
                        switch (currentFoodImage) {
                            case 1:
                                foodImages_iv.setImageResource(R.drawable.creme);
                                break;
                            case 2:
                                foodImages_iv.setImageResource(R.drawable.pizza);
                                break;
                            case 3:
                                foodImages_iv.setImageResource(R.drawable.pasta);
                                break;
                            case 4:
                                foodImages_iv.setImageResource(R.drawable.ktsitsot_orez_reutazar__i);
                                break;
                            case 5:
                                foodImages_iv.setImageResource(R.drawable.pancake);
                                break;
                        }
                    }
                });
            }
        }, 1000, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(timer!=null){
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}