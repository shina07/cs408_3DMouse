package com.forblind.threedmouse;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Arrays;

import Jama.Matrix;

/**
 * Created by user on 2015-12-03.
 */
public class CalibrateActivity extends Activity implements View.OnClickListener, SensorEventListener{

    ProgressBar progressBar;
    Button start;
    TextView explanation;

    double[] oldAcceleration;
    double[] newAcceleration;

    float[] oldRotation;
    float[] newRotation;

    float accelX, accelY, accelZ;

    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    private Sensor mRotationVector = null;

    int counter;

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        oldAcceleration = new double[3];
        newAcceleration = new double[3];

        oldRotation = new float[3];
        newRotation = new float[3];

        explanation = (TextView)findViewById(R.id.explanation);

        start = (Button)findViewById(R.id.startButton);
        start.setOnClickListener(this);

        progressBar = (ProgressBar)findViewById(R.id.calibrateProgress);
        progressBar.setMax(30);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startButton:
                CalibrateActivity.this.finish();
                Intent intent = new Intent(CalibrateActivity.this, MouseActivity.class);
                intent.putExtra("device", getIntent().getParcelableExtra("device"));
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_LINEAR_ACCELERATION:
                newAcceleration[0] = event.values[0];
                newAcceleration[1] = event.values[1];
                newAcceleration[2] = event.values[2];
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                newRotation[0] = event.values[0];
                newRotation[1] = event.values[1];
                newRotation[2] = event.values[2];
                break;
        }
        Log.e("TAG", oldRotation[0] + " :: " + newRotation[0]);
        Log.e("TAG", oldRotation[1] + " :: " + newRotation[1]);
        Log.e("TAG", oldRotation[2] + " :: " + newRotation[2]);

        if (Math.abs(newAcceleration[0] - oldAcceleration[0])<0.0875f &&
                Math.abs(newAcceleration[1] - oldAcceleration[1])<0.0875f &&
                Math.abs(newAcceleration[2] - oldAcceleration[2])<0.0875f &&
                Math.abs(newRotation[0] - oldRotation[0])<0.01f &&
                Math.abs(newRotation[1] - oldRotation[1])<0.01f &&
                Math.abs(newRotation[2] - oldRotation[2])<0.01f){

            progressBar.setProgress(++counter);

            accelX += newAcceleration[0];
            accelY += newAcceleration[1];
            accelZ += newAcceleration[2];

            if(counter == 30){

                ((MyApplication)getApplication()).acceleration_error[0] = accelX / 30.0f;
                ((MyApplication)getApplication()).acceleration_error[1] = accelY / 30.0f;
                ((MyApplication)getApplication()).acceleration_error[2] = accelZ / 30.0f;
                SensorManager.getRotationMatrixFromVector(((MyApplication)getApplication()).rotation_2d, newRotation);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        start.setVisibility(View.VISIBLE);
                        explanation.setText("센서보정완료");
                    }
                });
            }
        }
        else{
            counter = 0;
            progressBar.setProgress(0);

            accelX = 0f;
            accelY = 0f;
            accelZ = 0f;
        }
        oldRotation[0] = newRotation[0];
        oldRotation[1] = newRotation[1];
        oldRotation[2] = newRotation[2];
        oldAcceleration[0] = newAcceleration[0];
        oldAcceleration[1] = newAcceleration[1];
        oldAcceleration[2] = newAcceleration[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
