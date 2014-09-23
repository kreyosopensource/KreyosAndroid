package com.kreyos.watch.customcarouselslider;

import com.kreyos.watch.DailyTargetActivity;
import com.kreyos.watch.R;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class CarouselPageAdapter extends FragmentPagerAdapter implements
		ViewPager.OnPageChangeListener {

	private CarouselLinearLayout cur = null;
	private CarouselLinearLayout next = null;
	private DailyTargetActivity context;
	private FragmentManager fm;
	private float scale;

	public CarouselPageAdapter(DailyTargetActivity context, FragmentManager fm) {
		super(fm);
		this.fm = fm;
		this.context = context;
	}

	@Override
	public Fragment getItem(int position) 
	{	
        // make the first pager bigger than others
        if (position == CarouselManager.FIRST_PAGE)
        	scale = CarouselManager.BIG_SCALE;     	
        else
        	scale = CarouselManager.SMALL_SCALE;
        
        position = position % CarouselManager.PAGES;
        return CarouselFragment.newInstance(context, position, scale);
	}

	@Override
	public int getCount()
	{		
		return CarouselManager.PAGES * CarouselManager.LOOPS;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) 
	{	
		adjustScale(position, positionOffset, positionOffsetPixels);
	}
	
	
	public void adjustScale(int position, float positionOffset, 
			int positionOffsetPixels) {
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
		return (CarouselLinearLayout) 
				fm.findFragmentByTag(this.getFragmentTag(position))
				.getView().findViewById(R.id.root);
	}
	
	private String getFragmentTag(int position)
	{
		 return "android:switcher:" + context.mPager.getId() + ":" + position;
	}
}