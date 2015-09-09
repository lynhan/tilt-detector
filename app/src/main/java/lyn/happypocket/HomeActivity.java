package lyn.happypocket;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;


// put value in list
// refresh value overnight

public class HomeActivity extends Activity implements SensorEventListener {

    String tag = "HomeActivity";

    Calendar cal;
    long msSinceLastDegreeChange;
    int tiltnum;
    int direction; // 1 is increasing, -1 is decreasing
    int startDeg;
    int prevDeg;
    int diff;

    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    float[] mGravity;
    float[] mGeomagnetic;

    SharedPreferences prefFile;
    SharedPreferences.Editor editor;

    TextView dayCountTextView;
    TextView powerTextView;
    int dayCountInt;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Log.i(tag, "accelerometer is null");
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer == null) {
            Log.i(tag, "magnetometer is null");
        }

        // Detect the window position
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                Log.i(tag, "Rotation 0");
                break;
            case Surface.ROTATION_90:
                Log.i(tag, "Rotation 90");
                break;
            case Surface.ROTATION_180:
                Log.i(tag, "Rotation 180");
                break;
            case Surface.ROTATION_270:
                Log.i(tag, "Rotation 270");
                break;
            default:
                Log.i(tag, "Rotation unknown");
                break;
        }
        initListeners();
        tiltnum = 0;
        direction = 1;
        startDeg = 0;
        prevDeg = 0;

        cal = Calendar.getInstance();
        msSinceLastDegreeChange = cal.getTimeInMillis();

        prefFile = getSharedPreferences("values", Context.MODE_PRIVATE);
        dayCountInt = prefFile.getInt("dayCountInt", 0);
        editor = prefFile.edit();

        dayCountTextView=(TextView)findViewById(R.id.day_count);
        dayCountTextView.setText(String.valueOf(dayCountInt));

        powerTextView=(TextView)findViewById(R.id.power);
        Button b = (Button) findViewById(R.id.button_id);

        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                incrementDayCount();
            }
        });
    }

    public void incrementDayCount() {
        this.dayCountInt += 1;
        editor.putInt("dayCountInt", dayCountInt).apply();
        dayCountTextView.setText(String.valueOf(dayCountInt));
        Log.i(tag, "incremented incremented incremented incremented incremented incremented");
    }




    /**
     *
     * @param deg
     *
     * reset tilt tracking if switched directions outside of
     * acceptable tilt range
     * or
     * tilt speed is too slow TODO
     */
    public void checkShyModeReset(int deg) {
        diff = Math.abs(deg - startDeg);
        if (diff < 15) {
            return;
        }
        if (diff <= 25) {
            // switched direction before entering tilt range (> 20)
            if (direction == 1 && deg < prevDeg) {
                Log.i(tag, "tilt too small");
                direction = -1;
                startDeg = deg;
                tiltnum = 0;
                return;
            }
            else if (direction == -1 && deg > prevDeg) {
                Log.i(tag, "tilt too small");
                startDeg = deg;
                direction = 1;
                tiltnum = 0;
                return;
            }
        }
        else if (diff > 25) {
            if (direction == 1 && deg < prevDeg) {
                direction = -1;
                startDeg = deg;
                tiltnum += 1;
                Log.i(tag, "POINT OF CHANGE: " + deg);
                Log.i(tag, "OKAY TILT. tiltnum: " + tiltnum);
            }
            else if (direction == -1 && deg > prevDeg) {
                startDeg = deg;
                direction = 1;
                tiltnum += 1;
                Log.i(tag, "POINT OF CHANGE: " + deg);
                Log.i(tag, "OKAY TILT. tiltnum: " + tiltnum);
            }
        }
        prevDeg = deg;
        if (tiltnum >= 3) {
            incrementDayCount();
            tiltnum = 0;
        }
    }

    public void checkBamModeReset(int deg) {

    }


    public void onSensorChanged(SensorEvent event) {
        //Log.i(tag, "onSensorChanged()");
        if (event.values == null) {
            Log.w(tag, "event.values is null");
            return;
        }
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values;
                break;
            default:
                Log.w(tag, "Unknown sensor type " + sensorType);
                return;
        }
        if (mGravity == null) {
            Log.w(tag, "mGravity is null");
            return;
        }
        if (mGeomagnetic == null) {
            Log.w(tag, "mGeomagnetic is null");
            return;
        }
        float R[] = new float[9];
        if (! SensorManager.getRotationMatrix(R, null, mGravity, mGeomagnetic)) {
            Log.w(tag, "getRotationMatrix() failed");
            return;
        }

        float orientation[] = new float[9];
        SensorManager.getOrientation(R, orientation);
        // azimuth, pitch and roll - use pitch
        float roll = orientation[1];
        int pitchDeg = (int) Math.round(Math.toDegrees(roll));
        powerTextView.setText(String.valueOf(pitchDeg));
        checkShyModeReset(pitchDeg);
    }


    // SENSOR

    public void initListeners()
    {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    }


    protected void onResume() {
        super.onResume();
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
//        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
//        sensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    public void onDestroy() {
//        sensorManager.unregisterListener(this);
        super.onDestroy();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
