package micropolis.android;

import micropolisj.engine.MicropolisTool;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import static android.widget.AdapterView.OnItemClickListener;

public class PickBuildingDialogFragment extends DialogFragment
{
	public interface Listener
	{
		void onPickBuilding(MicropolisTool buildingTool);
	}
	Listener myListener;

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		if (activity instanceof Listener) {
			myListener = (Listener) activity;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.pick_building_dlg, null);
		builder.setView(content);

		setupContent(content);

		builder.setTitle(R.string.pickbuilding_caption);
		builder.setNegativeButton(
			R.string.cancel,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int id) {
				}
			});

		return builder.create();
	}

	private void setupContent(final View contentView)
	{
		GridView gridview = (GridView) contentView.findViewById(R.id.pick_building_gridview);
		gridview.setAdapter(new MyAdapter(contentView.getContext()));
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				PickBuildingDialogFragment.this.onItemClick(
					position,
					parent.getItemAtPosition(position)
					);
			}});
	}

	void onItemClick(int position, Object item)
	{
		if (myListener != null) {
			MicropolisTool tool = (MicropolisTool) item;
			myListener.onPickBuilding(tool);
		}
		dismiss();
	}

	class MyAdapter extends BaseAdapter
	{
		Context context;

		MyAdapter(Context c)
		{
			context = c;
		}

		@Override
		public int getCount()
		{
			return building_icons.length;
		}

		@Override
		public Object getItem(int position)
		{
			return building_tools[position];
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ImageView imageView;
			if (convertView == null) {
				// not recycled, so initialize some attributes
				imageView = new ImageView(context);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
			}
			else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageResource(building_icons[position]);
			return imageView;
		}

	}

	static int[] building_icons = {
		R.drawable.iccoal,
		R.drawable.icnuc,
		R.drawable.icpol,
		R.drawable.icfire,
		R.drawable.icstad,
		R.drawable.icseap,
		R.drawable.icairp,
		R.drawable.icpark
		};
	static MicropolisTool[] building_tools = {
		MicropolisTool.POWERPLANT,
		MicropolisTool.NUCLEAR,
		MicropolisTool.POLICE,
		MicropolisTool.FIRE,
		MicropolisTool.STADIUM,
		MicropolisTool.SEAPORT,
		MicropolisTool.AIRPORT,
		MicropolisTool.PARK
		};
}
