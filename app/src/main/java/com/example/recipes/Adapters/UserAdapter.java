package com.example.recipes.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Activities.ChatActivity;
import com.example.recipes.Models.ModelUsers;
import com.example.recipes.Activities.OtherProfileActivity;
import com.example.recipes.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Holder> {

    Context context;
    List<ModelUsers> usersList;

    public UserAdapter(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_row, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.Holder holder, int position) {

        String name = usersList.get(position).getName();
        String email = usersList.get(position).getEmail();
        String image = usersList.get(position).getProfile();
        String otherUid = usersList.get(position).getUid();


        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_add_image).into(holder.profileIv);
        }
        catch (Exception e){

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{context.getResources().getString(R.string.profile), context.getResources().getString(R.string.chat)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(context, OtherProfileActivity.class);
                            intent.putExtra("uid", otherUid);
                            context.startActivity(intent);

                        }
                        if (which == 1) {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid", otherUid);
                            context.startActivity(intent);

                        }
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        ImageView profileIv;
        TextView nameTv, emailTv;
        public Holder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profile_iv);
            nameTv = itemView.findViewById(R.id.name_tv);
            emailTv = itemView.findViewById(R.id.email_tv);


        }
    }

}
