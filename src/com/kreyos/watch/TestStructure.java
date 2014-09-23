package com.kreyos.watch;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class TestStructure {
	
	public int Tag;
	public Matrix CurrentMatrix;
	public Matrix SavedMatrix;
	public Bitmap Image;
	public Paint ImagePaint;
	
	
	public int ProgressPercent;
	public Bitmap ProgressImage;
	public Bitmap ProgressImageMask;
	public Bitmap ProgressPaint;
	
	public TestStructure() {
		
	}
	
	public Rect getRect() {
		
		float[] values = new float[9];
		CurrentMatrix.getValues( values );
		int globalX = Math.round( values[Matrix.MTRANS_X]) ;
		int globalY = Math.round( values[Matrix.MTRANS_Y]);
		int width = Math.round( values[Matrix.MSCALE_X] * Image.getWidth() );
		int height = Math.round( values[Matrix.MSCALE_Y]* Image.getHeight() );
		
		
		return  new Rect(
				globalX,
				globalY,	
				globalX + width, 		
				globalY + height); 
	}
	
	

}

