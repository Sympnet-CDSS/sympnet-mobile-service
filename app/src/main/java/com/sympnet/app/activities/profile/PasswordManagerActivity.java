package com.sympnet.app.activities.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.sympnet.app.R;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordManagerActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_manager);

        apiService = ApiClient.getClient().create(ApiService.class);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnChangePassword = findViewById(R.id.btnChangePassword);

        btnBack.setOnClickListener(v -> finish());

        btnChangePassword.setOnClickListener(v -> handlePasswordChange());
    }

    private void handlePasswordChange() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmNewPassword.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("currentPassword", currentPassword);
        data.put("newPassword", newPassword);

        apiService.resetPassword(data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PasswordManagerActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PasswordManagerActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PasswordManagerActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
