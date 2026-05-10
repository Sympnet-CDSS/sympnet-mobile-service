package com.sympnet.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sympnet.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatbotFragment extends Fragment {

    private LinearLayout chatContainer;
    private ScrollView scrollView;
    private EditText etMessage;
    private ImageButton btnSend;
    private List<String> symptomsList = new ArrayList<>();

    private static final String AI_URL = "http://192.168.100.8:8000";
    private static final String TAG = "ChatbotAI";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        chatContainer = view.findViewById(R.id.chatContainer);
        scrollView    = view.findViewById(R.id.scrollView);
        etMessage     = view.findViewById(R.id.etMessage);
        btnSend       = view.findViewById(R.id.btnSend);

        addBotMessage("👋 Bonjour ! Je suis SympNet AI.\n\nDécrivez vos symptômes et je vais vous aider à identifier votre maladie.");

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                addUserMessage(message);
                etMessage.setText("");
                sendToDiagnose(message);
            }
        });

        return view;
    }

    private void sendToDiagnose(String text) {
        addBotMessage("⏳ Analyse en cours...");

        new Thread(() -> {
            try {
                Log.d(TAG, "Sending to analyze-symptoms: " + text);

                URL url = new URL(AI_URL + "/analyze-symptoms");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);

                JSONObject requestBody = new JSONObject();
                requestBody.put("text", text);
                requestBody.put("language", "fr");

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes());
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "analyze-symptoms HTTP code: " + responseCode);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                Log.d(TAG, "analyze-symptoms response: " + response.toString());

                JSONObject analysisResult = new JSONObject(response.toString());
                JSONArray symptoms = analysisResult.getJSONArray("symptoms");

                if (symptoms.length() == 0) {
                    requireActivity().runOnUiThread(() ->
                            updateLastBotMessage("❓ Je n'ai pas détecté de symptômes clairs. Pouvez-vous décrire plus précisément ce que vous ressentez ?"));
                    return;
                }

                symptomsList.clear();
                StringBuilder symptomsText = new StringBuilder("✅ Symptômes détectés :\n");
                for (int i = 0; i < symptoms.length(); i++) {
                    String symptom = symptoms.getJSONObject(i).getString("symptom");
                    symptomsList.add(symptom);
                    symptomsText.append("• ").append(symptom).append("\n");
                }

                Log.d(TAG, "Symptoms list: " + symptomsList.toString());

                requireActivity().runOnUiThread(() ->
                        updateLastBotMessage(symptomsText.toString()));

                diagnoseSympoms();

            } catch (Exception e) {
                Log.e(TAG, "Error in sendToDiagnose: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() ->
                        updateLastBotMessage("❌ Erreur de connexion au service AI : " + e.getMessage()));
            }
        }).start();
    }

    private void diagnoseSympoms() {
        new Thread(() -> {
            try {
                Log.d(TAG, "Sending to diagnose: " + symptomsList.toString());

                URL url = new URL(AI_URL + "/diagnose");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(60000); // 60s car le diagnostic peut prendre du temps

                JSONObject requestBody = new JSONObject();
                JSONArray symptomsArray = new JSONArray(symptomsList);
                requestBody.put("symptoms", symptomsArray);

                Log.d(TAG, "Diagnose request body: " + requestBody.toString());

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes());
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "diagnose HTTP code: " + responseCode);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                Log.d(TAG, "Diagnose response: " + response.toString());

                JSONObject diagnosisResult = new JSONObject(response.toString());
                JSONArray hypotheses = diagnosisResult.getJSONArray("hypotheses");
                JSONArray recommendations = diagnosisResult.getJSONArray("recommendations");

                Log.d(TAG, "Hypotheses count: " + hypotheses.length());
                Log.d(TAG, "Recommendations count: " + recommendations.length());

                StringBuilder resultText = new StringBuilder("🩺 Diagnostic :\n\n");

                int count = Math.min(3, hypotheses.length());
                for (int i = 0; i < count; i++) {
                    JSONObject h = hypotheses.getJSONObject(i);
                    String diagnosis = h.getString("diagnosis");
                    double confidence = h.getDouble("confidence") * 100;
                    resultText.append(String.format("• %s (%.0f%%)\n", diagnosis, confidence));
                }

                if (recommendations.length() > 0) {
                    resultText.append("\n💊 Recommandations :\n");
                    for (int i = 0; i < Math.min(3, recommendations.length()); i++) {
                        resultText.append("• ").append(recommendations.getString(i)).append("\n");
                    }
                }

                resultText.append("\n⚠️ Ceci est une aide au diagnostic. Consultez un médecin pour confirmation.");

                String finalText = resultText.toString();
                requireActivity().runOnUiThread(() -> addBotMessage(finalText));

            } catch (Exception e) {
                Log.e(TAG, "Error in diagnoseSympoms: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() ->
                        addBotMessage("❌ Erreur lors du diagnostic : " + e.getMessage()));
            }
        }).start();
    }

    private void addUserMessage(String message) {
        TextView tv = new TextView(getContext());
        tv.setText(message);
        tv.setTextColor(0xFFFFFFFF);
        tv.setBackgroundResource(R.drawable.bg_btn_green);
        tv.setPadding(24, 16, 24, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.END;
        params.setMargins(80, 8, 8, 8);
        tv.setLayoutParams(params);

        chatContainer.addView(tv);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        TextView tv = new TextView(getContext());
        tv.setText(message);
        tv.setTextColor(0xFF1A2A3A);
        tv.setBackgroundResource(R.drawable.card_background);
        tv.setPadding(24, 16, 24, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.START;
        params.setMargins(8, 8, 80, 8);
        tv.setLayoutParams(params);
        tv.setTag("bot_message");

        chatContainer.addView(tv);
        scrollToBottom();
    }

    private void updateLastBotMessage(String message) {
        for (int i = chatContainer.getChildCount() - 1; i >= 0; i--) {
            View child = chatContainer.getChildAt(i);
            if (child instanceof TextView && "bot_message".equals(child.getTag())) {
                ((TextView) child).setText(message);
                break;
            }
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}