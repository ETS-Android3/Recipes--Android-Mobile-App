package com.example.recipes.Activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.recipes.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddPostActivity extends AppCompatActivity {
    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<Uri> mCamera;
    Uri imageUri = null;
    DatabaseReference userDBReference;
    ProgressDialog progressDialog;

    private static final int CAMERA_REQ_CODE = 11;
    private static final int STORAGE_REQ_CODE = 21;
    String [] cameraPermissions;
    String [] storagePermissions;


    TextInputLayout recipeNameEt, ingredientsEt, instructionsEt;
    ImageView recipeIv;
    Button uploadBtn;
    String userName, userEmail, uid, userProfilePicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.addNewRecipe);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();
        actionBar.setSubtitle(userEmail);
        userDBReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDBReference.orderByChild("email").equalTo(userEmail);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    userName = ""+dataSnapshot.child("name").getValue();
                    userEmail = ""+dataSnapshot.child("email").getValue();
                    userProfilePicture = ""+dataSnapshot.child("profile").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recipeNameEt = findViewById(R.id.recipe_name_et);
        ingredientsEt = findViewById(R.id.recipe_Ingredients_et);
        instructionsEt = findViewById(R.id.recipe_Instructions_et);
        recipeIv = findViewById(R.id.recipe_iv);
        uploadBtn = findViewById(R.id.upload_btn);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        Picasso.get().load(uri).into(recipeIv);
                       // recipeIv.setImageURI(uri);
                        imageUri = uri;
                    }
                });

        mCamera = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    Picasso.get().load(imageUri).into(recipeIv);
                    //recipeIv.setImageURI(imageUri);
                }
            }
        });

        recipeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable recipeName = recipeNameEt.getEditText().getText();
                Editable ingredients = ingredientsEt.getEditText().getText();
                Editable instructions = instructionsEt.getEditText().getText();
                if (recipeName.length() == 0){
                    Toast.makeText(AddPostActivity.this, R.string.enterRecipeName, Toast.LENGTH_LONG).show();
                    return;
                }
                if (ingredients.length() == 0){
                    Toast.makeText(AddPostActivity.this, R.string.enterIngredients, Toast.LENGTH_LONG).show();
                    return;
                }
                if (instructions.length() == 0){
                    Toast.makeText(AddPostActivity.this, R.string.enterInstructions, Toast.LENGTH_LONG).show();
                    return;
                }
                if (imageUri == null){
                    uploadData(recipeName, ingredients, instructions, " ");
                }
                else{
                    uploadData(recipeName, ingredients, instructions, String.valueOf(imageUri));
                }
            }
        });
    }

    private  Boolean checkStoragePermissions(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQ_CODE);
    }

    private Boolean checkCameraPermissions(){
        Boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        Boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQ_CODE);
    }

    private void uploadData(Editable recipeName, Editable ingredients, Editable instructions, String imageUri) {
        progressDialog.setMessage(getResources().getString(R.string.yourRecipeIsBeingCooked));
        progressDialog.show();
        String time = String.valueOf(System.currentTimeMillis());
        String filePathWithName = "Posts/" + "post_" + time;
        if (!imageUri.equals(" ")) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathWithName);
            storageReference.putFile(Uri.parse(imageUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    String downloadUri = Objects.requireNonNull(uriTask.getResult()).toString();
                    if (uriTask.isSuccessful()) {
                        HashMap<Object, String> hashMap = new HashMap<>();
                        //post Data
                        hashMap.put("uid", uid);
                        hashMap.put("uName", userName);
                        hashMap.put("uEmail", userEmail);
                        hashMap.put("uProfilePicture", userProfilePicture);
                        hashMap.put("pId", time);
                        hashMap.put("pRecipeName", recipeName.toString());
                        hashMap.put("pIngredients", ingredients.toString());
                        hashMap.put("pInstructions", instructions.toString());
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pTime", time);
                        hashMap.put("pLikes", "0");
                        hashMap.put ("pComments", "0");

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        databaseReference.child(time).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, getResources().getString(R.string.yourRecipeIsNowReady), Toast.LENGTH_LONG).show();
                                resetViews();

                                prepareNotification(time+"", userName + " " +getResources().getString(R.string.addedNewRecipe), "", "PostNotification", "POST");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    finish();
                }
            });
        }
        else{
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            //post Data
            hashMap.put("uid", uid);
            hashMap.put("uName", userName);
            hashMap.put("uEmail", userEmail);
            hashMap.put("uProfilePicture", userProfilePicture);
            hashMap.put("pId", time);
            hashMap.put("pRecipeName", recipeName.toString());
            hashMap.put("pIngredients", ingredients.toString());
            hashMap.put("pInstructions", instructions.toString());
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", time);
            hashMap.put("pLikes", "0");
            hashMap.put ("pComments", "0");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
            databaseReference.child(time).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, getResources().getString(R.string.yourRecipeIsNowReady), Toast.LENGTH_LONG).show();
                    resetViews();
                    prepareNotification(time+"", userName + " " +getResources().getString(R.string.addedNewRecipe), "", "PostNotification", "POST");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    finish();
                }
            });
        }
    }

    private void resetViews() {
        recipeNameEt.getEditText().setText("");
        ingredientsEt.getEditText().setText("");
        instructionsEt.getEditText().setText("");
        recipeIv.setImageURI(null);
        imageUri = null;
    }

    private void showImagePickDialog() {
        String [] options = {getResources().getString(R.string.gallery), getResources().getString(R.string.camera)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.chooseImageFrom));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if(!checkStoragePermissions()){
                        requestStoragePermission();
                    }
                    else {
                        mGetContent.launch("image/*");
                    }
                }
                if (which == 1) {
                    if(!checkCameraPermissions()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "temp");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        mCamera.launch(imageUri);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQ_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this,  R.string.cameraAndStoragePermissionsAreBothNecessary, Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQ_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        mGetContent.launch("image/*");
                    } else {
                        Toast.makeText(this, R.string.storagePermissionsNecessary, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user !=null ){
            userEmail = user.getEmail();
            uid = user.getUid();
        }
        else {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic){

        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic;
        String NOTIFICATION_TITLE = title;
        String NOTIFICATION_MESSAGE = description;
        String NOTIFICATION_TYPE = notificationType;

        JSONObject notificationJO = new JSONObject();
        JSONObject notificationBodyJO = new JSONObject();

        try {
            notificationBodyJO.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJO.put("sender", uid);
            notificationBodyJO.put("pId", pId);
            notificationBodyJO.put("pTitle", NOTIFICATION_TITLE);
            //notificationBodyJO.put("pDescription", NOTIFICATION_MESSAGE);
            notificationJO.put("to", NOTIFICATION_TOPIC);
            notificationJO.put("data", notificationBodyJO);
        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage()+"", Toast.LENGTH_LONG).show();
        }
        sendPostNotification(notificationJO);
    }
    private void sendPostNotification(JSONObject notificationJo){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: "+response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddPostActivity.this, error.toString()+"", Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key= ); //put the key

                return headers;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
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
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
