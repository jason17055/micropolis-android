package micropolis.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import micropolisj.engine.*;

public class MainActivity extends Activity
	implements PickBuildingDialogFragment.Listener,
		Micropolis.Listener
{
	Micropolis city;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.city = new Micropolis();
		new MapGenerator(city).generateNewCity();
		city.setFunds(20000);

		city.addListener(this);
		getMicropolisView().setCity(city);
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

	class MyAdvancer implements Runnable
	{
		void sched()
		{
			myHandler.postDelayed(this, 250);
		}

		public void run()
		{
			if (this != advanceSim) { return; }
			getCity().animate();
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
		findViewById(R.id.road_tool_btn).setVisibility(v);
		findViewById(R.id.rail_tool_btn).setVisibility(v);
		findViewById(R.id.wire_tool_btn).setVisibility(v);
		findViewById(R.id.res_tool_btn).setVisibility(v);
		findViewById(R.id.com_tool_btn).setVisibility(v);
		findViewById(R.id.ind_tool_btn).setVisibility(v);
		findViewById(R.id.coal_tool_btn).setVisibility(v);
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

	// implements Micropolis.Listener
	public void cityMessage(MicropolisMessage message, CityLocation loc, boolean isPic)
	{
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
			fundsInd.setText("Funds: "+city.budget.totalFunds);
		}
	}
}
