package com.joinalongapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.joinalongapp.joinalong.R;
import com.joinalongapp.viewmodel.Event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> homepageEventList;
    private ItemClickListener clickListener;
    private String[] colors;
    private final int NUMBERS_OF_COLORS = 12;

    public EventAdapter(Context context, List<Event> eventArrayList) {
        this.context = context;
        this.homepageEventList = eventArrayList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.event_cards_layout, parent, false);
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            colors = parent.getResources().getStringArray(R.array.list_of_colors_dark);
        }
        else{
            colors = parent.getResources().getStringArray(R.array.list_of_colors_light);
        }
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.slide_in_left);
        Event model = homepageEventList.get(position);
        String eventTitleString = model.getTitle();
        String eventDescriptionString = model.getOwnerName();
        Date eventDate = model.getBeginningDate();
        String eventLocation = model.getLocation();
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMMM d");
        String result = dateFormat.format(eventDate);

        String color = colors[position % NUMBERS_OF_COLORS];
        holder.eventRelativeLayout.setStrokeColor(Color.parseColor(color));
        holder.eventLocation.setText(eventLocation);
        holder.eventTitle.setText(eventTitleString);
        holder.eventDescription.setText(eventDescriptionString);
        holder.eventDate.setText(result);
        holder.itemView.startAnimation(animation);
        //TODO: add any button on click listeners for CardView buttons here
    }

    @Override
    public int getItemCount() {
        return homepageEventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView eventTitle;
        private TextView eventDescription;
        private TextView eventDate;
        private TextView eventLocation;
        private MaterialCardView eventRelativeLayout;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.cardViewEventName);
            eventDescription = itemView.findViewById(R.id.cardViewEventDescription);
            eventDate = itemView.findViewById(R.id.cardViewEventDate);
            eventLocation = itemView.findViewById(R.id.cardViewDistance);
            eventRelativeLayout = itemView.findViewById(R.id.eventMaterialCardView);
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
