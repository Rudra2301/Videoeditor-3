package com.example.enix.videoeditor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eNIX on 22-Aug-17.
 */

public class Option_time extends RecyclerView.Adapter<Option_time.Myviewholder> {

    Context context;

    String[] optionname;
    int selected_position = 0;

    private final ArrayList<Integer> selected = new ArrayList<>();

    public Option_time(Context context, String[] optionname) {

        this.context = context;
        this.optionname = optionname;

    }


    @Override
    public Option_time.Myviewholder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.filter_view, parent, false);
        Myviewholder viewHolder = new Myviewholder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(Option_time.Myviewholder holder, int position) {


        holder.optionname.setText(optionname[position]);

        Typeface custom_font = Typeface.createFromAsset(context.getAssets(),  "Champagne_Limousines.ttf");
        holder.optionname.setTypeface(custom_font);

        holder.optionname.setTextColor(selected_position == position ? Color.parseColor("#1adc9e") : Color.WHITE);


    }

    @Override
    public int getItemCount() {
        return optionname.length;
    }

    public class Myviewholder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView optionname;

        public Myviewholder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            optionname = (TextView) itemView.findViewById(R.id.textfilter);
        }

        @Override
        public void onClick(View view) {


            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            // Updating old as well as new positions
            notifyItemChanged(selected_position);
            selected_position = getAdapterPosition();
            notifyItemChanged(selected_position);
        }
    }
}
