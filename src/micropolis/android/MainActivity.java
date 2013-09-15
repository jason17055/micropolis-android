package micropolis.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
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
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import micropolisj.engine.*;

public class MainActivity extends Activity
	implements PickBuildingDialogFragment.Listener,
		Micropolis.Listener
{
	Micropolis city;

	static final String ST_MICROPOLIS = "micropolis.city";

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
			this.city = new Micropolis();
			new MapGenerator(city).generateNewCity();
			city.setFunds(20000);
		}

		city.addListener(this);
		getMicropolisView().setCity(city);
		getDemandIndicator().setCity(city);
	}

	@Override
	public void onRestoreInstanceState(Bundle b)
	{
		super.onRestoreInstanceState(b);

		String tmp = b.getString("currentTool");
		if (tmp != null) {
			setToolsVisibility(View.GONE);
			findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
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
			getCity().animate();
			updateDateLabel();
			sched();
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
		findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
		setTool(MicropolisTool.BULLDOZER);
	}

	public void onRoadToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
		setTool(MicropolisTool.ROADS);
	}

	public void onRailToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
		setTool(MicropolisTool.RAIL);
	}

	public void onWireToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
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
		findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
		setTool(MicropolisTool.RESIDENTIAL);
	}

	public void onComZoneToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
		setTool(MicropolisTool.COMMERCIAL);
	}

	public void onIndZoneToolClicked(View view)
	{
		setToolsVisibility(View.GONE);
		findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
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
		findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
		setTool(buildingTool);
	}

	public void onOkClicked(View view)
	{
		findViewById(R.id.ok_btn).setVisibility(View.GONE);
		setToolsVisibility(View.VISIBLE);
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

	static ResourceBundle cityMessageStrings = ResourceBundle.getBundle("micropolisj.CityMessages");

	// implements Micropolis.Listener
	public void cityMessage(MicropolisMessage message, CityLocation loc, boolean isPic)
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
}
