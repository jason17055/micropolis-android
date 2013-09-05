package micropolis.android;

import micropolisj.engine.*;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.AttributeSet;
import android.view.*;

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
	}
	GestureDetector gestDetector = new GestureDetector(getContext(), new MyGestureListener());

	@Override
	public boolean onTouchEvent(MotionEvent evt)
	{
		return gestDetector.onTouchEvent(evt);
	}
}
