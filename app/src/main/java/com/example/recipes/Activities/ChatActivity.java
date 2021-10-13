package com.example.recipes.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.recipes.Adapters.ChatAdapter;
import com.example.recipes.Models.ModelChat;
import com.example.recipes.Models.ModelUsers;
import com.example.recipes.R;
import com.example.recipes.notifications.Data;
import com.example.recipes.notifications.Sender;
import com.example.recipes.notifications.Token;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {
    final int LOCATION_PERMISSION_REQ = 1;
    FusedLocationProviderClient fusedLocationProviderClient;
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, statusTv;
    EditText messageEt;
    ImageButton sendBtn, locationBtn;
    String latitude, longitude;

    String otherUserUid;
    String selfUid;
    String otherUserImage;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDatabaseRef;
    ValueEventListener messageListener;
    DatabaseReference userRefForMessageStatus;

    List<ModelChat> chatList;
    ChatAdapter chatAdapter;

    private boolean notify = false;
    private RequestQueue requestQueue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_rv);
        profileIv = findViewById(R.id.user_profile_IV);
        nameTv = findViewById(R.id.name_tv);
        statusTv = findViewById(R.id.status_tv);
        messageEt = findViewById(R.id.message_et);
        sendBtn = findViewById(R.id.send_btn);
        locationBtn = findViewById(R.id.location_btn);
        requestQueue = Volley.newRequestQueue(getApplicationContext());


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        otherUserUid = intent.getStringExtra("hisUid");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDatabaseRef = firebaseDatabase.getReference("Users");

        Query userQuery = usersDatabaseRef.orderByChild("uid").equalTo(otherUserUid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    String name = ds.child("name").getValue() + "";
                    otherUserImage = ds.child("profile").getValue() + "";
                    String typingStatus = ds.child("typeTo").getValue() + "";
                    nameTv.setText(name);
                    if (typingStatus.equals(selfUid)){
                        statusTv.setText(R.string.typing);
                    }
                    else {
                        String onlineStatus = ds.child("onlineStatus").getValue() + "";
                        if(onlineStatus.equals("online")){
                            statusTv.setText(getResources().getString(R.string.online));
                        }
                        else {
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            statusTv.setText(getResources().getString(R.string.lastOnline) +" "+ dateTime);
                        }
                    }

                    try {
                        Picasso.get().load(otherUserImage).placeholder(R.drawable.ic_profile_default).into(profileIv);

                    }catch (Exception e){
                      // Picasso.get().load(R.drawable.ic_add_image).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermission();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = messageEt.getText().toString();
                if (message.length()  == 0){
                    Toast.makeText(ChatActivity.this, R.string.emptyMessageError, Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMessage(message);
                }
                messageEt.setText("");

            }
        });

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length() == 0){
                    checktTypingStatus("none");
                }
                else {
                    checktTypingStatus(otherUserUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessage();
        messageStatus();
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT>=23){
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQ);
            }
            else{
                getLocation();
            }
        }else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==LOCATION_PERMISSION_REQ){
            if (grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, R.string.locationPermissionNecessary, Toast.LENGTH_LONG).show();
            }
            else{
                getLocation();
            }
        }
        else {
            getLocation();
        }
    }

    private void getLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location lastLocation = locationResult.getLastLocation();
                //messageEt.setText(lastLocation.getLatitude()+","+ lastLocation.getLongitude());
                latitude = ""+lastLocation.getLatitude();
                longitude = ""+ lastLocation.getLongitude();
                messageEt.setText(getResources().getString(R.string.comeEatWithMe)+ "\n"+"https://maps.google.com/maps?daddr=" + latitude + "," + longitude);

            }
        };
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

//        request.setInterval(1000);
//        request.setFastestInterval(500);
        if (Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(request, callback, null);
        }
        else if (Build.VERSION.SDK_INT<22){
            fusedLocationProviderClient.requestLocationUpdates(request,callback, null);
        }

    }

    private void messageStatus() {
        userRefForMessageStatus = FirebaseDatabase.getInstance().getReference("Chats");
        messageListener = userRefForMessageStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(selfUid) && chat.getSender().equals(otherUserUid)){
                        HashMap<String, Object> messageStatusHashMap = new HashMap<>();
                        messageStatusHashMap.put("messageStatus", true);
                        ds.getRef().updateChildren(messageStatusHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(selfUid) && chat.getSender().equals(otherUserUid)
                            || chat.getReceiver().equals(otherUserUid) && chat.getSender().equals(selfUid)){
                        chatList.add(chat);
                    }

                    chatAdapter = new ChatAdapter(ChatActivity.this, chatList, otherUserImage);
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", selfUid);
        hashMap.put("receiver", otherUserUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("messageStatus", false);
        databaseReference.child("Chats").push().setValue(hashMap);

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Users").child(selfUid);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers user = snapshot.getValue(ModelUsers.class);
                if (notify){
                    sendNotification(otherUserUid, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference chatReference1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(selfUid).child(otherUserUid);
        chatReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatReference1.child("id").setValue(otherUserUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference chatReference2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(otherUserUid).child(selfUid);
        chatReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatReference2.child("id").setValue(selfUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(String otherUserUid, String name, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(otherUserUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(selfUid+"", name+": "+message,"Recipes- New Message", otherUserUid+"","ChatNotification" ,R.drawable.recipe_first_page );

                    Sender sender = new Sender(data, token.getToken());

                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("JSON_RESPONSE", "onResponse: " + response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: " + error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String , String > headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAApTgiLCg:APA91bG2bH_JfnwO3dOgdt-m7tbqduOJTo7O0lMgP5J4cJHSOF5UsTBTRkyDF6O0gNW5hiVAr6xwVvENVBEKyVtqQ0vJVuJRElzFbr3UE44JgN4KqK6Qd52qPmsgTY5tXLUMZrUakFu7");
                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user !=null ){
            selfUid = user.getUid();
        }
        else {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(selfUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        databaseReference.updateChildren(hashMap);
    }

    private void checktTypingStatus(String typing){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(selfUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typeTo", typing);

        databaseReference.updateChildren(hashMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);



        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        userRefForMessageStatus.removeEventListener(messageListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }
}