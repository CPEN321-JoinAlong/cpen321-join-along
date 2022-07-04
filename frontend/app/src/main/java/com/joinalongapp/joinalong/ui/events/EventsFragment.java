package com.joinalongapp.joinalong.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.joinalongapp.joinalong.databinding.FragmentEventBinding;

public class EventsFragment extends Fragment {

    private FragmentEventBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EventsViewModel dashboardViewModel =
                new ViewModelProvider(this).get(EventsViewModel.class);

        binding = FragmentEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textEvent;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}