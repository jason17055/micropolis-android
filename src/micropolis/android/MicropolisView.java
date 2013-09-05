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

	@Override
	public void onDraw(Canvas canvas)
	{
		Paint p = new Paint();

		canvas.save();
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
		if (activeScroller != null) {
			String s = "Scroller: " + activeScroller.toString();
			canvas.drawText(s,
				0.0f, 25.0f,
				p);
		}
	}

	class MyGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onDown(MotionEvent ev)
		{
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			originX += distanceX;
			originY += distanceY;
			invalidate();
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			startScrolling(velocityX, velocityY);
			return true;
		}
	}
	GestureDetector gestDetector = new GestureDetector(getContext(), new MyGestureListener());

	@Override
	public boolean onTouchEvent(MotionEvent evt)
	{
		return gestDetector.onTouchEvent(evt);
	}

	MyScrollStep activeScroller = null;
	Handler myHandler = new Handler();

	class MyScrollStep implements Runnable
	{
		OverScroller s = new OverScroller(getContext());

		MyScrollStep(float velX, float velY)
		{
			s.fling((int)originX, (int)originY, (int)-velX, (int)-velY,
				0, 32*100, 0, 32*100,
				32*10, 32*10);
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

	private void startScrolling(float velX, float velY)
	{
		this.activeScroller = new MyScrollStep(velX, velY);
		myHandler.postDelayed(activeScroller, 100);
	}
}
