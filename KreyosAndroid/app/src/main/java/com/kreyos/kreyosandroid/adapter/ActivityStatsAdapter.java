package com.kreyos.kreyosandroid.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.objectdata.ActivityDataRow;


public class ActivityStatsAdapter extends ArrayAdapter<ActivityDataRow> {

    Context context;

    public ActivityStatsAdapter(Context context, int resourceId,
                                ArrayList<ActivityDataRow> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {

        TextView textView_title;
        TextView textView_times;
        ImageView imageView_activitystats;
        TextView textView_steps;
        TextView textView_distance;
        TextView textView_calories;
        TextView textView_speed;
        TextView textView_averagespeed;
        TextView textView_topSpeed;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ActivityDataRow rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_unit_activity_values, null);
            holder = new ViewHolder();
            holder.textView_steps 			= (TextView) convertView.findViewById(R.id.text_value_steps);
            holder.textView_distance 		= (TextView) convertView.findViewById(R.id.text_value_distance);
            holder.textView_calories 		= (TextView) convertView.findViewById(R.id.text_value_calories);
            holder.textView_speed 			= (TextView) convertView.findViewById(R.id.text_value_speed);
            holder.textView_averagespeed 	= (TextView) convertView.findViewById(R.id.text_value_average_speed);
            holder.textView_topSpeed 		= (TextView) convertView.findViewById(R.id.text_value_top_speed);

            holder.textView_title			= (TextView) convertView.findViewById(R.id.label_title);
            holder.textView_times = (TextView) convertView.findViewById(R.id.text_time);
            holder.imageView_activitystats = (ImageView) convertView.findViewById(R.id.img_icon_activity);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }


        DecimalFormat f = new DecimalFormat("##.##");


        double steps = rowItem.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0);
        double distance = rowItem.data.get(ActivityDataRow.DataType.DATA_COL_DIST, 0.0);
        double calories = rowItem.data.get(ActivityDataRow.DataType.DATA_COL_CALS, 0.0);
        double speed 	= 0;
        double avespeed = 0;
        double topspeed = 0;
        if (steps > 0) {
            speed = (distance / (steps * 0.5));
            avespeed = speed * 0.7f;
            topspeed = speed * 1.1f;
        }

        holder.textView_steps.setText(""+f.format(steps));
        holder.textView_distance.setText(""+f.format(distance));
        holder.textView_calories.setText(""+f.format(calories));
        holder.textView_speed.setText(""+f.format(speed));
        holder.textView_averagespeed.setText(""+f.format(avespeed));
        holder.textView_topSpeed.setText(""+f.format(topspeed));

        // Random text title
        String titleText = "That's Fantastic!";

        holder.textView_title.setText(titleText);


        String hourValue 		= rowItem.hour < 10 ? ("0" + rowItem.hour) 		: ("" + rowItem.hour);
        String minuteValue 	= rowItem.minute < 10 ? ("0" + rowItem.minute) 	: ("" + rowItem.minute);

        holder.textView_times.setText(hourValue+":"+minuteValue);
        switch (rowItem.mode)
        {
            case 0:
                holder.imageView_activitystats.setImageResource(R.drawable.frag2_ic_activity);
                break;
            case 1:
                holder.imageView_activitystats.setImageResource(R.drawable.frag2_ic_running);
                break;
            case 2:
                holder.imageView_activitystats.setImageResource(R.drawable.frag2_ic_cycling);
                break;
            default:
                break;
        }

        return convertView;
    }
}