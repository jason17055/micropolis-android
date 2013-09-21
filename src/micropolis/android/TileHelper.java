package micropolis.android;

import android.content.Context;
import android.graphics.*;

public class TileHelper
{
	Context context;
	TileCache curTiles;

	public TileHelper(Context context, int tileSize)
	{
		this.context = context;
		this.curTiles = TileCache.getInstance(context, tileSize);
	}

	public void changeTileSize(int newTileSize)
	{
		curTiles = TileCache.getInstance(context, newTileSize);
	}

	public void drawTo(Canvas canvas, int tileNumber, int xpos, int ypos)
	{
		curTiles.drawTo(canvas, tileNumber, xpos, ypos);
	}
}
