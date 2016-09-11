package io.lightball.lightball.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import io.lightball.lightball.SetupActivity;
import io.lightball.lightball.R;
import io.lightball.lightball.entities.Player;

/**
 * Created by Alexander Hoffmann on 09.09.16.
 */
public class AddPlayerDialog extends android.support.v4.app.DialogFragment {

    private ArrayAdapter<String> spinnerAdapter;

    public interface AddPlayerDialogListener {
        public ArrayAdapter<String> getDialogAdapter();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_player, null);

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner_bt_gear);
        spinner.setAdapter(spinnerAdapter);

        builder.setView(view);
        builder.setTitle("Add player")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String playerName = ((EditText) ((Dialog) dialog).findViewById(R.id.edit_player_name)).getText().toString();
                        int team;
                        // Is the button now checked?
                        if(((RadioButton) ((Dialog) dialog).findViewById(R.id.radio_team1)).isChecked()){
                            team = 1;
                        }else{
                            team = 2;
                        }

                        Spinner spinner = (Spinner) ((Dialog) dialog).findViewById(R.id.spinner_bt_gear);

                        String deviceName = (String) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());
                        String deviceAddress = ((SetupActivity) getActivity()).getDeviceAddressByName(deviceName);

                        if(playerName.equals("kk")){
                            ((SetupActivity) getActivity()).addPlayerToTeam(new Player("345", null, playerName,100),team);
                        }else {
                            ((SetupActivity) getActivity()).addPlayerToTeam(new Player(deviceAddress, null, playerName,100),team);
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        Dialog dialog = builder.create();

        return dialog;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        AddPlayerDialogListener listener = (AddPlayerDialogListener) activity;
        spinnerAdapter = listener.getDialogAdapter();
    }

}
