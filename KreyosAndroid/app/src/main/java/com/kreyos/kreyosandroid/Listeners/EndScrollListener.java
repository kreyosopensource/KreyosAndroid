package com.kreyos.kreyosandroid.listeners;

import android.content.Context;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class EndScrollListener implements OnScrollListener {

    private int m_visibleThreshold 		= 0;
    private int m_currentPage			= 0;
    private int m_previousTotal 		= 0;
    private boolean m_loading 			= false;
    private IEndScroll m_delegate		= null;

    public EndScrollListener( IEndScroll p_delegate ) {
    	m_visibleThreshold 		= 4;
    	m_currentPage			= 0;
    	m_previousTotal 		= 0;
    	m_loading 				= true;
    	m_delegate				= p_delegate;
    }
    
    public EndScrollListener( int visibleThreshold ) {
    	m_visibleThreshold 		= visibleThreshold;
    	m_currentPage			= 0;
    	m_previousTotal 		= 0;
    	m_loading 				= true;
    	m_delegate				= null;
    }
    
    public void setDelegate( IEndScroll p_delegate ) {
    	m_delegate = p_delegate;
    }
    
    public void noScrollLoaded() {
    	Log.i( "EndScrollListener::noScrollLoaded", "No scroll loaded D:" );
    	m_loading = false;
    }

    @Override
    public void onScroll( 
    	AbsListView view, 
    	int firstVisibleItem,
    	int visibleItemCount, 
    	int totalItemCount
    ) {
        if( m_loading ) {
            if( totalItemCount > m_previousTotal ) {
                m_loading = false;
                m_previousTotal = totalItemCount;
                m_currentPage++;
            }
        }
        
        if( !m_loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + m_visibleThreshold) ) {
        	m_loading = true;
        	// Load the crappy data here
        	m_delegate.loadMoreScroll();
        }
    }

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

}