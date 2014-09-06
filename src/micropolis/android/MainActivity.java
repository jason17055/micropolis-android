package micropolis.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import micropolisj.engine.*;

public class MainActivity extends Activity
	implements PickBuildingDialogFragment.Listener,
		Micropolis.Listener,
		MicropolisView.InspectHelper
{
	Micropolis city;

	static final String ST_MICROPOLIS = "micropolis.city";
	/** Used in Intent. */
	static final String EXTRA_CITY_NAME = "micropolis.cityName";
	static final String EXTRA_SIM_DATA = "micropolis.cityData";

	private boolean restoreCity(Bundle st)
	{
		if (st == null) return false;

		byte [] bytes = st.getByteArray(ST_MICROPOLIS);
		if (bytes == null) return false;

		try {
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			this.city = new Micropolis();
			city.load(in);
			return true;
		}
		catch (IOException e)
		{
			// not really expected, but we must define
			// behavior for this case
			return false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (!restoreCity(savedInstanceState)) {

			loadCityFromIntent();
		}

		city.addListener(this);
		getMicropolisView().setCity(city);
		getDemandIndicator().setCity(city);
		updateDateLabel();
		fundsChanged();

		getMicropolisView().setInspectHelper(this);
	}

	private void loadCityFromIntent()
	{
		this.city = new Micropolis();

		Intent intent = getIntent();
		byte [] cityData = intent.getByteArrayExtra(EXTRA_SIM_DATA);
		if (cityData != null) {

			ByteArrayInputStream bytes = new ByteArrayInputStream(cityData);
			try {
				city.load(bytes);
				return;
			}
			catch (IOException e) {
				// should not happen
				throw new RuntimeException(e);
			}
		}

		String cityName = intent.getStringExtra(EXTRA_CITY_NAME);
		if (cityName != null) {

			File f = new File(getFilesDir(), cityName);
			try {
				city.load(f);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}

		}
	}

	@Override
	public void onRestoreInstanceState(Bundle b)
	{
		super.onRestoreInstanceState(b);

		String tmp = b.getString("currentTool");
		if (tmp != null) {
			setToolsVisibility(View.GONE);
			setOkCancelVisibility(View.VISIBLE);
			setTool(
				MicropolisTool.valueOf(tmp)
				);
		}
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.action_run_sim:
			doRunSim();
			return true;
		case R.id.action_pause_sim:
			doPauseSim();
			return true;
		case R.id.action_budget:
			doBudget();
			return true;
		case R.id.menu_no_overlay:
			doMapOverlay(MapState.ALL);
			return true;
		case R.id.menu_popden_overlay:
			doMapOverlay(MapState.POPDEN_OVERLAY);
			return true;
		case R.id.menu_growthrate_overlay:
			doMapOverlay(MapState.GROWTHRATE_OVERLAY);
			return true;
		case R.id.menu_landvalue_overlay:
			doMapOverlay(MapState.LANDVALUE_OVERLAY);
			return true;
		case R.id.menu_crime_overlay:
			doMapOverlay(MapState.CRIME_OVERLAY);
			return true;
		case R.id.menu_pollute_overlay:
			doMapOverlay(MapState.POLLUTE_OVERLAY);
			return true;
		case R.id.menu_traffic_overlay:
			doMapOverlay(MapState.TRAFFIC_OVERLAY);
			return true;
		case R.id.menu_power_overlay:
			doMapOverlay(MapState.POWER_OVERLAY);
			return true;
		case R.id.menu_fire_overlay:
			doMapOverlay(MapState.FIRE_OVERLAY);
			return true;
		case R.id.menu_police_overlay:
			doMapOverlay(MapState.POLICE_OVERLAY);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void setTool(MicropolisTool newTool)
	{
		MicropolisView view = (MicropolisView) findViewById(R.id.main_view);
		view.setTool(newTool);
	}

	Micropolis getCity()
	{
		return city;
	}

	MicropolisView getMicropolisView()
	{
		MicropolisView view = (MicropolisView) findViewById(R.id.main_view);
		return view;
	}

	DemandView getDemandIndicator()
	{
		return (DemandView)
			findViewById(R.id.demand_ind);
	}

	class MyAdvancer implements Runnable
	{
		void sched()
		{
			myHandler.postDelayed(this, 200);
		}

		public void run()
		{
			if (this != advanceSim) { return; }

			try {
				getCity().animate();
				updateDateLabel();
				sched();
			}
			catch (Exception e) {

				// An error was thrown by the simulator.
				// This should not happen, but it is a real pain to
				// debug without at least seeing the error message.
				// So, here we display a crude message box with the
				// error.

				AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
				b.setTitle("Error");
				b.setMessage(e.toString());
				b.show();
			}

		}
	}

	Handler myHandler = new Handler();
	MyAdvancer advanceSim = null;
	boolean simEnabled = false;

	void doPauseSim()
	{
		simEnabled = false;
		advanceSim = null;
	}

	void doRunSim()
	{
		simEnabled = true;
		advanceSim = new MyAdvancer();
		advanceSim.run();
	}

	void doBudget()
	{
		DialogFragment dlg = new BudgetFragment(city);
		dlg.show(getFragmentManager(), "BudgetFragment");
	}

	void doMapOverlay(MapState overlayState)
	{
		getMicropolisView().setOverlay(overlayState);
	}

	private void setOkCancelVisibility(int v)
	{
		findViewById(R.id.ok_btn).setVisibility(v);
		findViewById(R.id.cancel_btn).setVisibility(v);
	}

	private void setToolsVisibility(int v)
	{
		findViewById(R.id.bulldozer_tool_btn).setVisibility(v);
		findViewById(R.id.transport_tool_btn).setVisibility(v);
		findViewById(R.id.zones_tool_btn).setVisibility(v);
		findViewById(R.id.buildings_tool_btn).setVisibility(v);

		findViewById(R.id.transport_tool_btn).getBackground().clearColorFilter();
		findViewById(R.id.zones_tool_btn).getBackground().clearColorFilter();

		findViewById(R.id.transport_tools_submenu).setVisibility(View.GONE);
		findViewById(R.id.zone_tools_submenu).setVisibility(View.GONE);
	}

	public void onBulldozerToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		setOkCancelVisibility(View.VISIBLE);
		setTool(MicropolisTool.BULLDOZER);
	}

	public void onRoadToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		setOkCancelVisibility(View.VISIBLE);
		setTool(MicropolisTool.ROADS);
	}

	public void onRailToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		setOkCancelVisibility(View.VISIBLE);
		setTool(MicropolisTool.RAIL);
	}

	public void onWireToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		setOkCancelVisibility(View.VISIBLE);
		setTool(MicropolisTool.WIRE);
	}

	public void onTransportToolClicked(View view)
	{
		setToolsVisibility(View.VISIBLE);
		view.getBackground().setColorFilter(0xffffffff, PorterDuff.Mode.ADD);
		findViewById(R.id.transport_tools_submenu).setVisibility(View.VISIBLE);
	}

	public void onZonesToolClicked(View view)
	{
		setToolsVisibility(View.VISIBLE);
		view.getBackground().setColorFilter(0xffffffff, PorterDuff.Mode.ADD);
		findViewById(R.id.zone_tools_submenu).setVisibility(View.VISIBLE);
	}

	public void onResZoneToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		setOkCancelVisibility(View.VISIBLE);
		setTool(MicropolisTool.RESIDENTIAL);
	}

	public void onComZoneToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		setOkCancelVisibility(View.VISIBLE);
		setTool(MicropolisTool.COMMERCIAL);
	}

	public void onIndZoneToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		setOkCancelVisibility(View.VISIBLE);
		setTool(MicropolisTool.INDUSTRIAL);
	}

	public void onCoalToolClicked(View view)
	{
		DialogFragment dlg = new PickBuildingDialogFragment();
		dlg.show(getFragmentManager(), "PickBuildingDialogFragment");
	}

	// implements PickBuildingDialogFragment.Listener
	public void onPickBuilding(MicropolisTool buildingTool)
	{
		setToolsVisibility(View.GONE);
		setOkCancelVisibility(View.VISIBLE);
		setTool(buildingTool);
	}

	public void onOkClicked(View view)
	{
		setOkCancelVisibility(View.GONE);
		setToolsVisibility(View.VISIBLE);

		getMicropolisView().completeToolStroke();
		setTool(null);
	}

	public void onCancelClicked(View view)
	{
		setOkCancelVisibility(View.GONE);
		setToolsVisibility(View.VISIBLE);

		getMicropolisView().abortToolStroke();
		setTool(null);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (simEnabled && advanceSim != null) {
			advanceSim = null;
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if (simEnabled && advanceSim == null) {
			advanceSim = new MyAdvancer();
			advanceSim.sched();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle st)
	{
		super.onSaveInstanceState(st);

		try {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		city.save(bytes);

		st.putByteArray(ST_MICROPOLIS, bytes.toByteArray());
		}
		catch (IOException e) {
			// not expected
			throw new Error("unexpected: "+e, e);
		}

		MicropolisTool currentTool = getMicropolisView().currentTool;
		if (currentTool != null) {
			st.putString("currentTool", currentTool.name());
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		try
		{
		File saveFile = new File(getFilesDir(), "current.cty");
		city.save(saveFile);
		}
		catch (IOException e)
		{
			// unexpected for internal storage
			throw new RuntimeException(e);
		}
	}

	static ResourceBundle cityMessageStrings = ResourceBundle.getBundle("micropolisj.CityMessages");

	// implements Micropolis.Listener
	public void cityMessage(MicropolisMessage message, CityLocation loc)
	{
		String s = cityMessageStrings.getString(message.name());
		Toast.makeText(this,
			s,
			Toast.LENGTH_SHORT
			).show();
	}

	// implements Micropolis.Listener
	public void citySound(Sound sound, CityLocation loc)
	{
	}

	// implements Micropolis.Listener
	public void censusChanged() { }

	// implements Micropolis.Listener
	public void demandChanged() { }

	// implements Micropolis.Listener
	public void evaluationChanged() {}

	// implements Micropolis.Listener
	public void optionsChanged() {}

	// implements Micropolis.Listener
	public void fundsChanged()
	{
		TextView fundsInd = (TextView) findViewById(R.id.funds_ind);
		if (fundsInd != null) {
			NumberFormat nf = NumberFormat.getInstance();
			fundsInd.setText(nf.format(city.budget.totalFunds));
		}
	}

	static String formatGameDate(int cityTime)
	{
		Calendar c = Calendar.getInstance();
		c.set(1900 + cityTime/48, //year
			(cityTime%48)/4, //month
			(cityTime%4)*7+1 //day of month
			);
		return MessageFormat.format(
			"{0,date,MMM yyyy}",
			c.getTime()
			);
	}

	private void updateDateLabel()
	{
		TextView dateInd = (TextView) findViewById(R.id.date_ind);
		if (dateInd != null) {
			String s = formatGameDate(city.cityTime);
			dateInd.setText(s);
		}

		TextView popLbl = (TextView) findViewById(R.id.pop_ind);
		if (popLbl != null) {
			NumberFormat nf = NumberFormat.getInstance();
			String t = nf.format(city.getCityPopulation());
			popLbl.setText(t);
		}
	}

	//implements MicropolisView.InspectHelper
	public void inspectLocation(CityLocation loc)
	{
		assert loc != null;

		TileSpec tspec = Tiles.get(
			city.getTile(loc.x, loc.y)
			);
		if (tspec != null && tspec.owner != null) {
			CityLocation pLoc = new CityLocation(loc.x-tspec.ownerOffsetX, loc.y-tspec.ownerOffsetY);
			TileSpec baseSpec = Tiles.get(
				city.getTile(pLoc.x, pLoc.y)
				);

			if (baseSpec == tspec.owner) {
				loc = pLoc;
				tspec = baseSpec;
			}
		}

		ZoneStatus z = city.queryZoneStatus(loc.x, loc.y);

		DialogFragment dlg = new InspectLocationFragment(city, loc, z);
		dlg.show(getFragmentManager(), "InspectLocationFragment");
	}
}
