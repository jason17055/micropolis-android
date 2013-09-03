package micropolis.android;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.AttributeSet;
import android.view.View;

public class MicropolisView extends View
{
	Paint piePaint;
	Bitmap tilesBitmap;
	
	public MicropolisView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		piePaint = new Paint();
		piePaint.setColor(0xffff0000);
		piePaint.setStyle(Paint.Style.FILL);
		
		loadTilesBitmap();
	}

	private void loadTilesBitmap()
	{
		Drawable d = getResources().getDrawable(R.drawable.tiles);
		this.tilesBitmap = ((BitmapDrawable) d).getBitmap();
/*		BitmapFactory.Options options = new BitmapFactory.Options();
		this.tilesBitmap = BitmapFactory.decodeResource(
					getResources(),
					"res/graphics/tiles.png",
					options);*/
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		canvas.drawCircle(10.0f, 10.0f, 10.0f, piePaint);
	}
}
