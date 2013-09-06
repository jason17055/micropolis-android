package micropolis.android;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import micropolisj.engine.*;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
		case R.id.action_build_roads:
			doRoadTool();
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

	void doRoadTool()
	{
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
		setToolsVisibility(View.GONE);
		findViewById(R.id.ok_btn).setVisibility(View.VISIBLE);
		setTool(MicropolisTool.POWERPLANT);
	}

	public void onOkClicked(View view)
	{
		findViewById(R.id.ok_btn).setVisibility(View.GONE);
		setToolsVisibility(View.VISIBLE);
		setTool(null);
	}
}
