package com.kreyos.kreyosandroid.customcarouselslider;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.fragments.FragmentLeft4;
import com.kreyos.kreyosandroid.utilities.Constants;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CarouselFragment extends Fragment {

    private CarouselLinearLayout mRoot;

	public static Fragment newInstance(FragmentLeft4 pFragment, int pPosition,
			float pScale)
	{
        Log.d(Constants.TAG_DEBUG, "( CarouselFragment ) - New Instance");

		Bundle b = new Bundle();


        b.putInt("pos", pPosition);
        b.putFloat("scale", pScale);
        CarouselFragment fragmentCarousel = new CarouselFragment();
        fragmentCarousel.setArguments(b);
//        FragmentTransaction transaction = pFragment.getChildFragmentManager().beginTransaction();
//        transaction.add(R.id.view_pager, fragmentC ).commit();

//        b.putInt("pos", pPosition);
//        b.putFloat("scale", pScale);
//        return Fragment.instantiate(pFragment.getActivity(), CarouselFragment.class.getName(), b);
        return fragmentCarousel;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

        Log.d(Constants.TAG_DEBUG, "( CarouselFragment ) - On Create View");
        Log.d(Constants.TAG_DEBUG, "( CarouselFragment ) - set tag : " + this.getArguments().getInt("pos"));
		
		LinearLayout l = (LinearLayout) 
				inflater.inflate(R.layout.item_daily_target_slider, container, false);
		
		int pos = this.getArguments().getInt("pos");
		TextView tv = (TextView) l.findViewById(R.id.text);
		tv.setText("" + (pos+1) + "K");
		
//		CarouselLinearLayout root = (CarouselLinearLayout) l.findViewById(R.id.root);
//		float scale = this.getArguments().getFloat("scale");
//		root.setScaleBoth(scale);

        mRoot = (CarouselLinearLayout) l.findViewById(R.id.root);
        float scale = this.getArguments().getFloat("scale");
        mRoot.setScaleBoth(scale);

		return l;
	}

    public CarouselLinearLayout getRootLayout() {
        return mRoot;
    }
}
