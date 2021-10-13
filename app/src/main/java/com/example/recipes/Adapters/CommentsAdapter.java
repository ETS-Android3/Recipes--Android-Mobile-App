package com.example.recipes.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Models.ModelComment;
import com.example.recipes.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.Holder>{
    Context context;
    List<ModelComment> commentsList;
    String  myUid, postId;

    public CommentsAdapter(Context context, List<ModelComment> commentsList, String myUid, String postId) {
        this.context = context;
        this.commentsList = commentsList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_row, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        //get data
        String uid = commentsList.get(position).getUid();
        String name = commentsList.get(position).getuName();
        String image = commentsList.get(position).getuProfilePicture();
        String cid = commentsList.get(position).getcId();
        String comment = commentsList.get(position).getComment();

        //set data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        //  holder.timeTv.setText(formattedTime);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_profile_default).into(holder.userIv);
        }catch (Exception e){

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myUid.equals(uid)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle(R.string.deleteComment);
                    builder.setMessage(R.string.commentDeleteConfirmation);
                    builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create().show();
                }
                else{
                    Toast.makeText(context, R.string.youCantDeleteOtherUserComment, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void deleteComment(String cId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        databaseReference.child("Comments").child(cId).removeValue();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = "" + snapshot.child("pComments").getValue();
                int newCommentCount = Integer.parseInt(comments) - 1;
                databaseReference.child("pComments").setValue(""+newCommentCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        TextView nameTv, commentTv, timeTv;
        ImageView userIv;

        public Holder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.name_TV);
            commentTv = itemView.findViewById(R.id.comment_TV);
            timeTv = itemView.findViewById(R.id.time_TV);
            userIv = itemView.findViewById(R.id.profile_pic_IV);
        }
    }
}
