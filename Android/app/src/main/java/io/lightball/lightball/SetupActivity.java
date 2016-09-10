package io.lightball.lightball;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import io.lightball.lightball.dialogs.AddPlayerDialog;
import io.lightball.lightball.entities.Player;

public class SetupActivity extends AppCompatActivity implements TeamFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
}
