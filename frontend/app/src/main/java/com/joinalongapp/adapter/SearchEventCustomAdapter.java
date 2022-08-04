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
import com.joinalongapp.navbar.ViewEventFragment;
import com.joinalongapp.viewmodel.Event;

import java.util.List;

public class SearchEventCustomAdapter extends RecyclerView.Adapter<SearchEventCustomAdapter.ViewHolder>{
    private List<Event> dataset;
    private FragmentTransaction context;
    private Fragment fragment;


    public SearchEventCustomAdapter(List<Event> dataset) {
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
    public SearchEventCustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent,false);

        return new SearchEventCustomAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchEventCustomAdapter.ViewHolder holder, int position) {

        holder.getName().setText((dataset.get(holder.getBindingAdapterPosition())).getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ViewEventFragment viewEventFragment = new ViewEventFragment();
                Bundle info = new Bundle();
                info.putSerializable("event", dataset.get(holder.getBindingAdapterPosition()));
                info.putString("theFrom", "search");
                viewEventFragment.setArguments(info);
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.frameLayoutSearch, viewEventFragment);
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

    public void changeDataset(List<Event> input){
        dataset = input;
        notifyDataSetChanged();
    }
}
