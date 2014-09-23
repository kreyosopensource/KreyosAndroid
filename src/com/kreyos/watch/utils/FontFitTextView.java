package com.kreyos.watch.utils;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class FontFitTextView extends TextView
{
	
	  private float maxTextSizePx;
	
	  public FontFitTextView(Context context)
	  {
	    super(context);
	    initialise();
	  }
	
	  public FontFitTextView(Context context, AttributeSet attrs)
	  {
	    super(context, attrs);
	    initialise();
	  }
	
	  public FontFitTextView(Context context, AttributeSet attrs, int defStyle)
	  {
	    super(context, attrs, defStyle);
	    initialise();
	  }
	
	  /** Sets the maximum text size as the text size specified to use for this View.*/
	  private void initialise()
	  {
	    maxTextSizePx = getTextSize(); 
	  }

	  /** Reduces the font size continually until the specified 'text' fits within the View (i.e. the specified 'viewWidth').*/
	  private void refitText(String text, int viewWidth)
	  { 
	    if (viewWidth > 0)
	    {
	      TextPaint textPaintClone = new TextPaint();
	      textPaintClone.set(getPaint());
	
	      int availableWidth = viewWidth - getPaddingLeft() - getPaddingRight();
	      float trySize = maxTextSizePx;
	
	      // note that Paint text size works in px not sp
	      textPaintClone.setTextSize(trySize); 
	
	      while (textPaintClone.measureText(text) > availableWidth)
	      {
	        trySize--;
	        textPaintClone.setTextSize(trySize);
	      }
	
	      setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
	    }
	  }

	  @Override
	  protected void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter)
	  {
	    super.onTextChanged(text, start, lengthBefore, lengthAfter);
	
	    refitText(text.toString(), getWidth());
	  }

	  @Override
	  protected void onSizeChanged(int w, int h, int oldw, int oldh)
	  {
	    super.onSizeChanged(w, h, oldw, oldh);
	
	    if (w != oldw)
	      refitText(getText().toString(), w);
	  }
}
