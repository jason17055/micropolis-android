package micropolis.android;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;

public class PickBuildingDialogFragment extends DialogFragment
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.dialog_fire_missiles);
		builder.setNegativeButton(
			R.string.cancel,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int id) {
				}
			});

		return builder.create();
	}
}
