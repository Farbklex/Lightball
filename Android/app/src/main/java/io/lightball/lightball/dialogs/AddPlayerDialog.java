package io.lightball.lightball.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.RadioButton;

import io.lightball.lightball.SetupActivity;
import io.lightball.lightball.R;
import io.lightball.lightball.entities.Player;

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

                        ((SetupActivity) getActivity()).addPlayerToTeam(new Player("1", null, playerName,100),team);
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
