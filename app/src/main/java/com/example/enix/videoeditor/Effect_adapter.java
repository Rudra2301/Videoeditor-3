package com.example.enix.videoeditor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eNIX on 22-Aug-17.
 */

public class Effect_adapter extends RecyclerView.Adapter<Effect_adapter.Myviewholder> {

    Context context;
    int[] option_menu;
    String[] optionname;
    int selected_position = 0;
    private final ArrayList<Integer> selected = new ArrayList<>();

    public Effect_adapter(Context context, int[] option_menu, String[] optionname) {

        this.context = context;
        this.option_menu = option_menu;
        this.optionname = optionname;

    }


    @Override
    public Effect_adapter.Myviewholder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_effect, parent, false);
        Myviewholder viewHolder = new Myviewholder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(Effect_adapter.Myviewholder holder, int position) {


        holder.imFilter.setImageResource(option_menu[position]);


        holder.optionname.setVisibility(View.GONE);
        holder.optionname.setText(optionname[position]);

        Typeface custom_font = Typeface.createFromAsset(context.getAssets(),  "Champagne_Limousines.ttf");
        holder.optionname.setTypeface(custom_font);


        holder.itemView.setBackgroundColor(selected_position == position ? Color.parseColor("#1adc9e") : Color.parseColor("#282828"));
       /* if (!selected.contains(position)){

            holder.optionname.setTextColor(Color.WHITE);
        }
        else

            holder.optionname.setTextColor(Color.parseColor("#1adc9e"));*/


    }

    @Override
    public int getItemCount() {
        return option_menu.length;
    }

    public class Myviewholder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView imFilter;
        TextView optionname;

        public Myviewholder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            imFilter = (ImageView) itemView.findViewById(R.id.imageitem);
            optionname = (TextView) itemView.findViewById(R.id.optionname);
        }

        @Override
        public void onClick(View view) {

       /*   *//*  optionname.setTextColor(Color.parseColor("#1adc9e"));*//*




            if (selected.isEmpty()){
                selected.add(getAdapterPosition());
            }else {
                int oldSelected = selected.get(0);
                selected.clear();
                selected.add(getAdapterPosition());

                notifyItemChanged(oldSelected);
            }*/

            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            // Updating old as well as new positions
            notifyItemChanged(selected_position);
            selected_position = getAdapterPosition();
            notifyItemChanged(selected_position);

        }
    }

}
