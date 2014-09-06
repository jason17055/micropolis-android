package micropolis.android;

import micropolisj.engine.*;
import static micropolisj.engine.TileConstants.CLEAR;

import java.util.HashSet;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.AttributeSet;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.*;
import android.widget.OverScroller;
import android.widget.Toast;

public class MicropolisView extends View
{
	Micropolis city;
	Rect scrollBounds = new Rect();
	Matrix renderMatrix = new Matrix();

	int windowWidth;
	int windowHeight;

	TileHelper tiles;
	int tileSize = 32;
	static final int MIN_TILE_SIZE = 8;
	static final int MAX_TILE_SIZE = 32;
	float originX = 0.0f;
	float originY = 0.0f;

	static final float SCALE_MOMENTUM_FACTOR = 0.9f;
	static final int BLINK_INTERVAL_MS = 500;
	static final int PREVIEW_BLINK_ON = 400;
	static final int PREVIEW_BLINK_OFF = 100;

	float scaleFocusX = 0.0f;
	float scaleFocusY = 0.0f;
	float scaleFactor = 1.0f;

	boolean allowTouchMotion = true;
	MicropolisTool currentTool = null;
	ToolStroke currentToolStroke = null;
	ToolPreview toolPreview = null;
	MapState currentOverlay = MapState.ALL;

	boolean blinkUnpoweredZones = true;
	HashSet<CityLocation> unpoweredZones = new HashSet<CityLocation>();
	boolean powerBlink;
	boolean powerBlinkScheduled;

	HashSet<CityLocation> previewZones = new HashSet<CityLocation>();
	boolean previewBlink;
	boolean previewBlinkScheduled;

	public MicropolisView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		tiles = new TileHelper(context, tileSize);
	}

	public Micropolis getCity()
	{
		return this.city;
	}

	MapListener myMapListener = new MapListener() {
		public void mapOverlayDataChanged(MapState overlayDataType) {}
		public void spriteMoved(Sprite sprite) {}
		public void tileChanged(int xpos, int ypos) {
			onTileChanged(xpos, ypos);
		}
		public void wholeMapChanged() {
			invalidate();
		}
		};

	public void setCity(Micropolis newCity)
	{
		assert newCity != null;

		if (this.city != null) {
			city.removeMapListener(myMapListener);
			this.city = null;
		}

		this.city = newCity;

		updateScrollBounds();

		originX = (scrollBounds.left + scrollBounds.right) / 2.0f;
		originY = (scrollBounds.top + scrollBounds.bottom) / 2.0f;

		city.addMapListener(myMapListener);
		invalidate();
	}

	void setOverlay(MapState mapState)
	{
		currentOverlay = mapState;
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		windowWidth = w;
		windowHeight = h;

		updateRenderMatrix();
	}

	public void setTileSize(int newSize)
	{
		double f = ((double)newSize) / ((double)tileSize);
		scaleFactor /= f;
		originX *= f;
		originY *= f;

		this.tileSize = newSize;
		tiles.changeTileSize(tileSize);
		updateScrollBounds();
		updateRenderMatrix();	
		invalidate();
	}

	private void updateRenderMatrix()
	{
		renderMatrix.reset();
		renderMatrix.preTranslate(windowWidth/2, windowHeight/2);
		if (scaleFactor != 1.0f) {
			renderMatrix.preScale(scaleFactor, scaleFactor, scaleFocusX, scaleFocusY);
		}
		renderMatrix.preTranslate(Math.round(-originX), Math.round(-originY));
	}

	private void updateScrollBounds()
	{
		scrollBounds.left = 0;
		scrollBounds.top = 0;
		scrollBounds.right = tileSize*city.getWidth();
		scrollBounds.bottom = tileSize*city.getHeight();
	}

	Rect getTileBounds(CityRect box)
	{
		float [] pts = {
			box.x * tileSize,
			box.y * tileSize,
			(box.x+box.width) * tileSize,
			(box.y+box.height) * tileSize
			};
		renderMatrix.mapPoints(pts);
		return new Rect(
			(int)Math.floor(pts[0]),
			(int)Math.floor(pts[1]),
			(int)Math.ceil(pts[2]),
			(int)Math.ceil(pts[3])
			);
	}

	Rect getTileBounds(int xpos, int ypos)
	{
		float [] pts = {
			xpos * tileSize,
			ypos * tileSize,
			(xpos+1) * tileSize,
			(ypos+1) * tileSize
			};
		renderMatrix.mapPoints(pts);
		return new Rect(
			(int)Math.floor(pts[0]),
			(int)Math.floor(pts[1]),
			(int)Math.ceil(pts[2]),
			(int)Math.ceil(pts[3])
			);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		Paint p = new Paint();

		canvas.save();
		canvas.concat(renderMatrix);

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
				int t = city.getTile(x, y);
				if (blinkUnpoweredZones &&
					TileConstants.isZoneCenter(t) &&
					!city.isTilePowered(x, y))
				{
					unpoweredZones.add(new CityLocation(x, y));
					if (powerBlink) {
						t = TileConstants.LIGHTNINGBOLT;
					}
				}

				if (toolPreview != null) {
					int c = toolPreview.getTile(x, y);
					if (c != CLEAR) {

						previewZones.add(new CityLocation(x, y));
						if (previewBlink) {
							t = c;
						}
					}
				}

				tiles.drawTo(canvas, t, x, y);
			}
		}

		switch (currentOverlay) {
		case POPDEN_OVERLAY:
			drawPopDensity(canvas);
			break;
		case GROWTHRATE_OVERLAY:
			drawRateOfGrowth(canvas);
			break;
		case POLLUTE_OVERLAY:
			drawPollutionMap(canvas);
			break;
		case CRIME_OVERLAY:
			drawCrimeMap(canvas);
			break;
		case FIRE_OVERLAY:
			drawFireRadius(canvas);
			break;
		case POLICE_OVERLAY:
			drawPoliceRadius(canvas);
			break;
		}

		canvas.restore();

		maybeStartBlinkTimer();
	}

	void drawPopDensity(Canvas canvas)
	{
		int [][] A = city.popDensity;

		for (int y = 0; y < A.length; y++) {
			for (int x = 0; x < A[y].length; x++) {
				maybeDrawRect(canvas, getOverlayColor(A[y][x]),
					new Rect(
						x * 2 * tileSize,
						y * 2 * tileSize,
						(x+1) * 2 * tileSize,
						(y+1) * 2 * tileSize
					));
			}
		}
	}

	void drawRateOfGrowth(Canvas canvas)
	{
		int [][] A = city.rateOGMem;

		for (int y = 0; y < A.length; y++) {
			for (int x = 0; x < A[y].length; x++) {
				maybeDrawRect(canvas, getOverlayColor_rog(A[y][x]),
					new Rect(
						x * 8 * tileSize,
						y * 8 * tileSize,
						(x+1) * 8 * tileSize,
						(y+1) * 8 * tileSize
					));
			}
		}
	}

	void drawPollutionMap(Canvas canvas)
	{
		int [][] A = city.pollutionMem;

		for (int y = 0; y < A.length; y++) {
			for (int x = 0; x < A[y].length; x++) {
				maybeDrawRect(canvas, getOverlayColor(A[y][x]),
					new Rect(
						x * 2 * tileSize,
						y * 2 * tileSize,
						(x+1) * 2 * tileSize,
						(y+1) * 2 * tileSize
					));
			}
		}
	}

	void drawCrimeMap(Canvas canvas)
	{
		int [][] A = city.crimeMem;

		for (int y = 0; y < A.length; y++) {
			for (int x = 0; x < A[y].length; x++) {
				maybeDrawRect(canvas, getOverlayColor(A[y][x]),
					new Rect(
						x * 2 * tileSize,
						y * 2 * tileSize,
						(x+1) * 2 * tileSize,
						(y+1) * 2 * tileSize
					));
			}
		}
	}

	void drawFireRadius(Canvas canvas)
	{
		int [][] A = city.fireRate;

		for (int y = 0; y < A.length; y++) {
			for (int x = 0; x < A[y].length; x++) {
				maybeDrawRect(canvas, getOverlayColor(A[y][x]),
					new Rect(
						x * 8 * tileSize,
						y * 8 * tileSize,
						(x+1) * 8 * tileSize,
						(y+1) * 8 * tileSize
					));
			}
		}
	}

	void drawPoliceRadius(Canvas canvas)
	{
		int [][] A = city.policeMapEffect;

		for (int y = 0; y < A.length; y++) {
			for (int x = 0; x < A[y].length; x++) {
				maybeDrawRect(canvas, getOverlayColor(A[y][x]),
					new Rect(
						x * 8 * tileSize,
						y * 8 * tileSize,
						(x+1) * 8 * tileSize,
						(y+1) * 8 * tileSize
					));
			}
		}
	}

	void maybeDrawRect(Canvas canvas, int argb, Rect rect)
	{
		Paint p = new Paint();
		p.setColor(argb);
		canvas.drawRect(rect, p);
	}

	static final int VAL_LOW = 0xbfbfbf;
	static final int VAL_MEDIUM = 0xffff00;
	static final int VAL_HIGH = 0xff7f00;
	static final int VAL_VERYHIGH = 0xff0000;

	static final int VAL_PLUS      = 0x007f00;
	static final int VAL_VERYPLUS  = 0x00e600;
	static final int VAL_MINUS     = 0xff7f00;
	static final int VAL_VERYMINUS = 0xffff00;

	int getOverlayColor(int x)
	{
		if (x < 50) {
			return 0;
		}
		else if (x < 100) {
			return 0xcc000000 | VAL_LOW;
		}
		else if (x < 150) {
			return 0xcc000000 | VAL_MEDIUM;
		}
		else if (x < 200) {
			return 0xcc000000 | VAL_HIGH;
		}
		else {
			return 0xcc000000 | VAL_VERYHIGH;
		}
	}

	int getOverlayColor_rog(int x)
	{
		if (x > 100)
			return 0xcc000000 | VAL_VERYPLUS;
		else if (x > 20)
			return 0xcc000000 | VAL_PLUS;
		else if (x < -100)
			return 0xcc000000 | VAL_VERYMINUS;
		else if (x < -20)
			return 0xcc000000 | VAL_MINUS;
		else
			return 0;
	}

	void setToolPreview(ToolPreview newPreview)
	{
		if (toolPreview != null) {
			CityRect b = toolPreview.getBounds();
			invalidate(getTileBounds(b));
		}

		toolPreview = newPreview;
		if (toolPreview != null) {
			CityRect b = toolPreview.getBounds();
			invalidate(getTileBounds(b));
		}
	}

	void maybeStartBlinkTimer()
	{
		if (!powerBlinkScheduled && !unpoweredZones.isEmpty()) {

			myHandler.postDelayed(new Runnable() {
				public void run() {
					doBlink();
				}}, BLINK_INTERVAL_MS);
			powerBlinkScheduled = true;
		}

		if (!previewBlinkScheduled && !previewZones.isEmpty()) {

			myHandler.postDelayed(new Runnable() {
				public void run() {
					doBlinkPreview();
				}}, previewBlink ? PREVIEW_BLINK_ON : PREVIEW_BLINK_OFF);
			previewBlinkScheduled = true;
		}
	}

	public void doBlink()
	{
		powerBlinkScheduled = false;
		powerBlink = !powerBlink;

		for (CityLocation loc : unpoweredZones) {
			
			Rect r = getTileBounds(loc.x, loc.y);
			invalidate(r);
		}
		unpoweredZones.clear();
	}

	public void doBlinkPreview()
	{
		previewBlinkScheduled = false;
		previewBlink = !previewBlink;

		for (CityLocation loc : previewZones) {
			
			Rect r = getTileBounds(loc.x, loc.y);
			invalidate(r);
		}
		previewZones.clear();
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
			CityLocation loc = getLocation(e1.getX(), e1.getY());
			if (inToolPreview(loc)) {
				processToolDrag(loc,
					getLocation(e2.getX(), e2.getY())
					);
				return true;
			}

			return onRealScroll(e1, e2, distanceX, distanceY);
		}

		boolean onRealScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			originX += distanceX / scaleFactor;
			originY += distanceY / scaleFactor;

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

			updateRenderMatrix();
			invalidate();

			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			startMomentum(velocityX / scaleFactor, velocityY / scaleFactor);
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent evt)
		{
			processTool(evt.getX(), evt.getY());
			return true;
		}

		// implements OnScaleGestureListener
		public boolean onScale(ScaleGestureDetector d)
		{
			scaleFocusX = d.getFocusX() - windowWidth/2;
			scaleFocusY = d.getFocusY() - windowHeight/2;
			scaleFactor *= d.getScaleFactor();
			scaleFactor = Math.min(Math.max(scaleFactor, 0.5f), 2.0f);
			updateRenderMatrix();
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
			checkZoomLevel();
		}
	}
	MyGestureListener mgl = new MyGestureListener();
	GestureDetector gestDetector = new GestureDetector(getContext(), mgl);
	ScaleGestureDetector scaleDetector = new ScaleGestureDetector(getContext(), mgl);

	@Override
	public boolean onTouchEvent(MotionEvent evt)
	{
		if (allowTouchMotion) {
			boolean x1 = gestDetector.onTouchEvent(evt);
			boolean x2 = scaleDetector.onTouchEvent(evt);
			return x1 || x2;
		}
		else {
			return false;
		}
	}

	MyMomentumStep activeMotion = null;
	Handler myHandler = new Handler();

	interface MyMomentumStep extends Runnable
	{
		void cancelMomentum();
	}

	class MyScrollStep implements MyMomentumStep
	{
		OverScroller s = new OverScroller(getContext());

		MyScrollStep(float velX, float velY)
		{
			s.fling((int)originX, (int)originY, (int)-velX, (int)-velY,
				scrollBounds.left, scrollBounds.right,
				scrollBounds.top, scrollBounds.bottom,
				tileSize*4, tileSize*4);
		}

		// implements MyMomentumStep
		public void cancelMomentum()
		{
			// do nothing
		}

		// implements Runnable
		public void run()
		{
			if (activeMotion == this) {

				boolean activ = s.computeScrollOffset();

				originX = s.getCurrX();
				originY = s.getCurrY();
				updateRenderMatrix();
				invalidate();

				if (!activ) {
					activeMotion = null;
					completeScrollMomentum();
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

	class MyZoomStep implements MyMomentumStep
	{
		float targetFactor;
		MyZoomStep(float targetFactor)
		{
			this.targetFactor = targetFactor;
		}

		// implements MyMomentumStep
		public void cancelMomentum()
		{
			scaleFactor = targetFactor;
			updateRenderMatrix();
			invalidate();

			activeMotion = null;
			completeZoomMomentum(targetFactor);
		}

		// implements Runnable
		public void run()
		{
			if (activeMotion == this) {

				if (targetFactor < scaleFactor) {

					scaleFactor = Math.max(targetFactor, scaleFactor * SCALE_MOMENTUM_FACTOR);
				}
				else if (targetFactor > scaleFactor) {

					scaleFactor = Math.min(targetFactor, scaleFactor / SCALE_MOMENTUM_FACTOR);
				}
				updateRenderMatrix();
				invalidate();

				if (targetFactor == scaleFactor) {
					activeMotion = null;
					completeZoomMomentum(targetFactor);
				}
				else {
					myHandler.postDelayed(this, 100);
				}
			}
		}
	}

	private void checkZoomLevel()
	{
		if (scaleFactor > 1.414214) {
			startZoomMomentum(2.0f);
		}
		else if (scaleFactor < 0.707107) {
			startZoomMomentum(0.5f);
		}
		else {
			startZoomMomentum(1.0f);
		}
	}

	private void startZoomMomentum(float targetFactor)
	{
		if (scaleFactor != targetFactor) {
			this.activeMotion = new MyZoomStep(targetFactor);
			myHandler.postDelayed(activeMotion, 0);
		}
		else {
			completeZoomMomentum(targetFactor);
		}
	}

	private void completeZoomMomentum(float targetFactor)
	{
		if (tileSize > MIN_TILE_SIZE && targetFactor == 0.5) {
			setTileSize(tileSize/2);
		}
		else if (tileSize < MAX_TILE_SIZE && targetFactor == 2.0) {
			setTileSize(tileSize*2);
		}
	}

	private void completeScrollMomentum()
	{
		checkZoomLevel();
	}

	private void startMomentum(float velX, float velY)
	{
		this.activeMotion = new MyScrollStep(velX, velY);
		myHandler.postDelayed(activeMotion, 100);
	}

	private void stopMomentum()
	{
		if (this.activeMotion != null) {
			activeMotion.cancelMomentum();
			this.activeMotion = null;
		}
	}

	void inspectLocation(CityLocation loc)
	{
		if (inspectHelper != null) {
			inspectHelper.inspectLocation(loc);
		}
	}

	boolean inToolPreview(CityLocation loc)
	{
		if (toolPreview != null) {
			int c = toolPreview.getTile(loc.x, loc.y);
			return c != CLEAR;
		}
		return false;
	}

	private void processToolDrag(CityLocation origLoc, CityLocation newLoc)
	{
		currentToolStroke.dragTo(newLoc.x, newLoc.y);
		setToolPreview(currentToolStroke.getPreview());
	}

	private void processTool(float x, float y)
	{
		try {

		CityLocation loc = getLocation(x, y);
		if (currentToolStroke != null) {

			if (inToolPreview(loc)) {
				// ignore taps inside preview area
				return;
			}

			// to allow a new stroke to start...
			completeToolStroke();
		}

		if (currentTool != null) {
			currentToolStroke = currentTool.beginStroke(city, loc.x, loc.y);
			setToolPreview(
				currentToolStroke.getPreview()
				);
		}
		else {
			inspectLocation(loc);
		}

		}
		catch (Throwable e)
		{
			AlertDialog alert = new AlertDialog.Builder(getContext()).create();
			alert.setTitle("Error");
			alert.setMessage(e.toString() + e.getStackTrace()[0].toString());
			alert.show();
		}
	}

	private CityLocation getLocation(float x, float y)
	{
		Matrix aMatrix = new Matrix();
		if (!renderMatrix.invert(aMatrix)) {
			return new CityLocation(0,0);
		}

		float [] pts = new float[] { x, y };
		aMatrix.mapPoints(pts);

		return new CityLocation(
			(int)pts[0] / tileSize,
			(int)pts[1] / tileSize
			);
	}

	void setTool(MicropolisTool tool)
	{
		if (this.currentTool == tool) {
			return;
		}

		abortToolStroke();
		this.currentTool = tool;
	}

	void abortToolStroke()
	{
		this.currentToolStroke = null;
		setToolPreview(null);
	}

	void completeToolStroke()
	{
		if (currentToolStroke != null) {
			currentToolStroke.apply();
			currentToolStroke = null;
			setToolPreview(null);
		}
	}

	private void onTileChanged(int xpos, int ypos)
	{
		Rect r = getTileBounds(xpos, ypos);
		invalidate(r);
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{
		Bundle b = new Bundle();
		b.putParcelable("superState", super.onSaveInstanceState());
		b.putFloat("scaleFactor", scaleFactor);
		b.putFloat("originX", originX);
		b.putFloat("originY", originY);
		return b;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		if (state instanceof Bundle) {
			Bundle b = (Bundle) state;
			super.onRestoreInstanceState(b.getParcelable("superState"));
			scaleFactor = b.getFloat("scaleFactor");
			originX = b.getFloat("originX");
			originY = b.getFloat("originY");

			if (scaleFactor < 0.5f) {
				scaleFactor = 0.5f;
			}
			updateRenderMatrix();
		}
		else {
			super.onRestoreInstanceState(state);
		}
	}

	public interface InspectHelper
	{
		void inspectLocation(CityLocation loc);
	}
	InspectHelper inspectHelper;

	void setInspectHelper(InspectHelper helper)
	{
		this.inspectHelper = helper;
	}
}
