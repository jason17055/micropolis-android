package micropolis.android;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class MicropolisView extends View
{
	Paint piePaint;
	
	public MicropolisView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		piePaint = new Paint();
		piePaint.setColor(0xffff0000);
		piePaint.setStyle(Paint.Style.FILL);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		canvas.drawCircle(10.0f, 10.0f, 10.0f, piePaint);
	}
}
