package com.example.recipes.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipes.Adapters.CommentsAdapter;
import com.example.recipes.Models.ModelComment;
import com.example.recipes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    String otherUserUid, userUid, userEmail, userName, userProfilePicture, postId, pLikes, otherUserProfilePicture, otherUserName, pImage;
    TextView uNameTv, pTimeTv, pRecipeNameTv, pIngredientsTv, pInstructionsTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn, sendBtn;
    Button likeBtn;
    ImageView uPictureIv, pImageIv, uCommentPictureIv;
    LinearLayout ProfileLayout;
    EditText commentEt;
    ProgressDialog progressDialog;
    boolean commentInProcess = false;
    boolean likeInProcess = false;
    RecyclerView recyclerView;
    List<ModelComment> commentsList;
    CommentsAdapter commentsAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.recipeDetail);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        uPictureIv = findViewById(R.id.profile_pic_IV);
        pImageIv = findViewById(R.id.recipe_iv);
        uNameTv = findViewById(R.id.uName_Tv);
        pTimeTv = findViewById(R.id.time_tv);
        pRecipeNameTv = findViewById(R.id.recipeName_TV);
        pIngredientsTv = findViewById(R.id.ingredients_TV);
        pInstructionsTv = findViewById(R.id.instructions_TV);
        pLikesTv = findViewById(R.id.likes_TV);
        moreBtn = findViewById(R.id.more_option_btn);
        sendBtn = findViewById(R.id.send_Btn);
        likeBtn = findViewById(R.id.like_btn);
        uCommentPictureIv = findViewById(R.id.profile_pic_comment_IV);
        commentEt = findViewById(R.id.comment_ET);
        ProfileLayout = findViewById(R.id.profile_layout);
        pCommentsTv = findViewById(R.id.comments_TV);
        recyclerView = findViewById(R.id.recycler_view_comments);

        loadPostData();
        checkUserStatus();
        loadUserData();
        setLikes();
        loadComments();

        actionBar.setSubtitle(userName);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment();
            }
        });
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
    }

    private void addNotifications(String hisUid, String pId, String notification){
        String time = System.currentTimeMillis()+"";
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("time", time);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", userUid);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(hisUid).child("Notifications").child(time).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        commentsList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelComment modelComment = dataSnapshot.getValue(ModelComment.class);
                    commentsList.add(modelComment);

                    commentsAdapter = new CommentsAdapter(getApplicationContext(), commentsList, userUid, postId);
                    recyclerView.setAdapter(commentsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);
        if (otherUserUid.equals(userUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.deleteRecipe);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0) {
                    deletePost();
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void deletePost() {
        if (pImage.equals("noImage")){
            deletePoseWithoutImage();
        }
        else {
            deletePostWithImage();
        }
    }

    private void deletePostWithImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.deleting));
        StorageReference imageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            dataSnapshot.getRef().removeValue();
                            Toast.makeText(PostDetailActivity.this, R.string.recipeDeletedSuccessfully, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deletePoseWithoutImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.deleting));

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    dataSnapshot.getRef().removeValue();
                    Toast.makeText(PostDetailActivity.this, R.string.recipeDeletedSuccessfully, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        DatabaseReference likesReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(userUid)){
                    //change drawable of like button to liked, and text from like to liked
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    likeBtn.setText(R.string.liked);
                }
                else{
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_icon, 0, 0, 0);
                    likeBtn.setText(R.string.like);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {

        likeInProcess = true;
        DatabaseReference likesReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (likeInProcess){
                    if (snapshot.child(postId).hasChild(userUid)){
                        postsReference.child(postId).child("pLikes").setValue((Integer.parseInt(pLikes)-1)+"");
                        likesReference.child(postId).child(userUid).removeValue();
                        likeInProcess=false;

                    }
                    else{
                        postsReference.child(postId).child("pLikes").setValue((Integer.parseInt(pLikes)+1)+"");
                        likesReference.child(postId).child(userUid).setValue("Liked");
                        likeInProcess=false;
                        if (!(otherUserUid.equals(userUid))) {
                            addNotifications(otherUserUid, postId, getResources().getString(R.string.likedYourRecipe));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addComment() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.addingComment));
        String comment = commentEt.getText().toString().trim();
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, R.string.typeYourComment, Toast.LENGTH_LONG).show();
            return;
        }
        String time = String.valueOf(System.currentTimeMillis());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", comment);
        hashMap.put("uid", userUid);
        hashMap.put("uProfilePicture", userProfilePicture);
        hashMap.put("cId", time);
        hashMap.put("uName", userName);

        databaseReference.child(time).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, R.string.commentAdded, Toast.LENGTH_LONG).show();
                commentEt.setText("");
                updateCommentCount();
                if (!(otherUserUid.equals(userUid))) {
                    addNotifications(otherUserUid, postId, getResources().getString(R.string.commentedOnYourRecipe));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        finish();
    }

    private void updateCommentCount() {
        commentInProcess = true;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (commentInProcess){
                    String comments = "" + snapshot.child("pComments").getValue();
                    int newCommentCount = Integer.parseInt(comments) + 1;
                    databaseReference.child("pComments").setValue(""+newCommentCount);
                    commentInProcess = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUserData() {
        Query query = FirebaseDatabase.getInstance().getReference("Users");
        query.orderByChild("uid").equalTo(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    userName = ""+dataSnapshot.child("name").getValue();
                    userProfilePicture = ""+dataSnapshot.child("profile").getValue();

                    try {
                        Picasso.get().load(userProfilePicture).placeholder(R.drawable.ic_profile_default).into(uCommentPictureIv);
                    }catch (Exception e){

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = databaseReference.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String pRecipeName = ""+dataSnapshot.child("pRecipeName").getValue();
                    String pIngredients = ""+dataSnapshot.child("pIngredients").getValue();
                    String pInstructions = ""+dataSnapshot.child("pInstructions").getValue();
                    pLikes = ""+dataSnapshot.child("pLikes").getValue();
                    String pTime = ""+dataSnapshot.child("pTime").getValue();
                    otherUserProfilePicture = ""+dataSnapshot.child("uProfilePicture").getValue();
                    otherUserUid = ""+dataSnapshot.child("uid").getValue();
                    otherUserName = ""+dataSnapshot.child("uName").getValue();
                    pImage = ""+dataSnapshot.child("pImage").getValue();
                    String commentsCount = ""+dataSnapshot.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTime));

                    pRecipeNameTv.setText(pRecipeName);
                    // pTimeTv.setText(time);
                    pIngredientsTv.setText(pIngredients);
                    pInstructionsTv.setText(pInstructions);
                    pLikesTv.setText(pLikes +" " + getResources().getString(R.string.likes));
                    uNameTv.setText(otherUserName);
                    pCommentsTv.setText(commentsCount +" " + getResources().getString(R.string.comments));

                    if (pImage.equals("noImage")){
                        pImageIv.setVisibility(View.GONE);
                    }
                    else {
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        } catch (Exception e) {

                        }
                    }
                    try {
                        Picasso.get().load(otherUserProfilePicture).placeholder(R.drawable.ic_profile_default).into(uPictureIv);
                    }catch (Exception e) {
                       // Picasso.get().load(R.drawable.ic_profile_default).into(uPictureIv);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){ //the user is signed in
            userEmail = user.getEmail();
            userUid = user.getUid();

        }
        else{
            //the user not signed in
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}