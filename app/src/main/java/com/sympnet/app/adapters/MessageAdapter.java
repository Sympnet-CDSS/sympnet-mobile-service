package com.sympnet.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sympnet.app.R;
import com.sympnet.app.model.Message;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserId;
    private String partnerPhoto;

    public MessageAdapter(List<Message> messages, String currentUserId, String partnerPhoto) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.partnerPhoto = partnerPhoto;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message != null && message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvStatus;
        android.widget.ImageView ivUserPhoto;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
        }

        void bind(Message message) {
            tvMessage.setText(message.getContent());
            if (message.getSentAt() != null) {
                // Affichage direct de la String date ou extraction de l'heure
                String time = message.getSentAt();
                if (time.contains("T") && time.contains(":")) {
                    try {
                        time = time.split("T")[1].substring(0, 5);
                    } catch (Exception e) {}
                }
                tvTime.setText(time);
            }
            
            if (message.isRead()) {
                tvStatus.setText("✓✓");
                tvStatus.setTextColor(0xFF4FC3F7); 
            } else if (message.isDelivered()) {
                tvStatus.setText("✓✓");
                tvStatus.setTextColor(0xFFB2DFDB); 
            } else {
                tvStatus.setText("✓");
                tvStatus.setTextColor(0xFFB2DFDB);
            }
            
            if (ivUserPhoto != null) {
                android.content.SharedPreferences prefs = itemView.getContext().getSharedPreferences("SympNetPrefs", android.content.Context.MODE_PRIVATE);
                String base64 = prefs.getString("userPhotoBase64", null);
                if (base64 != null && !base64.isEmpty()) {
                    try {
                        if (base64.contains(",")) base64 = base64.split(",")[1];
                        byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (bmp != null) {
                            com.bumptech.glide.Glide.with(itemView.getContext()).load(bmp).transform(new com.bumptech.glide.load.resource.bitmap.CircleCrop()).into(ivUserPhoto);
                        }
                    } catch (Exception e) {}
                }
            }
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvInitials;
        View avatarContainer;
        android.widget.ImageView ivDocPhoto;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvInitials = itemView.findViewById(R.id.tvReceivedInitials);
            avatarContainer = itemView.findViewById(R.id.docAvatarSmall);
            ivDocPhoto = itemView.findViewById(R.id.ivDocPhoto);
        }

        void bind(Message message) {
            tvMessage.setText(message.getContent());
            if (message.getSentAt() != null) {
                String time = message.getSentAt();
                if (time.contains("T") && time.contains(":")) {
                    try {
                        time = time.split("T")[1].substring(0, 5);
                    } catch (Exception e) {}
                }
                tvTime.setText(time);
            }

            // Initials
            String name = message.getSenderName();
            if (name != null && !name.isEmpty()) {
                String[] parts = name.split(" ");
                String initials = "";
                if (parts.length > 0) initials += parts[0].charAt(0);
                if (parts.length > 1) initials += parts[1].charAt(0);
                tvInitials.setText(initials.toUpperCase());
            } else {
                tvInitials.setText("D");
            }
            
            if (partnerPhoto != null && !partnerPhoto.isEmpty() && ivDocPhoto != null) {
                try {
                    String base64 = partnerPhoto;
                    if (base64.contains(",")) base64 = base64.split(",")[1];
                    byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bmp != null) {
                        com.bumptech.glide.Glide.with(itemView.getContext()).load(bmp).into(ivDocPhoto);
                        ivDocPhoto.setVisibility(View.VISIBLE);
                        tvInitials.setVisibility(View.GONE);
                    }
                } catch (Exception e) {}
            }
        }
    }
}