package com.kreyos.watch;

import com.kreyos.watch.R;
import com.kreyos.watch.ScreenSlidePageFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScreenSlidePageFragment extends Fragment {

	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.snappable_home_activity, container, false);
        setupActivityPanel( rootView );
        return rootView;
    }
    
    private void setupActivityPanel( ViewGroup p_view ) {

		String calorie;
		String distance;
		String steps;
		
		calorie = "0.0";
		distance = "0.0";
		steps = "0";
		

		TextView txt_steps 		= (TextView)p_view.findViewById(R.id.txt_steps);
		TextView txt_distance 	= (TextView)p_view.findViewById(R.id.txt_distance);
		TextView txt_calories 	= (TextView)p_view.findViewById(R.id.txt_calories);
		
		txt_calories.setText(calorie);
		txt_distance.setText(distance);
		txt_steps.setText(steps);
	}
    
    
    

}