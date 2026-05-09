package com.example.projex_mobile.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.projex_mobile.fragments.AuthFragment.LoginFragment;
import com.example.projex_mobile.fragments.AuthFragment.RegisterFragment;

public class AuthPagerAdapter extends FragmentStateAdapter {

    public AuthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        if (position == 0) {
            return new LoginFragment();
        }

        return new RegisterFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}