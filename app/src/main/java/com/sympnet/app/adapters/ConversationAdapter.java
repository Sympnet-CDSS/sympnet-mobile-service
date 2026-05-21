package com.sympnet.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sympnet.app.R;
import com.sympnet.app.model.Conversation;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation, listener);
    }

    @Override
    public int getItemCount() {
        return conversations != null ? conversations.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvLastMessage, tvTime, tvUnread, tvInitials;
        android.widget.ImageView ivAvatar;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnread = itemView.findViewById(R.id.tvUnread);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }

        void bind(final Conversation conversation, final OnConversationClickListener listener) {
            String name = conversation.getOtherUserName() != null ? conversation.getOtherUserName() : "Inconnu";
            tvName.setText(name);
            tvRole.setText(conversation.getOtherUserRole());
            tvLastMessage.setText(conversation.getLastMessage());
            
            // Set initials
            if (tvInitials != null) {
                String initials = "";
                String[] parts = name.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].charAt(0);
                if (parts.length > 1 && !parts[1].isEmpty()) initials += parts[1].charAt(0);
                tvInitials.setText(initials.toUpperCase());
            }

            if (ivAvatar != null) {
                if (conversation.getOtherUserAvatar() != null && !conversation.getOtherUserAvatar().isEmpty()) {
                    try {
                        String base64 = conversation.getOtherUserAvatar();
                        if (base64.contains(",")) base64 = base64.split(",")[1];
                        byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (bmp != null) {
                            ivAvatar.setImageBitmap(bmp);
                            ivAvatar.setVisibility(View.VISIBLE);
                            if (tvInitials != null) tvInitials.setVisibility(View.GONE);
                        } else {
                            ivAvatar.setVisibility(View.GONE);
                            if (tvInitials != null) tvInitials.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        ivAvatar.setVisibility(View.GONE);
                        if (tvInitials != null) tvInitials.setVisibility(View.VISIBLE);
                    }
                } else {
                    ivAvatar.setVisibility(View.GONE);
                    if (tvInitials != null) tvInitials.setVisibility(View.VISIBLE);
                }
            }
            
            if (conversation.getLastMessageAt() != null) {
                String time = conversation.getLastMessageAt();
                if (time.contains("T") && time.contains(":")) {
                    try {
                        time = time.split("T")[1].substring(0, 5);
                    } catch (Exception e) {}
                }
                tvTime.setText(time);
            }

            if (conversation.getUnreadCount() > 0) {
                tvUnread.setText(String.valueOf(conversation.getUnreadCount()));
                tvUnread.setVisibility(View.VISIBLE);
            } else {
                tvUnread.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onConversationClick(conversation));
        }
    }
}