package micropolis.android;

import micropolisj.engine.*;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.AttributeSet;
import android.view.View;

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

	@Override
	public void onDraw(Canvas canvas)
	{
		Paint p = new Paint();

		for (int y = 0; y < 32; y++) {
			for (int x = 0; x < 32; x++) {
				int t = city.getTile(x, y) & TileConstants.LOMASK;
				t = t % 64;

				canvas.drawBitmap(tilesBitmap,
					new Rect(0, t*32, 32, t*32+32),
					new Rect(x*32, y*32, x*32+32, y*32+32),
					p);
			}
		}
	}
}
