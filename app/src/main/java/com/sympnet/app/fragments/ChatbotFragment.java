package com.sympnet.app.fragments;
import com.sympnet.app.activities.doctor.DoctorDetailsActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.sympnet.app.R;
import com.sympnet.app.network.ApiClient;
import com.sympnet.app.network.ApiService;
import com.sympnet.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotFragment extends Fragment {

    private static final String TAG = "ChatbotAI";
    private LinearLayout chatContainer;
    private ScrollView scrollView;
    private EditText etMessage;
    private ImageButton btnSend;

    private double userLat = 36.8065; // Tunis par défaut
    private double userLng = 10.1815;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int COLOR_BOT_TEXT = 0xFF1A2A3A;
    private static final int COLOR_ACCENT = 0xFF0D9488;

    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;
    private java.util.List<org.json.JSONObject> historyItems = new java.util.ArrayList<>();
    private android.widget.ArrayAdapter<org.json.JSONObject> historyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        chatContainer = view.findViewById(R.id.chatContainer);
        scrollView = view.findViewById(R.id.scrollView);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        ImageButton btnMic = view.findViewById(R.id.btnMic);
        drawerLayout = view.findViewById(R.id.drawerLayout);

        android.widget.ImageView btnMenu = view.findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(android.view.Gravity.LEFT));
        }

        android.widget.Button btnNewConvSidebar = view.findViewById(R.id.btnNewConvSidebar);
        if (btnNewConvSidebar != null) {
            btnNewConvSidebar.setOnClickListener(v -> {
                clearChat();
                drawerLayout.closeDrawer(android.view.Gravity.LEFT);
            });
        }
        
        android.widget.ImageButton btnCloseSidebar = view.findViewById(R.id.btnCloseSidebar);
        if (btnCloseSidebar != null) {
            btnCloseSidebar.setOnClickListener(v -> drawerLayout.closeDrawer(android.view.Gravity.LEFT));
        }

        android.widget.ListView lvHistory = view.findViewById(R.id.lvHistory);
        if (lvHistory != null) {
            loadHistory();
            historyAdapter = new android.widget.ArrayAdapter<org.json.JSONObject>(requireContext(), R.layout.item_history, historyItems) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_history, parent, false);
                    }
                    try {
                        org.json.JSONObject item = getItem(position);
                        String prompt = item.getString("prompt");
                        TextView tvTitle = convertView.findViewById(R.id.tvHistoryTitle);
                        TextView tvSub = convertView.findViewById(R.id.tvHistorySubtitle);
                        android.widget.ImageView ivIcon = convertView.findViewById(R.id.ivHistoryIcon);
                        
                        tvTitle.setText(prompt.length() > 25 ? prompt.substring(0, 25) + "..." : prompt);
                        
                        if (position == 0) tvSub.setText("Aujourd'hui");
                        else if (position == 1) tvSub.setText("Hier");
                        else tvSub.setText("Précédent");

                        String p = prompt.toLowerCase();
                        int bgColor = 0xFFECFDF5;
                        int tintColor = 0xFF10B981;
                        if (p.contains("tête") || p.contains("head")) {
                            bgColor = 0xFFF5F3FF; tintColor = 0xFF8B5CF6;
                        } else if (p.contains("coeur") || p.contains("poitrine") || p.contains("heart") || p.contains("mal")) {
                            bgColor = 0xFFFEF2F2; tintColor = 0xFFEF4444;
                        } else if (p.contains("fièvre") || p.contains("fever")) {
                            bgColor = 0xFFFFFBEB; tintColor = 0xFFF59E0B;
                        } else if (p.contains("respir")) {
                            bgColor = 0xFFEFF6FF; tintColor = 0xFF3B82F6;
                        }
                        
                        ivIcon.setBackgroundColor(bgColor);
                        ivIcon.setColorFilter(tintColor);
                    } catch(Exception e) {}
                    return convertView;
                }
            };
            lvHistory.setAdapter(historyAdapter);
            lvHistory.setOnItemClickListener((parent, view1, position, id) -> {
                clearChat();
                try {
                    org.json.JSONObject item = historyItems.get(position);
                    String prompt = item.getString("prompt");
                    addUserMessage(prompt);
                    
                    // Convert JSONObject to Map<String, Object> for processDiagnosticResult
                    Map<String, Object> resultMap = new com.google.gson.Gson().fromJson(item.getJSONObject("response").toString(), new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType());
                    processDiagnosticResult(resultMap, prompt);
                } catch (Exception e) {}
                
                drawerLayout.closeDrawer(android.view.Gravity.LEFT);
            });
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestLocation();

        // Ajout d'un bouton "Nouvelle conversation" en haut
        ImageButton btnNewChat = new ImageButton(getContext());
        btnNewChat.setImageResource(android.R.drawable.ic_menu_add);
        btnNewChat.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnNewChat.setPadding(0, 0, 32, 0);
        btnNewChat.setOnClickListener(v -> clearChat());
        
        ViewGroup parent = (ViewGroup) chatContainer.getParent().getParent();
        if (parent instanceof LinearLayout && ((LinearLayout) parent).getChildAt(0) instanceof LinearLayout) {
            LinearLayout header = (LinearLayout) ((LinearLayout) parent).getChildAt(0);
            header.addView(btnNewChat);
        }

        addBotMessage("Bonjour ! Je suis SympNet AI.\n\nDécrivez vos symptômes et je vais analyser votre état et trouver des médecins spécialisés proches de vous.");

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                addUserMessage(message);
                etMessage.setText("");
                sendToDiagnose(message);
            }
        });

        if (btnMic != null) {
            btnMic.setOnClickListener(v -> {
                Intent intent = new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault());
                intent.putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...");
                try {
                    startActivityForResult(intent, 1000);
                } catch (Exception e) {
                    android.widget.Toast.makeText(getContext(), "Votre appareil ne supporte pas la saisie vocale.", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == android.app.Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                etMessage.setText(result.get(0));
            }
        }
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                    Log.d(TAG, "Location obtained: " + userLat + ", " + userLng);
                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }

    private void sendToDiagnose(String text) {
        addBotMessage("⏳ Analyse médicale en cours...");

        String token = SessionManager.getInstance(requireContext()).getUserToken();
        String userId = SessionManager.getInstance(requireContext()).getCurrentUserId();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Map<String, Object> payload = new HashMap<>();
        payload.put("patient_id", userId != null ? userId : "guest");
        payload.put("text", text);
        payload.put("patient_history", new HashMap<String, Object>());
        payload.put("allergies", new ArrayList<String>());

        apiService.getDiagnostic("Bearer " + token, payload).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processDiagnosticResult(response.body(), text);
                    saveToHistory(text, response.body());
                } else {
                    updateLastBotMessage(" Erreur de l'IA (Code " + response.code() + "). Réessayez.");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                updateLastBotMessage(" Erreur réseau : " + t.getMessage());
            }
        });
    }

    private void processDiagnosticResult(Map<String, Object> result, String originalText) {
        try {
            Map<String, Object> confidenceMap = (Map<String, Object>) result.get("confidence");
            Map<String, Object> explanationMap = (Map<String, Object>) result.get("explanations");
            Map<String, Object> symptomMap = (Map<String, Object>) result.get("symptom_analysis");

            String diagnosis = confidenceMap != null ? (String) confidenceMap.get("top_diagnosis") : "Indéterminé";
            double score = 0.0;
            if (confidenceMap != null && confidenceMap.get("final_score") instanceof Number) {
                score = ((Number) confidenceMap.get("final_score")).doubleValue();
            }

            String explanation = explanationMap != null ? (String) explanationMap.get("doctor_summary") : "";
            String recommendation = confidenceMap != null ? (String) confidenceMap.get("recommendation") : "";
            List<Map<String, Object>> alternatives = confidenceMap != null ? (List<Map<String, Object>>) confidenceMap.get("alternative_diagnoses") : null;

            String aiSpecialty = (symptomMap != null) ? (String) symptomMap.get("specialty") : "medecine_generale";
            String friendlySpecialty = getFriendlySpecialtyName(aiSpecialty);
            
            // On affiche la carte diagnostic au lieu du texte brut
            View diagnosticCard = addDiagnosticCard(diagnosis, score, friendlySpecialty);

            // Chercher les médecins
            loadNearbyDoctors(aiSpecialty, diagnosticCard);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing diagnostic", e);
            updateLastBotMessage(" Erreur lors de l'analyse des résultats.");
        }
    }

    private void loadNearbyDoctors(String aiSpecialty, View diagnosticCard) {
        String token = SessionManager.getInstance(requireContext()).getUserToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getDoctors().enqueue(new Callback<List<com.sympnet.app.model.Doctor>>() {
            @Override
            public void onResponse(Call<List<com.sympnet.app.model.Doctor>> call, Response<List<com.sympnet.app.model.Doctor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.sympnet.app.model.Doctor> allDocs = response.body();
                    List<com.sympnet.app.model.Doctor> filteredDocs = new ArrayList<>();

                    // Filtrage par spécialité (comparaison flexible et intelligente)
                    String search = aiSpecialty.toLowerCase();
                    for (com.sympnet.app.model.Doctor d : allDocs) {
                        String docSpec = d.getSpecialty().toLowerCase();
                        
                        boolean match = docSpec.contains(search.substring(0, Math.min(search.length(), 5))) || 
                                        search.contains(docSpec.substring(0, Math.min(docSpec.length(), 5)));
                        
                        if (!match && search.contains("generale") && (docSpec.contains("génér") || docSpec.contains("gener"))) {
                            match = true;
                        }

                        if (match) {
                            filteredDocs.add(d);
                        }
                    }

                    if (!filteredDocs.isEmpty()) {
                        renderRealDoctorResults(filteredDocs, aiSpecialty, diagnosticCard);
                    } else {
                        LinearLayout container = diagnosticCard.findViewById(R.id.doctorListContainer);
                        if (container != null) {
                            addInfoTextToContainer(container, "ℹ️ Aucun spécialiste en '" + getFriendlySpecialtyName(aiSpecialty) + "' n'est disponible.");
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<com.sympnet.app.model.Doctor>> call, Throwable t) {
                Log.e(TAG, "Failed to load real doctors", t);
            }
        });
    }

    private void renderRealDoctorResults(List<com.sympnet.app.model.Doctor> doctors, String specialty, View diagnosticCard) {
        LinearLayout container = diagnosticCard.findViewById(R.id.doctorListContainer);
        if (container == null) return;

        // Filtrer par distance (ex: max 100 km)
        List<com.sympnet.app.model.Doctor> nearbyDocs = new ArrayList<>();
        for (com.sympnet.app.model.Doctor d : doctors) {
            double dist = calculateHaversine(userLat, userLng, d.getLatitude(), d.getLongitude());
            if (dist <= 100.0) { // On ne garde que ceux à moins de 100km
                nearbyDocs.add(d);
            }
        }

        if (nearbyDocs.isEmpty()) {
            // Si aucun à moins de 100km, on prend tous les médecins de cette spécialité
            nearbyDocs.addAll(doctors);
            if (nearbyDocs.isEmpty()) {
                addInfoTextToContainer(container, "ℹ️ Aucun spécialiste trouvé.");
                return;
            }
        }

        // Trier par distance
        java.util.Collections.sort(nearbyDocs, (d1, d2) -> {
            double dist1 = calculateHaversine(userLat, userLng, d1.getLatitude(), d1.getLongitude());
            double dist2 = calculateHaversine(userLat, userLng, d2.getLatitude(), d2.getLongitude());
            return Double.compare(dist1, dist2);
        });

        // Prendre uniquement les 3 plus proches
        int limit = Math.min(nearbyDocs.size(), 3);
        List<com.sympnet.app.model.Doctor> closestDoctors = nearbyDocs.subList(0, limit);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (com.sympnet.app.model.Doctor doc : closestDoctors) {
            View docView = inflater.inflate(R.layout.item_doctor, container, false);

            TextView tvName = docView.findViewById(R.id.docName);
            TextView tvSpec = docView.findViewById(R.id.docSpecialty);
            double dist = calculateHaversine(userLat, userLng, doc.getLatitude(), doc.getLongitude());
            
            tvName.setText("Dr. " + doc.getFirstName() + " " + doc.getLastName());
            tvSpec.setText(doc.getSpecialty() + " • " + String.format("%.1f km", dist));

            TextView btnInfo = docView.findViewById(R.id.btnInfo);
            if (btnInfo != null) btnInfo.setText("Voir ce médecin");

            docView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), DoctorDetailsActivity.class);
                intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ID, String.valueOf(doc.getId()));
                intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_NAME, "Dr. " + doc.getFirstName() + " " + doc.getLastName());
                intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_SPECIALTY, doc.getSpecialty());
                intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_RATING, (float) doc.getRating());
                intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LAT, doc.getLatitude());
                intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_LNG, doc.getLongitude());
                intent.putExtra(DoctorDetailsActivity.EXTRA_DOCTOR_ADDRESS, doc.getAddress());
                startActivity(intent);
            });

            if (btnInfo != null) btnInfo.setOnClickListener(v -> docView.performClick());

            // Marges pour les sous-cartes
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 4, 0, 4);
            docView.setLayoutParams(params);

            container.addView(docView);
        }
        scrollToBottom();
    }

    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la terre
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void addUserMessage(String message) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_user, chatContainer, false);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        TextView tvTime = view.findViewById(R.id.tvTime);
        android.widget.ImageView ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        
        tvMessage.setText(message);
        tvTime.setText(getCurrentTime());
        
        // Load User Photo
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("SympNetPrefs", android.content.Context.MODE_PRIVATE);
        String base64 = prefs.getString("userPhotoBase64", null);
        if (base64 != null && !base64.isEmpty() && ivUserAvatar != null) {
            try {
                if (base64.startsWith("http") || base64.startsWith("/uploads")) {
                    String url = base64.startsWith("http") ? base64 : "https://unequal-barrier-placate.ngrok-free.dev" + base64;
                    ivUserAvatar.clearColorFilter();
                    ivUserAvatar.setBackgroundResource(android.R.color.transparent);
                    ivUserAvatar.setPadding(0, 0, 0, 0);
                    com.bumptech.glide.Glide.with(this).load(url).transform(new com.bumptech.glide.load.resource.bitmap.CircleCrop()).into(ivUserAvatar);
                } else {
                    if (base64.contains(",")) base64 = base64.split(",")[1];
                    byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bmp != null) {
                        ivUserAvatar.clearColorFilter();
                        ivUserAvatar.setBackgroundResource(android.R.color.transparent);
                        ivUserAvatar.setPadding(0, 0, 0, 0);
                        ivUserAvatar.setImageBitmap(bmp);
                    }
                }
            } catch (Exception e) {}
        }
        
        chatContainer.addView(view);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_bot, chatContainer, false);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        TextView tvTime = view.findViewById(R.id.tvTime);
        
        tvMessage.setText(message);
        tvTime.setText(getCurrentTime());
        view.setTag("bot_message");
        
        chatContainer.addView(view);
        scrollToBottom();
    }

    private View addDiagnosticCard(String diagnosis, double score, String specialty) {
        // Retirer le message 
        updateLastBotMessage(null); 

        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_ai_diagnostic_card, chatContainer, false);
        TextView tvDiagnosis = view.findViewById(R.id.tvDiagnosis);
        TextView tvConfidencePercent = view.findViewById(R.id.tvConfidencePercent);
        ProgressBar pbConfidence = view.findViewById(R.id.pbConfidence);
        TextView tvRecommendation = view.findViewById(R.id.tvRecommendation);
        TextView tvTime = view.findViewById(R.id.tvTime);

        tvDiagnosis.setText(diagnosis);
        int percent = (int) (score * 100);
        tvConfidencePercent.setText(percent + "%");
        pbConfidence.setProgress(percent);

        int color;
        if (percent < 40) {
            color = 0xFFEF4444; 
        } else if (percent < 75) {
            color = 0xFFD97706; 
        } else {
            color = 0xFF10B981;     
        }
        pbConfidence.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        tvConfidencePercent.setTextColor(color);

        tvRecommendation.setText("Cartes de spécialistes en " + specialty.replace("un ", ""));
        tvTime.setText(getCurrentTime());

        chatContainer.addView(view);
        scrollToBottom();
        return view;
    }

    private void addInfoTextToContainer(LinearLayout container, String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(0xFF78909C);
        tv.setPadding(0, 16, 0, 0);
        container.addView(tv);
    }

    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
    }

    private void updateLastBotMessage(String message) {
        for (int i = chatContainer.getChildCount() - 1; i >= 0; i--) {
            View child = chatContainer.getChildAt(i);
            if ("bot_message".equals(child.getTag())) {
                if (message == null) {
                    chatContainer.removeView(child);
                } else {
                    TextView tv = child.findViewById(R.id.tvMessage);
                    if (tv != null) tv.setText(message);
                }
                break;
            }
        }
        scrollToBottom();
    }

    private String getFriendlySpecialtyName(String key) {
        if (key == null) return "un médecin généraliste";
        switch (key.toLowerCase()) {
            case "cardiologie": return "un cardiologue";
            case "dermatologie": return "un dermatologue";
            case "psychiatrie": return "un psychiatre";
            case "neurologie": return "un neurologue";
            case "gastroenterologie": return "un gastro-entérologue";
            case "pneumologie": return "un pneumologue";
            case "medecine_generale": return "un médecin généraliste";
            case "maladies_rares": return "un spécialiste des maladies rares";
            default: return "un spécialiste (" + key + ")";
        }
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void clearChat() {
        chatContainer.removeAllViews();
        addBotMessage("Nouvelle conversation démarrée.\nComment puis-je vous aider ?");
    }

    private void saveToHistory(String prompt, Map<String, Object> responseMap) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("prompt", prompt);
            
            String jsonStr = new com.google.gson.Gson().toJson(responseMap);
            obj.put("response", new org.json.JSONObject(jsonStr));
            
            historyItems.add(0, obj);
            
            if (historyItems.size() > 15) {
                historyItems.remove(15);
            }
            if (historyAdapter != null) historyAdapter.notifyDataSetChanged();
            
            android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("SympNetPrefs", android.content.Context.MODE_PRIVATE);
            org.json.JSONArray arr = new org.json.JSONArray(historyItems);
            prefs.edit().putString("ai_history_full", arr.toString()).apply();
        } catch (Exception e) {}
    }

    private void loadHistory() {
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("SympNetPrefs", android.content.Context.MODE_PRIVATE);
        String hist = prefs.getString("ai_history_full", "[]");
        try {
            org.json.JSONArray arr = new org.json.JSONArray(hist);
            historyItems.clear();
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                historyItems.add(obj);
            }
        } catch (Exception e) {}
    }
}