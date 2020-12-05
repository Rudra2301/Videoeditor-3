package com.example.enix.videoeditor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by eNIX on 22-Aug-17.
 */

public class Filter_adapter extends RecyclerView.Adapter<Filter_adapter.Myviewholder> {

    Context context;
    List<String> option_menu;

    public Filter_adapter(Context context, List<String> option_menu) {

        this.context=context;
        this.option_menu=option_menu;


    }

    @Override
    public Filter_adapter.Myviewholder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.filter_view, parent, false);
        Myviewholder viewHolder = new Myviewholder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(Filter_adapter.Myviewholder holder, int position) {


        holder.textfilter.setText(option_menu.get(position));
    }

    @Override
    public int getItemCount() {
        return option_menu.size();
    }

    public class Myviewholder extends RecyclerView.ViewHolder {

        public TextView textfilter;
        public Myviewholder(View itemView) {
            super(itemView);

            textfilter = (TextView) itemView.findViewById(R.id.textfilter);
        }
    }
}
