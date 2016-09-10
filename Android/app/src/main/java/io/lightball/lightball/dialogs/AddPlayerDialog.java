package io.lightball.lightball.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.Inflater;

import io.lightball.lightball.PlayfieldActivity;
import io.lightball.lightball.R;
import io.lightball.lightball.ble.BleDevicesScanner;
import io.lightball.lightball.entities.Player;
import io.lightball.lightball.utils.BluetoothDeviceData;
import io.lightball.lightball.utils.ExpandableHeightExpandableListView;

/**
 * Created by Alexander Hoffmann on 09.09.16.
 */
public class AddPlayerDialog extends android.support.v4.app.DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(R.layout.dialog_add_player);
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

                        ((PlayfieldActivity) getActivity()).addPlayerToTeam(new Player("1",playerName,100),team);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }


}
