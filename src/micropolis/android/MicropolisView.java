package micropolis.android;

import micropolisj.engine.*;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.AttributeSet;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.*;
import android.widget.OverScroller;

public class MicropolisView extends View
{
	Micropolis city;
	Bitmap [] tilesBitmap;
	Rect scrollBounds = new Rect();
	Matrix renderMatrix = new Matrix();

	int tileSize = 32;
	float originX = 0.0f;
	float originY = 0.0f;

	float scaleFocusX = 0.0f;
	float scaleFocusY = 0.0f;
	float scaleFactor = 1.0f;

	MicropolisTool currentTool = null;

	public MicropolisView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		loadTilesBitmap();
	}

	public void setCity(Micropolis newCity)
	{
		this.city = newCity;

		city.addMapListener(new MapListener() {
			public void mapOverlayDataChanged(MapState overlayDataType) {}
			public void spriteMoved(Sprite sprite) {}
			public void tileChanged(int xpos, int ypos) {
				onTileChanged(xpos, ypos);
			}
			public void wholeMapChanged() {
				invalidate();
			}
			});
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		scrollBounds.left = -(w/2);
		scrollBounds.top = -(h/2);
		scrollBounds.right = scrollBounds.left + tileSize*city.getWidth();
		scrollBounds.bottom = scrollBounds.top + tileSize*city.getHeight();
	}

	private void loadTilesBitmap()
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		this.tilesBitmap = new Bitmap[32];
		for (int i = 0; i < tilesBitmap.length; i++) {
			String s = String.format("tiles%02d", i);
			int rid = getResources().getIdentifier(s, "drawable", "micropolis.android");
			tilesBitmap[i] = BitmapFactory.decodeResource(
					getResources(), rid, options
					);
		}
	}

	private void updateRenderMatrix()
	{
		renderMatrix.reset();
		if (scaleFactor != 1.0f) {
			renderMatrix.preScale(scaleFactor, scaleFactor, scaleFocusX, scaleFocusY);
		}
		renderMatrix.preTranslate(-originX, -originY);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		Paint p = new Paint();

		canvas.save();
		canvas.concat(renderMatrix);

		Rect bounds = canvas.getClipBounds();
		int minY = bounds.top / tileSize;
		int maxY = bounds.bottom / tileSize + 1;
		int minX = bounds.left / tileSize;
		int maxX = bounds.right / tileSize + 1;

		minY = Math.max(minY, 0);
		maxY = Math.min(maxY, city.getHeight());
		minX = Math.max(minX, 0);
		maxX = Math.min(maxX, city.getWidth());

		for (int y = minY; y < maxY; y++) {
			for (int x = minX; x < maxX; x++) {
				int t = city.getTile(x, y) & TileConstants.LOMASK;
				int tz = t % 32;

				canvas.drawBitmap(tilesBitmap[t/32],
					new Rect(0, tz*tileSize, tileSize, tz*tileSize+tileSize),
					new Rect(x*tileSize, y*tileSize, x*tileSize+tileSize, y*tileSize+tileSize),
					p);
			}
		}

		canvas.restore();
//		if (activeScroller != null) {
//			String s = "Scroller: " + activeScroller.toString();
//			canvas.drawText(s,
//				0.0f, 25.0f,
//				p);
//		}
	}

	class MyGestureListener extends GestureDetector.SimpleOnGestureListener
			implements ScaleGestureDetector.OnScaleGestureListener
	{
		@Override
		public boolean onDown(MotionEvent ev)
		{
			stopMomentum();
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			originX += distanceX;
			originY += distanceY;

			if (originX < scrollBounds.left) {
				originX = scrollBounds.left;
			}
			if (originX > scrollBounds.right) {
				originX = scrollBounds.right;
			}

			if (originY < scrollBounds.top) {
				originY = scrollBounds.top;
			}
			if (originY > scrollBounds.bottom) {
				originY = scrollBounds.bottom;
			}

			updateRenderMatrix();
			invalidate();
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			startMomentum(velocityX, velocityY);
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent evt)
		{
			processTool(evt.getX(), evt.getY());
			return true;
		}

		// implements OnScaleGestureListener
		public boolean onScale(ScaleGestureDetector d)
		{
			scaleFocusX = d.getFocusX();
			scaleFocusY = d.getFocusY();
			scaleFactor *= d.getScaleFactor();
			scaleFactor = Math.min(Math.max(scaleFactor, 0.5f), 2.0f);
			updateRenderMatrix();
			invalidate();
			return true;
		}

		// implements OnScaleGestureListener
		public boolean onScaleBegin(ScaleGestureDetector d)
		{
			return true;
		}

		// implements OnScaleGestureListener
		public void onScaleEnd(ScaleGestureDetector d)
		{
		}
	}
	MyGestureListener mgl = new MyGestureListener();
	GestureDetector gestDetector = new GestureDetector(getContext(), mgl);
	ScaleGestureDetector scaleDetector = new ScaleGestureDetector(getContext(), mgl);

	@Override
	public boolean onTouchEvent(MotionEvent evt)
	{
		boolean x1 = gestDetector.onTouchEvent(evt);
		boolean x2 = scaleDetector.onTouchEvent(evt);
		return x1 || x2;
	}

	MyScrollStep activeScroller = null;
	Handler myHandler = new Handler();

	class MyScrollStep implements Runnable
	{
		OverScroller s = new OverScroller(getContext());

		MyScrollStep(float velX, float velY)
		{
			s.fling((int)originX, (int)originY, (int)-velX, (int)-velY,
				scrollBounds.left, scrollBounds.right,
				scrollBounds.top, scrollBounds.bottom,
				tileSize*4, tileSize*4);
		}

		public void run()
		{
			if (activeScroller == this) {

				boolean activ = s.computeScrollOffset();

				originX = s.getCurrX();
				originY = s.getCurrY();
				updateRenderMatrix();
				invalidate();

				if (!activ) {
					activeScroller = null;
				}
				else {
					myHandler.postDelayed(this, 100);
				}
			}
		}

		@Override
		public String toString()
		{
			return "X: "+s.getCurrX()+" ("+s.getFinalX()+")";
		}
	}

	private void startMomentum(float velX, float velY)
	{
		this.activeScroller = new MyScrollStep(velX, velY);
		myHandler.postDelayed(activeScroller, 100);
	}

	private void stopMomentum()
	{
		this.activeScroller = null;
	}

	private void processTool(float x, float y)
	{
		try {

		CityLocation loc = getLocation(x, y);
		if (currentTool != null) {
			currentTool.apply(city, loc.x, loc.y);
		}

		}
		catch (Throwable e)
		{
			AlertDialog alert = new AlertDialog.Builder(getContext()).create();
			alert.setTitle("Error");
			alert.setMessage(e.toString());
			alert.show();
		}
	}

	private CityLocation getLocation(float x, float y)
	{
		Matrix aMatrix = new Matrix();
		if (!renderMatrix.invert(aMatrix)) {
			return new CityLocation(0,0);
		}

		float [] pts = new float[] { x, y };
		aMatrix.mapPoints(pts);

		return new CityLocation(
			(int)pts[0] / tileSize,
			(int)pts[1] / tileSize
			);
	}

	void setTool(MicropolisTool tool)
	{
		this.currentTool = tool;
	}

	private void onTileChanged(int xpos, int ypos)
	{
		//TODO- only invalidate the area where this tile is drawn
		invalidate();
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{
		Bundle b = new Bundle();
		b.putParcelable("superState", super.onSaveInstanceState());
		b.putFloat("scaleFactor", scaleFactor);
		b.putFloat("originX", originX);
		b.putFloat("originY", originY);
		return b;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		if (state instanceof Bundle) {
			Bundle b = (Bundle) state;
			super.onRestoreInstanceState(b.getParcelable("superState"));
			scaleFactor = b.getFloat("scaleFactor");
			originX = b.getFloat("originX");
			originY = b.getFloat("originY");

			if (scaleFactor < 0.5f) {
				scaleFactor = 0.5f;
			}
			updateRenderMatrix();
		}
		else {
			super.onRestoreInstanceState(state);
		}
	}

}
