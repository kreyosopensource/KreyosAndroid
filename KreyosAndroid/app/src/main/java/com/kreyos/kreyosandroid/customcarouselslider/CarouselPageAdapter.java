package com.kreyos.kreyosandroid.customcarouselslider;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.fragments.FragmentLeft4;
import com.kreyos.kreyosandroid.utilities.Constants;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class CarouselPageAdapter extends FragmentPagerAdapter implements
		ViewPager.OnPageChangeListener {

	private CarouselLinearLayout cur = null;
	private CarouselLinearLayout next = null;
	private FragmentLeft4 mParentFragment;
	private FragmentManager fm;
	private float scale;

	public CarouselPageAdapter(FragmentLeft4 pParentFragment, FragmentManager fm) {
		super(fm);
		this.fm = fm;
		this.mParentFragment = pParentFragment;

        Log.d(Constants.TAG_DEBUG, "( CarouselPageAdapter ) - constructor");
	}

	@Override
	public Fragment getItem(int position) 
	{
        Log.d(Constants.TAG_DEBUG, "( CarouselPageAdapter ) - Get item");

        // make the first pager bigger than others
        if (position == CarouselManager.FIRST_PAGE)
        	scale = CarouselManager.BIG_SCALE;     	
        else
        	scale = CarouselManager.SMALL_SCALE;
        
        position = position % CarouselManager.PAGES;



        return CarouselFragment.newInstance(mParentFragment, position, scale);
	}

	@Override
	public int getCount()
	{
        Log.d(Constants.TAG_DEBUG, "( CarouselPageAdapter ) - Get count");
		return CarouselManager.PAGES * CarouselManager.LOOPS;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) 
	{
        Log.d(Constants.TAG_DEBUG, "( CarouselPageAdapter ) - On Page Scrolled");

		adjustScale(position, positionOffset, positionOffsetPixels);
	}
	
	
	public void adjustScale(int position, float positionOffset, 
			int positionOffsetPixels) {

        Log.d(Constants.TAG_DEBUG, "( CarouselPageAdapter ) -Adjust scale");

		//TODO :
		if (positionOffset >= 0f && positionOffset <= 1f)
		{


			cur = getRootView(position);
			next = getRootView(position +1);

			cur.setScaleBoth(CarouselManager.BIG_SCALE
					- CarouselManager.DIFF_SCALE * positionOffset);
			next.setScaleBoth(CarouselManager.SMALL_SCALE
					+ CarouselManager.DIFF_SCALE * positionOffset);
		}
	}

	@Override
	public void onPageSelected(int position) {
		CarouselManager.SELECTED_PAGE = position;
	}
	
	@Override
	public void onPageScrollStateChanged(int state) {}
	
	private CarouselLinearLayout getRootView(int position)
	{
        Log.d(Constants.TAG_DEBUG, "( CarouselPageAdapter ) - Get Root View");

        CarouselFragment fragment = (CarouselFragment) fm.findFragmentByTag(this.getFragmentTag(position));

        if ( fragment == null ) {
            Log.d(Constants.TAG_DEBUG, "( CarouselPageAdapter ) - Get Root View !!!! NULL");
        }

        CarouselLinearLayout layout = fragment.getRootLayout();


		return layout;
	}
	
	private String getFragmentTag(int pPosition)
	{
        int idViewPager = mParentFragment.mPager.getId();
//        Log.d(Constants.TAG_DEBUG, "( CarouselPageAdapter ) - getFragmentTag : " + "android:switcher:" + mParentFragment.mPager.getId() + ":" + pPosition);
        Log.d(Constants.TAG_DEBUG, "( CarouselPageAdapter ) - getFragmentTag : " + "android:switcher:" + idViewPager + ":" + pPosition);
        return "android:switcher:" + idViewPager + ":" + pPosition;
	}
}