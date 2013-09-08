package micropolis.android;

import micropolisj.engine.*;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;

public class DemandView extends View
	implements Micropolis.Listener
{
	Micropolis city;
	Bitmap backgroundImage;

	public DemandView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		loadBackgroundImage();
	}

	private void loadBackgroundImage()
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		backgroundImage = BitmapFactory.decodeResource(
			getResources(), R.drawable.demandg, options
			);
	}

	public void setCity(Micropolis newCity)
	{
		if (this.city != null) {
			this.city.removeListener(this);
		}

		this.city = newCity;

		if (this.city != null) {
			this.city.addListener(this);
		}
		invalidate();
	}

	static final int UPPER_EDGE = 19;
	static final int LOWER_EDGE = 28;
	static final int MAX_LENGTH = 16;
	static final int RES_LEFT = 8;
	static final int RES_RIGHT = 8+6;
	static final int COM_LEFT = 17;
	static final int COM_RIGHT = 17+6;
	static final int IND_LEFT = 26;
	static final int IND_RIGHT = 26+6;
	static final Paint resPaint = new Paint();
	static final Paint comPaint = new Paint();
	static final Paint indPaint = new Paint();
	static final Paint outlinePaint = new Paint();
	static {
		resPaint.setColor(Color.GREEN);
		comPaint.setColor(Color.BLUE);
		indPaint.setColor(Color.YELLOW);
		outlinePaint.setColor(Color.BLACK);
		outlinePaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		canvas.drawBitmap(
			backgroundImage,
			0, 0,
			null);

		if (this.city == null)
			return;

		int resValve = city.getResValve();
		int ry0 = resValve <= 0 ? LOWER_EDGE : UPPER_EDGE;
		int ry1 = ry0 - resValve / 100;

		if (ry1 - ry0 > MAX_LENGTH) { ry1 = ry0 + MAX_LENGTH; }
		if (ry1 - ry0 < -MAX_LENGTH) { ry1 = ry0 - MAX_LENGTH; }

		int comValve = city.getComValve();
		int cy0 = comValve <= 0 ? LOWER_EDGE : UPPER_EDGE;
		int cy1 = cy0 - comValve / 100;

		int indValve = city.getIndValve();
		int iy0 = indValve <= 0 ? LOWER_EDGE : UPPER_EDGE;
		int iy1 = iy0 - indValve / 100;

		if (ry0 != ry1) {
			Rect resRect = new Rect(
					RES_LEFT, Math.min(ry0, ry1),
					RES_RIGHT, Math.max(ry0, ry1));
			canvas.drawRect(resRect, resPaint);
			canvas.drawRect(resRect, outlinePaint);
		}

		if (cy0 != cy1) {
			Rect comRect = new Rect(
					COM_LEFT, Math.min(cy0, cy1),
					COM_RIGHT, Math.max(cy0, cy1)
				);
			canvas.drawRect(comRect, comPaint);
			canvas.drawRect(comRect, outlinePaint);
		}

		if (iy0 != iy1) {
			Rect indRect = new Rect(
					IND_LEFT, Math.min(iy0, iy1),
					IND_RIGHT, Math.max(iy0, iy1)
				);
			canvas.drawRect(indRect, indPaint);
			canvas.drawRect(indRect, outlinePaint);
		}
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec)
	{
		setMeasuredDimension(
			backgroundImage.getWidth(),
			backgroundImage.getHeight()
			);
	}

	// implements Micropolis.Listener
	public void demandChanged()
	{
		invalidate();
	}

	// implements Micropolis.Listener
	public void cityMessage(MicropolisMessage m, CityLocation p, boolean x) { }
	public void citySound(Sound sound, CityLocation p) { }
	public void censusChanged() { }
	public void evaluationChanged() { }
	public void fundsChanged() { }
	public void optionsChanged() { }
}
