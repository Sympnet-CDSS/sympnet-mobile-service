package com.sympnet.app.activities;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.sympnet.app.R;
import com.sympnet.app.adapters.OnboardingPagerAdapter;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private Button btnNext, btnSkip;
    private OnboardingPagerAdapter adapter;
    private TextView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.dots_layout);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);

        adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);

        setupDots(0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == 3) {
                    btnNext.setText("COMMENCER");
                } else {
                    btnNext.setText("SUIVANT →");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 3) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
                finish();
            }
        });

        btnSkip.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupDots(int currentPosition) {
        dotsLayout.removeAllViews();
        dots = new TextView[4];
        for (int i = 0; i < 4; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("•");
            dots[i].setTextSize(30);
            dots[i].setTextColor(getResources().getColor(R.color.gray_400));
            dotsLayout.addView(dots[i]);
        }
        if (dots.length > 0) {
            dots[currentPosition].setTextColor(getResources().getColor(R.color.teal_500));
        }
    }
}
