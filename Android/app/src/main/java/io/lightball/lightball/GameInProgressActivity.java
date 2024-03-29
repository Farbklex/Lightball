package io.lightball.lightball;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import io.lightball.lightball.ble.BleManager;
import io.lightball.lightball.entities.Player;
import io.lightball.lightball.interfaces.GameStateInterface;
import io.lightball.lightball.utils.BluetoothManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameInProgressActivity extends AppCompatActivity
        implements GameStateInterface, BleManager.BleManagerListener {

    ArrayList<View> mTeam1Views = new ArrayList<>();
    ArrayList<View> mTeam2Views = new ArrayList<>();
    ArrayList<View> mPlayers = new ArrayList<>();

    // Bluetooth stuff
    public static final String TAG = "GameInProgressActivity";
    public static final String UUID_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_DFU = "00001530-1212-EFDE-1523-785FEABCD123";
    public static final int kTxMaxCharacters = 20;

    protected BluetoothGattService mUartService;
    protected BleManager mBleManager;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_in_progress);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.resumeButton).setOnTouchListener(mDelayHideTouchListener);
        fillTeamViews();
        //Setup team 1
        ArrayList<Player> team1 = GameStateManager.getInstance().getTeam1();
        ArrayList<Player> team2 = GameStateManager.getInstance().getTeam2();

        //Setup the player views
        if (team1 != null) {
            int i = 0;
            for(Player player : team1){
                initPlayerView(player,mTeam1Views.get(i));
                i++;
            }
        }
        //Also for team 2
        if (team2 != null) {
            int i = 0;
            for(Player player : team2){
                initPlayerView(player,mTeam2Views.get(i));
                i++;
            }
        }

        GameStateManager.getInstance().setCallback(this);

        updateScore();

        Chronometer chronometer = (Chronometer) findViewById(R.id.time);
        chronometer.start();

        BluetoothManager btMgr = BluetoothManager.getInstance();
        btMgr.setBleListener(this);

        mBleManager = BleManager.getInstance(this);
        mBleManager.setBleListener(this);
        boolean isConnecting = mBleManager.connect(this, GameStateManager.getInstance().getTeam1().get(0).id);
    }

    private void initPlayerView(final Player player, View view) {
        if (player != null) {
            view.setTag(player.id);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Debug","OnClick triggered");
                    GameStateManager.getInstance().reduceHealth(player.id);
                }
            });

            ((TextView) view.findViewById(R.id.player_name)).setText(player.name);
            setPlayerHealth(view, 100);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Chronometer chronometer = (Chronometer) findViewById(R.id.time);
        chronometer.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Chronometer chronometer = (Chronometer) findViewById(R.id.time);
        chronometer.start();
    }

    /**
     * Finds all views of players and stores them in two arrays, one for each team.
     */
    private void fillTeamViews() {
        mTeam1Views.add(findViewById(R.id.team1Player1));
        mTeam1Views.add(findViewById(R.id.team1Player2));
        mTeam1Views.add(findViewById(R.id.team1Player3));

        mTeam2Views.add(findViewById(R.id.team2Player1));
        mTeam2Views.add(findViewById(R.id.team2Player2));
        mTeam2Views.add(findViewById(R.id.team2Player3));

        mPlayers.addAll(mTeam1Views);
        mPlayers.addAll(mTeam2Views);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        findViewById(R.id.overlay).setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        findViewById(R.id.overlay).setVisibility(View.VISIBLE);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);

        Chronometer c = (Chronometer) findViewById(R.id.time);
        c.stop();
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void setPlayerHealth(String playerId, int health) {
        for (View v : mPlayers) {
            if (v.getTag() != null && v.getTag().equals(playerId)) {
                setPlayerHealth(v, health);
            }
        }
        updateScore();
    }

    private void setPlayerHealth(View v, int health) {
        int fullSize = Math.round((GameInProgressActivity.this.getResources().getDimension(R.dimen.player_portrait_size)));
        int threeQuartersSize = Math.round((GameInProgressActivity.this.getResources().getDimension(R.dimen.player_portrait_size_75)));
        int halfSize = Math.round((GameInProgressActivity.this.getResources().getDimension(R.dimen.player_portrait_size_50)));
        int quarterSize = Math.round((GameInProgressActivity.this.getResources().getDimension(R.dimen.player_portrait_size_25)));

        ImageView playerHealth = (ImageView) v.findViewById(R.id.playerHealth);
        View playerPortrait = v.findViewById(R.id.player_portrait);

        if (health == 100) {
            playerHealth.setLayoutParams(new RelativeLayout.LayoutParams(fullSize, fullSize));
            Log.d("Debug", "Set health to 100");
        } else if (health == 75) {
            playerHealth.setLayoutParams(new RelativeLayout.LayoutParams(fullSize, threeQuartersSize));
            Log.d("Debug", "Set health to 75");
        } else if (health == 50) {
            playerHealth.setLayoutParams(new RelativeLayout.LayoutParams(fullSize, halfSize));
            Log.d("Debug", "Set health to 50");
        } else if (health == 25) {
            playerHealth.setLayoutParams(new RelativeLayout.LayoutParams(fullSize, quarterSize));
            Log.d("Debug", "Set health to 25");
        } else {
            playerHealth.setLayoutParams(new RelativeLayout.LayoutParams(fullSize, 0));
            Log.d("Debug", "Set health to 0");
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerHealth.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        playerHealth.setLayoutParams(params);
    }

    private void updateScore() {
        int[] scores = GameStateManager.getInstance().getScores();
        ((TextView) findViewById(R.id.scoreTextView)).setText("Score:" + scores[0] + "-" + scores[1]);

    }

    @Override
    public void setGameEnd(int winningTeam) {
        View viewWinner = null;
        View viewLooser = null;
        if(winningTeam == 1){
            viewWinner = findViewById(R.id.team1);
            viewLooser = findViewById(R.id.team2);
        }else if (winningTeam == 2){
            viewWinner = findViewById(R.id.team2);
            viewLooser = findViewById(R.id.team1);
        }

        viewWinner.setBackgroundColor(getResources().getColor(R.color.green_overlay));
        viewLooser.setBackgroundColor(getResources().getColor(R.color.red_overlay));
    }

    @Override
    public void resetGameState() {
        findViewById(R.id.team1).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.team2).setBackgroundColor(Color.TRANSPARENT);

        Chronometer chronometer = (Chronometer) findViewById(R.id.time);
        chronometer.start();
    }

    // Utility

    // region Send Data to UART
    protected void sendData(String text) {
        final byte[] value = text.getBytes(Charset.forName("UTF-8"));
        sendData(value);
    }

    protected void sendData(byte[] data) {
        mUartService = BleManager.getInstance(this).getGattService(UUID_SERVICE);
        if (mUartService != null) {
            // Split the value into chunks (UART service has a maximum number of characters that can be written )
            for (int i = 0; i < data.length; i += kTxMaxCharacters) {
                final byte[] chunk = Arrays.copyOfRange(data, i, Math.min(i + kTxMaxCharacters, data.length));
                BleManager.getInstance(getApplicationContext()).writeService(mUartService, UUID_TX, chunk);
            }
        } else {
            Log.w(TAG, "Uart Service not discovered. Unable to send data");
        }
    }

    // Callbacks
    @Override
    public void onConnected() {

    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onServicesDiscovered() {
        mUartService = BleManager.getInstance(this).getGattService(UUID_SERVICE);
    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "");
    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {
        Log.d(TAG, "");
    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }

    protected void enableRxNotifications() {
        BleManager.getInstance(this).enableNotification(mUartService, UUID_RX, true);
    }

    public void resetMatch(View v){
        GameStateManager.getInstance().resetGame();
    }

    public void resume(View v){
        hide();
    }
}
