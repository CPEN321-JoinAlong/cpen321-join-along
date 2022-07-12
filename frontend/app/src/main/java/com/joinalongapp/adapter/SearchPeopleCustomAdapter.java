package com.joinalongapp.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.joinalong.R;
import com.joinalongapp.navbar.ViewProfileFragment;
import com.joinalongapp.viewmodel.UserProfile;

import java.util.List;

public class SearchPeopleCustomAdapter extends RecyclerView.Adapter<SearchPeopleCustomAdapter.ViewHolder>{

    private List<UserProfile> dataset;
    private FragmentTransaction context;
    private Fragment fragment;


    public SearchPeopleCustomAdapter(List<UserProfile> dataset) {
        this.dataset = dataset;
        this.context = context;
        this.fragment = fragment;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private View.OnClickListener onItemClickListener;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setTag(this);
            name = (TextView) itemView.findViewById(R.id.entryName);
            itemView.setOnClickListener(onItemClickListener);

        }

        public TextView getName() {
            return name;
        }

        public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent,false);

        return new SearchPeopleCustomAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.getName().setText((dataset.get(holder.getBindingAdapterPosition())).getFullName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ViewProfileFragment viewProfileFragment = new ViewProfileFragment();
                Bundle info = new Bundle();
                info.putBoolean("HIDE", false);
                info.putSerializable("USER_INFO", dataset.get(holder.getBindingAdapterPosition()));
                viewProfileFragment.setArguments(info);
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frameLayoutSearch, viewProfileFragment);
                //fragmentTransaction.hide(activity.getSupportFragmentManager().findFragmentById(R.id.frameLayoutSearch));
                //fragmentTransaction.add(R.id.frameLayoutSearch, viewProfileFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
    }

    @Override
    public int getItemCount() {
        if(dataset == null){
            return 0;
        }
        else{
            return dataset.size();
        }
    }

    public void changeDataset(List<UserProfile> input){
        dataset = input;
        notifyDataSetChanged();
    }


}
