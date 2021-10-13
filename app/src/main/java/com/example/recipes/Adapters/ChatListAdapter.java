package com.example.recipes.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Activities.ChatActivity;
import com.example.recipes.Models.ModelUsers;
import com.example.recipes.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {

    Context context;
    List<ModelUsers> usersList;
    HashMap<String, String> lastMessageHashMap;

    public ChatListAdapter(Context getContextValue, List<ModelUsers> usersList) {
        this.context = getContextValue;
        this.usersList = usersList;
        lastMessageHashMap = new HashMap<>();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_row, parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String hisUid = usersList.get(position).getUid();
        String userImage = usersList.get(position).getProfile();
        String userName = usersList.get(position).getName();
        String lastMessage = lastMessageHashMap.get(hisUid);


        holder.nameTV.setText(userName);
        if (lastMessage==null || lastMessage.equals("default")){
            holder.last_message_chat_TV.setVisibility(View.GONE);
        }
        else{
            holder.last_message_chat_TV.setVisibility(View.VISIBLE);
            holder.last_message_chat_TV.setText(lastMessage);
        }
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_profile_default).into(holder.profileIV);
        }catch (Exception e){
          //  Picasso.get().load(R.drawable.ic_profile_default).into(holder.profileIV);
        }

        if (usersList.get(position).getOnlineStatus().equals("online")){
            holder.status_IV.setImageResource(R.drawable.online_status_icon);
        }
        else{
            holder.status_IV.setImageResource(R.drawable.offline_status_icon);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });


    }
    public void setLastMessage(String userId, String lastMessage){
        lastMessageHashMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView nameTV, last_message_chat_TV;
        ImageView profileIV, status_IV;


        public Holder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.name_chat_TV);
            last_message_chat_TV = itemView.findViewById(R.id.last_message_chat_TV);
            profileIV = itemView.findViewById(R.id.profile_pic_chat_IV);
            status_IV = itemView.findViewById(R.id.status_IV);
        }
    }
}
