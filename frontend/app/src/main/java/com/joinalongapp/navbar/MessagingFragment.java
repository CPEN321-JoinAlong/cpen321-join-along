package com.joinalongapp.navbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.joinalongapp.joinalong.ManageChatActivity;
import com.joinalongapp.joinalong.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessagingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagingFragment extends Fragment {
    private static final int CHAT_LIST_INDEX = 0;
    private static final int NUMBER_OF_TABS = 2;

    TabLayout tabLayout;
    FragmentManager fragmentManager;
    ViewPager2 viewPager2;
    ImageButton createChat;

    public MessagingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessagingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessagingFragment newInstance(String param1, String param2) {
        MessagingFragment fragment = new MessagingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Trace myTrace = FirebasePerformance.getInstance().newTrace("MessagingFragmentUIComponents");
        myTrace.start();
        View rootView = inflater.inflate(R.layout.fragment_messaging, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        MessagingFragment.ViewStateAdapter viewStateAdapter = new MessagingFragment.ViewStateAdapter(fragmentManager, getLifecycle());
        viewPager2 = (ViewPager2) rootView.findViewById(R.id.messagingViewPager);
        viewPager2.setAdapter(viewStateAdapter);

        tabLayout = rootView.findViewById(R.id.messagingTabLayout);
        createChat = rootView.findViewById(R.id.addButton);

        createChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent manageChat = new Intent(getActivity(), ManageChatActivity.class);
                manageChat.putExtra("EDIT_OPTION", false);
                startActivity(manageChat);
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                System.out.println("TabUnselected");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                System.out.println("TabReselected");
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                //super.onPageSelected(position);
            }
        });


        myTrace.stop();

        return rootView;
    }
    private class ViewStateAdapter extends FragmentStateAdapter {

        public ViewStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if(position == CHAT_LIST_INDEX){
                return new MessagingListFragment();
            }
            else{
                return new MessagingRequestFragment();
            }
        }

        @Override
        public int getItemCount() {
            return NUMBER_OF_TABS;
        }
    }
}