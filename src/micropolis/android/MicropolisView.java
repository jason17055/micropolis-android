package micropolis.android;

import micropolisj.engine.*;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.AttributeSet;
import android.os.Handler;
import android.view.*;
import android.widget.OverScroller;

public class MicropolisView extends View
{
	Micropolis city;
	Paint piePaint;
	Paint bluePaint;
	Bitmap tilesBitmap;
	Rect scrollBounds = new Rect();

	public MicropolisView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		city = new Micropolis();
		new MapGenerator(city).generateNewCity();

		piePaint = new Paint();
		piePaint.setColor(0xffff0000);
		piePaint.setStyle(Paint.Style.FILL);

		bluePaint = new Paint();
		bluePaint.setColor(0xff0000ff);

		loadTilesBitmap();
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
		this.tilesBitmap = BitmapFactory.decodeResource(
				getResources(),
				R.drawable.tiles,
				options);
	}

	int tileSize = 32;
	float originX = 0.0f;
	float originY = 0.0f;

	float scaleFocusX = 0.0f;
	float scaleFocusY = 0.0f;
	float scaleFactor = 1.0f;

	@Override
	public void onDraw(Canvas canvas)
	{
		Paint p = new Paint();

		canvas.save();
		if (scaleFactor != 1.0f) {
			canvas.scale(scaleFactor, scaleFactor, scaleFocusX, scaleFocusY);
		}
		canvas.translate(-originX, -originY);

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
				t = t % 64;

				canvas.drawBitmap(tilesBitmap,
					new Rect(0, t*tileSize, tileSize, t*tileSize+tileSize),
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

			invalidate();
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			startMomentum(velocityX, velocityY);
			return true;
		}

		// implements OnScaleGestureListener
		public boolean onScale(ScaleGestureDetector d)
		{
			scaleFocusX = d.getFocusX();
			scaleFocusY = d.getFocusY();
			scaleFactor *= d.getScaleFactor();
			scaleFactor = Math.min(Math.max(scaleFactor, 0.5f), 2.0f);
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
}
