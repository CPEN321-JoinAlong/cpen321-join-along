package com.joinalongapp.joinalong.ui.managefriends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.joinalongapp.joinalong.databinding.FragmentFriendsBinding;

public class FriendsFragment extends Fragment {

    private FragmentFriendsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FriendsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(FriendsViewModel.class);

        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textFriends;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}