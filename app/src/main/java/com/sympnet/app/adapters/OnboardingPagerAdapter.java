package com.sympnet.app.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sympnet.app.R;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.ViewHolder> {

    private Context context;
    private int[] icons = {R.drawable.ic_diagnostic, R.drawable.ic_gps, R.drawable.ic_chat, R.drawable.ic_brain};
    private String[] titles = {"Diagnostic Intelligent", "Trouvez votre médecin", "Consultation à distance", "IA Explicable"};
    private String[] subtitles = {
            "5 agents IA analysent vos symptômes et génèrent un score de confiance avec explication complète du raisonnement (XAI). NLP · XAI · SHAP",
            "Trouvez le bon médecin près de vous. L'IA suggère automatiquement les spécialistes adaptés à vos symptômes, triés par distance, disponibilité et note.",
            "Consultez votre médecin en temps réel. Chat multimédia, appel vidéo WebRTC et gestion d'ordonnances numériques — tout depuis votre téléphone.",
            "Comprenez chaque diagnostic grâce à notre IA explicable. Visualisez le raisonnement complet derrière chaque recommandation médicale."
    };
    private int[] colors = {R.color.teal_500, R.color.blue_500, R.color.purple_500, R.color.orange_500};

    public OnboardingPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_onboarding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.icon.setImageResource(icons[position]);
        holder.title.setText(titles[position]);
        holder.subtitle.setText(subtitles[position]);
        holder.icon.setColorFilter(context.getResources().getColor(colors[position]));
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, subtitle;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.onboarding_icon);
            title = itemView.findViewById(R.id.onboarding_title);
            subtitle = itemView.findViewById(R.id.onboarding_subtitle);
        }
    }
}