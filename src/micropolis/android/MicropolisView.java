package micropolis.android;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.AttributeSet;
import android.view.View;

public class MicropolisView extends View
{
	Paint piePaint;
	Paint bluePaint;
	Bitmap tilesBitmap;
	
	public MicropolisView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
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
		canvas.drawBitmap(tilesBitmap,
			0.0f, 0.0f,
			p);

		canvas.drawCircle(10.0f, 10.0f, 10.0f, piePaint);
		canvas.drawCircle(10.0f, 100.0f, 8.0f, piePaint);

		canvas.drawText("Size of bitmap: "
			+ tilesBitmap.getWidth() + " x " + tilesBitmap.getHeight(),
			20.0f, 116.0f, bluePaint);
	}
}
