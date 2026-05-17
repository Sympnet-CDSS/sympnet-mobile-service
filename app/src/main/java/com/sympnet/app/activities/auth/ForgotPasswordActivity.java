package com.sympnet.app.activities.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sympnet.app.R;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.sympnet.app.activities.BaseActivity;

public class ForgotPasswordActivity extends BaseActivity {

    private TextInputLayout tilEmail, tilCode;
    private LinearLayout layoutNewPassword;
    private TextInputEditText etEmail, etCode, etNewPassword, etConfirmPassword;
    private Button btnAction;
    private TextView tvTitle, tvDesc, tvBackToLogin;
    
    private int step = 1;
    private ApiService apiService;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        apiService = ApiClient.getClient().create(ApiService.class);

        tilEmail = findViewById(R.id.til_email);
        tilCode = findViewById(R.id.til_code);
        layoutNewPassword = findViewById(R.id.layout_new_password);
        
        etEmail = findViewById(R.id.et_email);
        etCode = findViewById(R.id.et_code);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        
        btnAction = findViewById(R.id.btn_action);
        tvTitle = findViewById(R.id.tv_title);
        tvDesc = findViewById(R.id.tv_desc);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        btnAction.setOnClickListener(v -> handleAction());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleAction() {
        if (step == 1) {
            sendVerificationCode();
        } else if (step == 2) {
            verifyCode();
        } else if (step == 3) {
            resetPassword();
        }
    }

    private void sendVerificationCode() {
        userEmail = etEmail.getText().toString().trim();
        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("email", userEmail);

        apiService.forgotPassword(data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Verification code sent to " + userEmail, Toast.LENGTH_SHORT).show();
                    showStep2();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Error: Email not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyCode() {
        String code = etCode.getText().toString().trim();
        if (code.length() < 6) {
            Toast.makeText(this, "Please enter the 6-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("email", userEmail);
        data.put("code", code);

        apiService.verifyResetCode(data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showStep3();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Invalid verification code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Server error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword() {
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();
        
        if (newPass.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("email", userEmail);
        data.put("password", newPass);

        apiService.resetPassword(data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Server error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStep2() {
        step = 2;
        tilEmail.setVisibility(View.GONE);
        tilCode.setVisibility(View.VISIBLE);
        tvTitle.setText("Verify Code 🔑");
        tvDesc.setText("Enter the 6-digit code sent to your email");
        btnAction.setText("Verify Code");
    }

    private void showStep3() {
        step = 3;
        tilCode.setVisibility(View.GONE);
        layoutNewPassword.setVisibility(View.VISIBLE);
        tvTitle.setText("New Password 🛡️");
        tvDesc.setText("Create a strong password for your account");
        btnAction.setText("Reset Password");
    }
}
