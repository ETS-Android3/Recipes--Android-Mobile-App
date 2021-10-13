package com.example.recipes.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipes.Adapters.PostsAdapter;
import com.example.recipes.Models.ModelPost;
import com.example.recipes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class OtherProfileActivity extends AppCompatActivity {
    RecyclerView postsRecyclerView;
    List<ModelPost> postsList;
    PostsAdapter postsAdapter;
    String uid;
    FirebaseAuth firebaseAuth;
    ImageView profileIv, coverIv;
    TextView nameTv, emailTv, specialityTv;
    LinearLayout profileLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

//        profileLinearLayout = findViewById(R.id.profile_layout);
//        profileLinearLayout.setClickable(false);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Recipes");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        profileIv = findViewById(R.id.profile_iv);
        coverIv = findViewById(R.id.cover_iv);
        nameTv = findViewById(R.id.name_tv);
        emailTv = findViewById(R.id.email_tv);
        specialityTv = findViewById(R.id.speciality_tv);
        postsRecyclerView = findViewById(R.id.posts_recycler_view);
        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent= getIntent();
        uid = intent.getStringExtra("uid");


        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()){
                    String name = "" + data.child("name").getValue();
                    String email = "" + data.child("email").getValue();
                    String speciality = "" + data.child("speciality").getValue();
                    String profilePic = "" + data.child("profile").getValue();
                    String coverPic = "" + data.child("cover").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    specialityTv.setText(speciality);

                    try {
                        Picasso.get().load(profilePic).placeholder(R.drawable.ic_profile_default).into(profileIv);
                    }
                    catch (Exception e){
                     //   Picasso.get().load(R.drawable.ic_profile_default).into(profileIv);
                    }

                    try {
                        Picasso.get().load(coverPic).into(coverIv);
                    }
                    catch (Exception e){
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        postsList = new ArrayList<>();
        checkUserStatus();
        loadOtherUserPosts();

    }

    private void loadOtherUserPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelPost myPost = dataSnapshot.getValue(ModelPost.class);
                    postsList.add(myPost);
                    postsAdapter = new PostsAdapter(OtherProfileActivity.this, postsList);
                    postsRecyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OtherProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }

    private void searchOtherUserPosts(String searchQuery){
        LinearLayoutManager layoutManager = new LinearLayoutManager(OtherProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelPost myPost = dataSnapshot.getValue(ModelPost.class);
                    if (myPost.getpRecipeName().toLowerCase().contains(searchQuery.toLowerCase())){
                        postsList.add(myPost);

                    }

                    postsAdapter = new PostsAdapter(OtherProfileActivity.this, postsList);
                    postsRecyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OtherProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchOtherUserPosts(query);
                }
                else{
                    loadOtherUserPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    searchOtherUserPosts(newText);
                }
                else{
                    loadOtherUserPosts();
                }
                return false;
            }
        });

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

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null ){
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


}