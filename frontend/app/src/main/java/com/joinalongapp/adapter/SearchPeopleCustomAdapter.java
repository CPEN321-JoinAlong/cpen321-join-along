package com.joinalongapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.UserProfile;

import java.util.List;

public class SearchPeopleCustomAdapter extends RecyclerView.Adapter<SearchPeopleCustomAdapter.ViewHolder>{

    private List<UserProfile> dataset;

    public SearchPeopleCustomAdapter(List<UserProfile> dataset) {
        this.dataset = dataset;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.entryName);
        }

        public TextView getName() {
            return name;
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
                System.out.println(holder.getBindingAdapterPosition());
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


}
