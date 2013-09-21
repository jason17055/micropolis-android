package micropolis.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;

import java.util.HashMap;
import java.util.Map;

class TileCache
{
	Context appContext;
	int tileSize;
	Bitmap [] tilesBitmap;

	private TileCache(Context appContext, int tileSize)
	{
		this.appContext = appContext;
		this.tileSize = tileSize;
		loadTilesBitmap();
	}

	private static Map<Integer,TileCache> INSTANCES
		= new HashMap<Integer,TileCache>();
	public static TileCache getInstance(Context aContext, int tileSize)
	{
		TileCache tc = INSTANCES.get(tileSize);
		if (tc == null) {
			tc = new TileCache(
					aContext.getApplicationContext(),
					tileSize
				);
			INSTANCES.put(tileSize, tc);
		}
		return tc;
	}

	static final int SLICE_COUNT = 16;
	static final int SLICE_SIZE = 64;

	private void loadTilesBitmap()
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		this.tilesBitmap = new Bitmap[SLICE_COUNT];
		for (int i = 0; i < tilesBitmap.length; i++) {
			String s = String.format("tiles%d_%02d", tileSize, i);
			int rid = getResources().getIdentifier(s, "drawable", "micropolis.android");
			tilesBitmap[i] = BitmapFactory.decodeResource(
					getResources(), rid, options
					);
		}
	}

	public void drawTo(Canvas canvas, int tileNumber, int xpos, int ypos)
	{
		drawTo(canvas, tileNumber,
			new Rect(xpos*tileSize, ypos*tileSize, xpos*tileSize+tileSize, ypos*tileSize+tileSize)
			);
	}

	public void drawTo(Canvas canvas, int tileNumber, Rect dest)
	{
		int tm = tileNumber / SLICE_SIZE;
		int tn = tileNumber % SLICE_SIZE;

		canvas.drawBitmap(tilesBitmap[tm%SLICE_COUNT],
			new Rect(0, tn*tileSize, tileSize, tn*tileSize+tileSize),
			dest,
			null /*paint*/
			);
	}

	/** Convenience method. */
	protected final Resources getResources()
	{
		return appContext.getResources();
	}
}
