package com.sympnet.app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sympnet.app.fragments.Onboardingfragment;

/**
 * Drives the 4-page onboarding ViewPager2.
 * Each page is an Onboardingfragment configured via static factory args.
 */
public class OnboardingPagerAdapter extends FragmentStateAdapter {

    public OnboardingPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return Onboardingfragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}