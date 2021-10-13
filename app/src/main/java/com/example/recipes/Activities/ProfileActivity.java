package com.example.recipes.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.recipes.Fragments.ChatListFragment;
import com.example.recipes.Fragments.HomeFragment;
import com.example.recipes.Fragments.NotificationsFragment;
import com.example.recipes.Fragments.ProfileFragment;
import com.example.recipes.Fragments.UsersFragment;
import com.example.recipes.LocationSwitchReceiver;
import com.example.recipes.R;
import com.example.recipes.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    String uid;
    FirebaseUser user;
    LocationSwitchReceiver locationSwitchReceiver = new LocationSwitchReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.profile);
        firebaseAuth = FirebaseAuth.getInstance();
        BottomNavigationView navigationView = findViewById(R.id.nav_profile);
        navigationView.setOnItemSelectedListener(selectedListener);

        //default
        boolean isNewUser = getIntent().getBooleanExtra("first_time", false);
        if (isNewUser){
            navigationView.setSelectedItemId(R.id.nav_profile);
        }
        else {
            navigationView.setSelectedItemId(R.id.nav_home);
        }
        checkUserStatus();

        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, R.string.FCMRegistrationTokenFailed, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (user != null){
                    updateToken(task.getResult());
                }
            }
        });


    }

    public void updateToken(String token){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token newToken = new Token(token);
        databaseReference.child(uid).setValue(newToken);
    }

    private NavigationBarView.OnItemSelectedListener selectedListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.nav_home:
                    actionBar.setTitle(R.string.home);
                    HomeFragment fragmentHome = new HomeFragment();
                    FragmentTransaction ftlHome = getSupportFragmentManager().beginTransaction();
                    ftlHome.replace(R.id.content, fragmentHome, "");
                    ftlHome.commit();
                    return true;

                case R.id.nav_profile:
                    actionBar.setTitle(R.string.profile);
                    ProfileFragment fragmentProfile = new ProfileFragment();
                    FragmentTransaction ftlProfile = getSupportFragmentManager().beginTransaction();
                    ftlProfile.replace(R.id.content, fragmentProfile, "");
                    ftlProfile.commit();
                    return true;

                case R.id.nav_users:
                    actionBar.setTitle(R.string.users);
                    UsersFragment fragmentUsers = new UsersFragment();
                    FragmentTransaction ftlUsers = getSupportFragmentManager().beginTransaction();
                    ftlUsers.replace(R.id.content, fragmentUsers, "");
                    ftlUsers.commit();
                    return true;

                case R.id.nav_chat:
                    actionBar.setTitle(R.string.chat);
                    ChatListFragment fragmentChat = new ChatListFragment();
                    FragmentTransaction ftlChat = getSupportFragmentManager().beginTransaction();
                    ftlChat.replace(R.id.content, fragmentChat, "");
                    ftlChat.commit();
                    return true;

                case R.id.nav_notifications:
                    actionBar.setTitle(R.string.notification);
                    NotificationsFragment fragmentNotifications = new NotificationsFragment();
                    FragmentTransaction ftlNotifications = getSupportFragmentManager().beginTransaction();
                    ftlNotifications.replace(R.id.content, fragmentNotifications, "");
                    ftlNotifications.commit();
                    return true;
            }
            return false;
        }
    };

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user !=null ){
            uid = user.getUid();
            SharedPreferences sharedPreferences = getSharedPreferences("USER_SP", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Current_USERID", uid);
            editor.apply();
        }
        else {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(locationSwitchReceiver, filter);
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(locationSwitchReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}