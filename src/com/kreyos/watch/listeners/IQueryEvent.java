package com.kreyos.watch.listeners;

import android.database.Cursor;

public interface IQueryEvent {
	public void onQueryStart( String p_queryKey );
	public void onQueryComplete( String p_queryKey, Cursor p_query );
	public void onQueryError( String p_queryKey, String p_error );
	public void runOnUiThread(Runnable runnable);
}
