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

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
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

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
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
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvInitials;
        View avatarContainer;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvInitials = itemView.findViewById(R.id.tvReceivedInitials);
            avatarContainer = itemView.findViewById(R.id.docAvatarSmall);
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
        }
    }
}