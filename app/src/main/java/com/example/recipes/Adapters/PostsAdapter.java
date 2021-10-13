package com.example.recipes.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Models.ModelPost;
import com.example.recipes.Activities.OtherProfileActivity;
import com.example.recipes.Activities.PostDetailActivity;
import com.example.recipes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.Holder>{

    Context context;
    List<ModelPost> postsList;
    String myUid;
    private DatabaseReference likesReference;
    private DatabaseReference postsReference;
    boolean processLike = false;


    public PostsAdapter(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postsList = postList;
        myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        likesReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsReference = FirebaseDatabase.getInstance().getReference().child("Posts");

    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.posts_row, parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        //get the data
        String uid = postsList.get(position).getUid();
        String uName = postsList.get(position).getuName();
        String uProfilePicture = postsList.get(position).getuProfilePicture();
        String pId = postsList.get(position).getpId();
        String pRecipeName = postsList.get(position).getpRecipeName();
        String pIngredients = postsList.get(position).getpIngredients();
        String pInstructions = postsList.get(position).getpInstructions();
        String pImage = postsList.get(position).getpImage();
        String pTime = postsList.get(position).getpTime();
        String pLikes = postsList.get(position).getpLikes();
        String pComments = postsList.get(position).getpComments();


        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTime));
        String time = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.uNameTV.setText(uName);
        holder.pTimeTV.setText(time);
        holder.pRecipeNameTV.setText(pRecipeName);
        holder.pIngredientsTV.setText(pIngredients);
        holder.pInstructionsTV.setText(pInstructions);
        holder.pLikesTV.setText(pLikes+" " +context.getResources().getString(R.string.likes));
        holder.pCommentsTV.setText(pComments+ " " +context.getResources().getString(R.string.comments));


        setLikes(holder,pId);


        try {
            Picasso.get().load(uProfilePicture).placeholder(R.drawable.ic_profile_default).into(holder.uPictureIV);
        } catch (Exception e) {

        }
        if (pImage.equals("noImage")){
            holder.pImageIV.setVisibility(View.GONE);
        }
        else {
            holder.pImageIV.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(pImage).into(holder.pImageIV);
            } catch (Exception e) {

            }
        }

        holder.moreOptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Toast.makeText(context, "More option",Toast.LENGTH_LONG).show();
                showMoreOptions(holder.moreOptionBtn, uid, myUid, pId, pImage);
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pLikes = Integer.parseInt(postsList.get(holder.getAdapterPosition()).getpLikes());
                processLike = true;
                String postId = postsList.get(holder.getAdapterPosition()).getpId();
                likesReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (processLike){
                            if (snapshot.child(postId).hasChild(myUid)){
                                postsReference.child(postId).child("pLikes").setValue((pLikes-1)+"");
                                likesReference.child(postId).child(myUid).removeValue();
                                processLike=false;
                            }
                            else{
                                postsReference.child(postId).child("pLikes").setValue((pLikes+1)+"");
                                likesReference.child(postId).child(myUid).setValue("Liked");
                                processLike=false;
                               // if (!postsReference.child(postId).child("uid").getKey().equals(myUid)) {
                                if (!(uid.equals(myUid))){
                                    addNotifications(uid + "", pId + "", context.getResources().getString(R.string.likedYourRecipe));
                                }
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        holder.profileLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OtherProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);

            }
        });


    }

    private void setLikes(Holder holder, String postKey) {
        likesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)){
                    //change drawable of like button to liked, and text from like to liked
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.likeBtn.setText(R.string.liked);
                }
                else{
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_icon, 0, 0, 0);
                    holder.likeBtn.setText(R.string.like);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showMoreOptions(ImageButton moreOptionBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreOptionBtn, Gravity.END);
        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.deleteRecipe);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0) {
                    deletePost(pId, pImage);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void deletePost(String pId, String pImage) {
        if (pImage.equals("noImage")){
            deletePoseWithoutImage(pId);
        }
        else {
            deletePostWithImage(pId, pImage);
        }
    }

    private void deletePostWithImage(String pId, String pImage) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.deleting));
        StorageReference imageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            dataSnapshot.getRef().removeValue();
                            Toast.makeText(context, R.string.recipeDeletedSuccessfully, Toast.LENGTH_LONG).show();
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
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void deletePoseWithoutImage(String pId) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.deleting));

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    dataSnapshot.getRef().removeValue();
                    Toast.makeText(context, R.string.recipeDeletedSuccessfully, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        hashMap.put("sUid", myUid);

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

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        ImageView uPictureIV, pImageIV;
        TextView uNameTV, pTimeTV, pRecipeNameTV, pIngredientsTV, pInstructionsTV, pLikesTV, pCommentsTV;
        ImageButton moreOptionBtn;
        Button likeBtn, commentBtn;
        LinearLayout profileLinearLayout;

        public Holder(@NonNull View itemView) {
            super(itemView);
            uPictureIV = itemView.findViewById(R.id.profile_pic_IV);
            pImageIV = itemView.findViewById(R.id.recipe_iv);
            uNameTV = itemView.findViewById(R.id.uName_Tv);
            pTimeTV = itemView.findViewById(R.id.uTime_Tv);
            pRecipeNameTV = itemView.findViewById(R.id.recipeName_TV);
            pIngredientsTV = itemView.findViewById(R.id.ingredients_TV);
            pInstructionsTV = itemView.findViewById(R.id.instructions_TV);
            pLikesTV = itemView.findViewById(R.id.likes_TV);
            moreOptionBtn = itemView.findViewById(R.id.more_option_btn);
            likeBtn = itemView.findViewById(R.id.like_btn);
            commentBtn = itemView.findViewById(R.id.comment_btn);
            profileLinearLayout = itemView.findViewById(R.id.profile_layout);
            pCommentsTV = itemView.findViewById(R.id.comments_TV);
        }
    }
}
