package com.joinalongapp.joinalong.ui.messaging;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.joinalongapp.joinalong.databinding.FragmentMessagingBinding;

public class MessagingFragment extends Fragment {

    private FragmentMessagingBinding binding;

    public MessagingFragment() {

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MessagingViewModel notificationsViewModel =
                new ViewModelProvider(this).get(MessagingViewModel.class);

        binding = FragmentMessagingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textMessaging;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}