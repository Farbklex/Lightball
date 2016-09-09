package io.lightball.lightball;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

/**
 * Created by bsc_ahoffmann on 10.09.16.
 */
public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_activity);
        NumberPicker np = (NumberPicker) findViewById(R.id.healthNumberPicker);
        np.setMinValue(0);
        np.setMaxValue(100);
    }

    public void sendHealth(View v){
        Toast.makeText(DebugActivity.this, "Sent", Toast.LENGTH_SHORT).show();
    }
}
