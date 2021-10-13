package com.example.recipes.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipes.Adapters.PostsAdapter;
import com.example.recipes.Activities.AddPostActivity;
import com.example.recipes.Models.ModelPost;
import com.example.recipes.R;
import com.example.recipes.Activities.RegisterActivity;
import com.example.recipes.Activities.SettingsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference databaseReference;


    StorageReference storageReference;
    String storagePath = "Users_Profile&Cover_Imgs/";
    ImageView profileIv, coverIv;
    TextView nameTv, emailTv, specialityTv;

    FloatingActionButton floatingActionButton;
    RecyclerView postsRecyclerView;

    ProgressDialog progressDialog;

    private static final int CAMERA_REQ_CODE = 1;
    private static final int CAMERA_PICK_IMAGE_REQ = 2;

    private static final int STORAGE_REQ_CODE = 3;
    private static final int GALLERY_PICK_IMAGE_REQ = 4;

    List<ModelPost> postsList;
    PostsAdapter postsAdapter;
    String uid;

    String cameraPermissions[];
    String storagePermissions[];

    Uri pictureUri;
    String profileOrCoverPhoto;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        profileIv = view.findViewById(R.id.profile_user_iv);
        coverIv = view.findViewById(R.id.cover_iv);
        nameTv = view.findViewById(R.id.name_tv);
        emailTv = view.findViewById(R.id.email_tv);
        specialityTv = view.findViewById(R.id.speciality_tv);
        floatingActionButton = view.findViewById(R.id.floating_action_btn);
        postsRecyclerView = view.findViewById(R.id.posts_recycler_view2);


        progressDialog = new ProgressDialog(getActivity());

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
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
                       // Picasso.get().load(R.drawable.ic_profile_default).into(profileIv);
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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        postsList = new ArrayList<>();

        checkUserStatus();
        loadPosts();


        // Inflate the layout for this fragment
        return view;
    }

    private void loadPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    postsAdapter = new PostsAdapter(getActivity(), postsList);
                    postsRecyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }

    private void searchMyPosts(String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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

                    postsAdapter = new PostsAdapter(getActivity(), postsList);
                    postsRecyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }

    private boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    //dep
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(getActivity(),storagePermissions, STORAGE_REQ_CODE);
    }

    private boolean checkCameraPermission(){
        boolean res = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean res1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return res && res1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(getActivity(),cameraPermissions, CAMERA_REQ_CODE);
    }
    //dep

    private void showEditProfileDialog() {
        String editOptions[] = {getResources().getString(R.string.editProfilePicture) , getResources().getString(R.string.editCoverPicture), getResources().getString(R.string.editName), getResources().getString(R.string.editSpeciality),getResources().getString(R.string.changePassword)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.profileEdit);
        builder.setItems(editOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:{
                        progressDialog.setMessage(getResources().getString(R.string.editProfilePicture));
                        profileOrCoverPhoto = "profile";
                        pickProfilePicDialog();
                        break;
                    }
                    case 1:{
                        progressDialog.setMessage(getResources().getString(R.string.editCoverPicture));
                        profileOrCoverPhoto = "cover";
                        pickProfilePicDialog();
                        break;
                    }
                    case 2:{
                        progressDialog.setMessage(getResources().getString(R.string.editName));
                        showNameSpecialityUpdateDialog("name");
                        break;
                    }
                    case 3:{
                        progressDialog.setMessage(getResources().getString(R.string.editSpeciality));
                        showNameSpecialityUpdateDialog("speciality");
                        break;
                    }
                    case 4:{
                        progressDialog.setMessage(getResources().getString(R.string.changePassword));
                        showChangePasswordDialog();
                        break;
                    }
                }
            }
        });
        builder.create().show();

    }

    private void showChangePasswordDialog() {

        TextInputLayout oldPassword_ET, newPassword_ET;
        Button updatePassword_btn;
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.update_password_dialog, null);

        oldPassword_ET = view.findViewById(R.id.old_password_ET);
        newPassword_ET = view.findViewById(R.id.new_password_ET);
        updatePassword_btn = view.findViewById(R.id.update_password_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


        updatePassword_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable oldPassword = oldPassword_ET.getEditText().getText();
                Editable newPassword = newPassword_ET.getEditText().getText();
                if (newPassword.toString().contains(" ")){
                    Toast.makeText(getActivity(), R.string.passwordCantContainSpaces, Toast.LENGTH_LONG).show();
                    return;
                }
                if (oldPassword.length()==0){
                    Toast.makeText(getActivity(), R.string.enterYourOldPassword, Toast.LENGTH_LONG).show();
                    return;
                }
                if (newPassword.length()<6){
                    Toast.makeText(getActivity(), R.string.passwordMinLength, Toast.LENGTH_LONG).show();
                    return;
                }
                alertDialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });

    }

    private void updatePassword(Editable oldPassword, Editable newPassword) {
        progressDialog.show();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword.toString());
        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                user.updatePassword(newPassword.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), R.string.passwordUpdatedSuccessfully, Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), e.getMessage()+"", Toast.LENGTH_LONG).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage()+"", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showNameSpecialityUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(key.equals("speciality")) {
            builder.setTitle(getResources().getString(R.string.update) + " " + getResources().getString(R.string.speciality));
        }
        else{
            builder.setTitle(getResources().getString(R.string.update) + " " + getResources().getString(R.string.name));
        }
        LinearLayoutCompat layoutCompat = new LinearLayoutCompat(getActivity());
        layoutCompat.setOrientation(LinearLayoutCompat.VERTICAL);
        layoutCompat.setPadding(10,10,10,10);
        EditText editText = new EditText(getActivity());
        if(key.equals("speciality")) {
            editText.setHint(getResources().getString(R.string.enter) + " " + getResources().getString(R.string.speciality));
        }
        else{
            editText.setHint(getResources().getString(R.string.enter) + " " + getResources().getString(R.string.name));

        }
        layoutCompat.addView(editText);
        builder.setView(layoutCompat);
        builder.setPositiveButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString();
                if(value.length() > 0){
                    progressDialog.show();
                    HashMap<String, Object> res = new HashMap<>();
                    res.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(res).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), R.string.updated, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), e.getMessage()+ "", Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (key.equals("name")) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = databaseReference.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String child = dataSnapshot.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for ( DataSnapshot dataSnapshot:snapshot.getChildren()){
                                    String child = dataSnapshot.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments")){
                                        String child1 = ""+ dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                                                    String child = dataSnapshot.getKey();
                                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                else {
                    Toast.makeText(getActivity(), R.string.pleaseEnter + key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();
            }
        });
        builder.create().show();

    }

    private void pickProfilePicDialog(){
        String[] editOptions = {"Camera" , "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pickProfilePictureFrom);
        builder.setItems(editOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:{
                        if(!checkCameraPermission()){
                            requestCameraPermission();
                        }
                        else {
                            pickFromCamera();
                        }
                        break;
                    }
                    case 1:{
                        if(!checkStoragePermission()){
                            requestStoragePermission();
                        }
                        else {
                            pickFromGallery();
                        }
                        break;
                    }
                }
            }
        });
        builder.create().show();
    }

    //dep
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQ_CODE:{
                if(grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(), R.string.pleaseEnableCameraAndStoragePermissions, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQ_CODE:{
                if(grantResults.length > 0) {
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getActivity(), R.string.pleaseEnableStoragePermission, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_PICK_IMAGE_REQ){
                pictureUri = data.getData();
                uploadPicture(pictureUri);
            }

            if (requestCode == CAMERA_PICK_IMAGE_REQ){
                uploadPicture(pictureUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadPicture(Uri pictureUri) {
        progressDialog.show();
        String filePathAndName = storagePath + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference1 = storageReference.child(filePathAndName);

        storageReference1.putFile(pictureUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();
                if(uriTask.isSuccessful()){
                    HashMap<String, Object> res = new HashMap<>();
                    res.put(profileOrCoverPhoto, downloadUri.toString());
                    databaseReference.child(user.getUid()).updateChildren(res).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), R.string.pictureUpdated, Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), R.string.errorUpdating, Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (profileOrCoverPhoto.equals("profile")) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = databaseReference.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String postId = dataSnapshot.getKey();
                                    snapshot.getRef().child(postId).child("uProfilePicture").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for ( DataSnapshot dataSnapshot:snapshot.getChildren()){
                                    String child = dataSnapshot.getKey();
                                    if (snapshot.child(child).hasChild("Comments")){
                                        String child1 = ""+ snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot1: snapshot.getChildren()){
                                                    String commentId = dataSnapshot1.getKey();
                                                    snapshot.getRef().child(commentId).child("uProfilePicture").setValue(downloadUri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), R.string.unknownError, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "temp");
        values.put(MediaStore.Images.Media.DESCRIPTION, "temp");
        pictureUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, CAMERA_PICK_IMAGE_REQ);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_PICK_IMAGE_REQ);
    }
    //dep

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user !=null ){
            uid = user.getUid();
        }
        else {
            Intent intent = new Intent(getActivity(), RegisterActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchMyPosts(query);
                }
                else{
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    searchMyPosts(newText);
                }
                else{
                    loadPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        else if (id == R.id.action_settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}