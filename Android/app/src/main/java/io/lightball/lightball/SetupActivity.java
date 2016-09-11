package io.lightball.lightball;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import io.lightball.lightball.dialogs.AddPlayerDialog;
import io.lightball.lightball.entities.Player;
import io.lightball.lightball.utils.BluetoothDeviceData;
import io.lightball.lightball.utils.BluetoothManager;

public class SetupActivity extends AppCompatActivity implements TeamFragment.OnListFragmentInteractionListener, AddPlayerDialog.AddPlayerDialogListener, BluetoothManager.OnScanCompleteListener {

    String deviceNames[] = {"Scanning..."};
    HashMap<String, String> devices = new HashMap<>();
    ArrayAdapter<String> deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BluetoothManager btMgr = BluetoothManager.getInstance();
        btMgr.init(getApplicationContext());
        btMgr.scan(this);

        deviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceAdapter.addAll(deviceNames);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPlayerDialog dialog = new AddPlayerDialog();
                dialog.show(getSupportFragmentManager(),"ADD_PLAYER");

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the actsion bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playfield, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_debug) {
            Intent intent = new Intent(this, DebugActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(Player item) {

    }

    public void addPlayerToTeam(Player player, int team){
        TeamFragment teamFragment = null;
        if(team == 1){
            teamFragment = ((TeamFragment) getSupportFragmentManager().findFragmentById(R.id.playersTeam1));
        }else if(team == 2){
            teamFragment = ((TeamFragment) getSupportFragmentManager().findFragmentById(R.id.playersTeam2));
        }
        if(teamFragment != null){
            teamFragment.addPlayerToTeam(player, team);
            Toast.makeText(SetupActivity.this, "Player " + player.name + " added to team " + team, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void startGame(View v){
        Intent intent = new Intent(this, GameInProgressActivity.class);
        startActivity(intent);
    }

    @Override
    public ArrayAdapter<String> getDialogAdapter() {
        return deviceAdapter;
    }

    @Override
    public void onScanComplete(ArrayList<BluetoothDeviceData> res) {
        ArrayList<String> deviceNames = new ArrayList<>();
        for (BluetoothDeviceData data : res) {
            if (data.advertisedName != null
                    && data.advertisedName.contains("Lightball")) {
                devices.put(data.advertisedName, data.device.getAddress());
                deviceNames.add(data.advertisedName);
            }
        }

        devices.put("Lightball Gear Dummy", "0");
        deviceNames.add("Lightball Gear Dummy");

        deviceAdapter.clear();
        deviceAdapter.addAll(deviceNames);
    }

    public String getDeviceAddressByName(String name) {
        return devices.get(name);
    }
}
