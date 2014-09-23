package com.kreyos.watch.customcarouselslider;

import android.content.Context;
import android.support.v4.view.ViewPager;

public class CarouselManager {

	public static int SELECTED_PAGE = 0;
	public static final int PAGES = 30;
	// You can choose a bigger number for LOOPS, but you know, nobody will fling
	// more than 1000 times just in order to test your "infinite" ViewPager :D 
	public static final int LOOPS = 1000; 
	public static final int FIRST_PAGE = PAGES * LOOPS / 2;
	public static final float BIG_SCALE = 1.0f;
	public static final float SMALL_SCALE = 0.65f;
	public static final float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;
	
	
	private Context mContext;
	public CarouselPageAdapter mAdapter;
	public ViewPager mPager;
	
	public CarouselManager(Context p_context, ViewPager p_pager, CarouselPageAdapter p_adapter) {
		// TODO:
		mContext = p_context;
		mPager = p_pager;
		mAdapter = p_adapter;
	}
	
	public void init(int p_center, int p_margin , int p_maxOnScreen) {
		// TODO:
		mPager.setAdapter(mAdapter);
		mPager.setOnPageChangeListener(mAdapter);
		
		// Set current item to the middle page so we can fling to both
		// directions left and right
		mPager.setCurrentItem(FIRST_PAGE + (p_center - 1));
		
		// Necessary or the pager will only have one extra page to show
		// make this at least however many pages you can see
		mPager.setOffscreenPageLimit(p_maxOnScreen);
		
		// Set margin for pages as a negative number, so a part of next and 
		// previous pages will be showed
		mPager.setPageMargin(p_margin); //800 //350
	}
	
	public int getSelectedPage() {
		// TODO:
		return (SELECTED_PAGE -  (PAGES * LOOPS / 2)) + 1;
	}
}
