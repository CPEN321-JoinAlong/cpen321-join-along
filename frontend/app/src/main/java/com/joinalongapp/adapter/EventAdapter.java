package com.joinalongapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.Event;

import java.util.List;
import java.util.Random;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> homepageEventList;
    private ItemClickListener clickListener;

    public EventAdapter(Context context, List<Event> eventArrayList) {
        this.context = context;
        this.homepageEventList = eventArrayList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.event_cards_layout, parent, false);
        //Random random = new Random();
        //int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        //view.setBackgroundColor(color);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event model = homepageEventList.get(position);
        String eventTitleString = model.getTitle();
        String eventDescriptionString = model.getDescription();
        holder.eventTitle.setText(eventTitleString);
        holder.eventDescription.setText(eventDescriptionString);

        //TODO: add any button on click listeners for CardView buttons here
    }

    @Override
    public int getItemCount() {
        return homepageEventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView eventTitle;
        private TextView eventDescription;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.cardViewEventName);
            eventDescription = itemView.findViewById(R.id.cardViewEventDescription);
            itemView.setOnClickListener(this);

            //TODO: add any buttons on CardView here
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public Event getItem(int id) {
        return homepageEventList.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
