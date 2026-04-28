package com.sympnet.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.sympnet.app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private static final int TYPE_SENT     = 1;
    private static final int TYPE_RECEIVED = 2;

    private RecyclerView recyclerMessages;
    private TextInputEditText etMessage;
    private MessageAdapter adapter;
    private final List<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Doctor info passed from DoctorAdapter or wherever you launch this
        String doctorName = getIntent().getStringExtra("DOCTOR_NAME");
        if (doctorName == null) doctorName = "Dr. Olivia Turner";

        // Header
        TextView tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorName.setText(doctorName);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // RecyclerView
        recyclerMessages = findViewById(R.id.recyclerMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);   // newest messages at the bottom
        recyclerMessages.setLayoutManager(layoutManager);

        adapter = new MessageAdapter(messages);
        recyclerMessages.setAdapter(adapter);

        // Input
        etMessage = findViewById(R.id.etMessage);
        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        if (etMessage.getText() == null) return;
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        messages.add(new Message(text, time, TYPE_SENT));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerMessages.scrollToPosition(messages.size() - 1);
        etMessage.setText("");
    }

    // ── Message model ─────────────────────────────────────────────────────

    static class Message {
        String text, time;
        int type;
        Message(String text, String time, int type) {
            this.text = text; this.time = time; this.type = type;
        }
    }

    // ── Adapter ───────────────────────────────────────────────────────────

    static class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<Message> list;

        MessageAdapter(List<Message> list) { this.list = list; }

        @Override
        public int getItemViewType(int pos) { return list.get(pos).type; }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_SENT) {
                return new MsgHolder(inf.inflate(R.layout.item_message_sent, parent, false));
            } else {
                return new MsgHolder(inf.inflate(R.layout.item_message_received, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
            Message msg = list.get(pos);
            MsgHolder h = (MsgHolder) holder;
            h.tvText.setText(msg.text);
            h.tvTime.setText(msg.time);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class MsgHolder extends RecyclerView.ViewHolder {
            TextView tvText, tvTime;
            MsgHolder(View v) {
                super(v);
                tvText = v.findViewById(R.id.tvMessageText);
                tvTime = v.findViewById(R.id.tvMessageTime);
            }
        }
    }
}