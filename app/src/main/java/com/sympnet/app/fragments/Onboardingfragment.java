package com.sympnet.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sympnet.app.R;

public class Onboardingfragment extends Fragment {

    private static final String ARG_PAGE = "page_index";

    public static Onboardingfragment newInstance(int pageIndex) {
        Onboardingfragment f = new Onboardingfragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageIndex);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        int page = getArguments() != null ? getArguments().getInt(ARG_PAGE, 0) : 0;

        int layoutRes;
        switch (page) {
            case 1:  layoutRes = R.layout.fragment_onboarding_2; break;
            case 2:  layoutRes = R.layout.fragment_onboarding_3; break;
            case 3:  layoutRes = R.layout.fragment_onboarding_4; break;
            default: layoutRes = R.layout.fragment_onboarding_1; break;
        }

        View root = inflater.inflate(layoutRes, container, false);
        animateChildren(root);
        return root;
    }

    private void animateChildren(View root) {
        if (!(root instanceof ViewGroup)) return;
        ViewGroup innerContainer = findFirstLinearLayout((ViewGroup) root);
        if (innerContainer == null) return;

        int count = innerContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = innerContainer.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(40f);
            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(120L * i)
                    .setDuration(450)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f))
                    .start();
        }
    }

    private ViewGroup findFirstLinearLayout(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof LinearLayout) {
                return (ViewGroup) child;
            }
        }
        return null;
    }
}
