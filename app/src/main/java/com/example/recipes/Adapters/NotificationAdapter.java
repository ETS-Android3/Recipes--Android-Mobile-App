package com.example.recipes.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Models.ModelNotification;
import com.example.recipes.Activities.PostDetailActivity;
import com.example.recipes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder>
{
    private Context context;
    List<ModelNotification> notificationsList;
    private FirebaseAuth firebaseAuth;

    public NotificationAdapter(Context context, List<ModelNotification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_row, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) { //get and set the data to each row in recyclerView
        ModelNotification modelNotification = notificationsList.get(position);
        String time = modelNotification.getTime();
        String notification = modelNotification.getNotification();
        String senderUid = modelNotification.getsUid();
        String postId = modelNotification.getpId();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(time));
        String formattedTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String name = dataSnapshot.child("name").getValue()+"";
                    String email = dataSnapshot.child("email").getValue()+"";
                    String image = dataSnapshot.child("profile").getValue()+"";

                    modelNotification.setsName(name);
                    modelNotification.setsEmail(email);
                    modelNotification.setsImage(image);

                    holder.nameTV.setText(name);

                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_profile_default).into(holder.profileIV);
                    }catch (Exception e){
                        holder.profileIV.setImageResource(R.drawable.ic_profile_default);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.notificationTV.setText(notification);
        holder.timeTV.setText(formattedTime);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", postId);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.deleteNotification);
                builder.setMessage(R.string.notificationDeleteConfirmation);
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Users");
                        databaseReference1.child(firebaseAuth.getUid()).child("Notifications").child(time).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, R.string.notificationDeleted, Toast.LENGTH_LONG).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage()+"", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView nameTV, notificationTV, timeTV;
        ImageView profileIV;
        public Holder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.name_notification_TV);
            notificationTV = itemView.findViewById(R.id.notification_TV);
            timeTV = itemView.findViewById(R.id.notification_time_TV);
            profileIV = itemView.findViewById(R.id.profilePic_notification_IV);

        }
    }
}
