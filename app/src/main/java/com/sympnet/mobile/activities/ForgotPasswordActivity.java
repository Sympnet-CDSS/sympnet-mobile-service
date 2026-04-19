package com.sympnet.mobile.activities;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sympnet.mobile.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSend;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.et_email);
        btnSend = findViewById(R.id.btn_send);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Email de réinitialisation envoyé à " + email, Toast.LENGTH_SHORT).show();
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }
}
