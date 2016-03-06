package com.forblind.threedmouse;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.forblind.threedmouse.utils.TDPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import Jama.Matrix;

/**
 * Created by SangHaWoo on 2015-11-15.
 * send 6 sensor information via bluetooth
 */
    public class MouseActivity extends AppCompatActivity implements SensorEventListener{

        private SensorManager mSensorManager = null;
        private Sensor mAccelerometer = null;
        private Sensor mRotationVector = null;

        boolean traverseMode, rotationMode;

        private boolean currentMode; // true : 2D, false : 3D

    public static Activity activity;

    ImageView leftButton, rightButton;

    float[] acceleration;
    float[] position;
    float[] velocity;
    float[] rotation_ori;
    float[] acceleration_error;
    int counter;
    Matrix orientation_ori_invM;
    float[] orientation;
    float[] rotation;
    long timestamp = 182537083;//10000000;
    int[] states;
    int linear_sensitivity, angular_senitivity;


    float[] linearAcceleration;

    boolean started;
    boolean acceleratorReceived;
    boolean rotationReceived;
    boolean orientationSet;
    boolean accelerationSet;

    boolean ld,lu,rd,ru;

    JSONObject toSend;

    Handler mHandler = new MyHandler();

    ConnectThread connectThread;
    ConnectedThread connectedThread;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(connectedThread!=null){
            connectedThread.cancel();
        }
        if(connectThread!=null){
            connectThread.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse);

        activity = this;

        SharedPreferences preferences = TDPreferences.getPreference(this);
        linear_sensitivity = preferences.getInt("linearSensitivity", 1);
        angular_senitivity = preferences.getInt("angularSensitivity", 15);

        acceleration = new float[3];
        acceleration_error = ((MyApplication)getApplication()).acceleration_error;
        position = new float[3];
        velocity = new float[3];
        orientation = new float[3];
        rotation = new float[9];
        states = new int[3];

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        leftButton = (ImageView)findViewById(R.id.leftButton);
        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(!currentMode){
                            started = true;
                            traverseMode = true;
                        } else {
                            ld = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!currentMode){
                            started = false;
                            orientationSet = false;
                            traverseMode = false;
                            velocity[0] = 0f;
                            velocity[1] = 0f;
                            velocity[2] = 0f;
                            position[0] = 0f;
                            position[1] = 0f;
                            position[2] = 0f;
                            states[0] = 0;
                            states[1] = 0;
                            states[2] = 0;
                            try{
                                new Thread().sleep(5);
                                toSend.put("positionX", 0);
                                toSend.put("positinoY", 0);
                                toSend.put("positinoZ", 0);
                                toSend.put("angleX", 0);
                                toSend.put("angleY", 0);
                                toSend.put("angleZ", 0);
                                toSend.put("stateZ", 3);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            connectedThread.write(toSend.toString().getBytes());
                        } else {
                            lu = true;
                        }
                        break;
                }
                return true;
            }
        });

        rightButton = (ImageView)findViewById(R.id.rightButton);
        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(!currentMode){
                            started = true;
                            rotationMode = true;
                        } else {
                            rd = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!currentMode){
                            started = false;
                            orientationSet = false;
                            rotationMode = false;
                            velocity[0] = 0f;
                            velocity[1] = 0f;
                            velocity[2] = 0f;
                            position[0] = 0f;
                            position[1] = 0f;
                            position[2] = 0f;
                            states[0] = 0;
                            states[1] = 0;
                            states[2] = 0;
                            try{
                                new Thread().sleep(5);
                                toSend.put("positionX", 0);
                                toSend.put("positinoY", 0);
                                toSend.put("positinoZ", 0);
                                toSend.put("angleX", 0);
                                toSend.put("angleY", 0);
                                toSend.put("angleZ", 0);
                                toSend.put("stateZ", 3);
                            } catch (JSONException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            connectedThread.write(toSend.toString().getBytes());
                        } else {
                            ru = true;
                        }
                        break;
                }
                return true;
            }
        });

        toSend = new JSONObject();

        BluetoothDevice device = getIntent().getParcelableExtra("device");
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(MouseActivity.this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(MouseActivity.this, mRotationVector, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //temp for commit
        mSensorManager.unregisterListener(MouseActivity.this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_LINEAR_ACCELERATION:
                String value00 = String.format("%d", (int)event.values[0]*50);
                String value01 = String.format("%d", (int)event.values[1]*50);
                String value02 = String.format("%d", (int)event.values[2]*50);
                acceleration = Arrays.copyOf(event.values, 3);
                acceleratorReceived = true;
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                String value10 = String.format("%.2f", event.values[0]);
                String value11 = String.format("%.2f", event.values[1]);
                String value12 = String.format("%.2f", event.values[2]);
                SensorManager.getRotationMatrixFromVector(rotation, event.values);
                SensorManager.getOrientation(rotation, orientation);
                if(!orientationSet && started)
                {
                    float[] orientation_ori = Arrays.copyOf(orientation, orientation.length);
                    rotation_ori = Arrays.copyOf(rotation,rotation.length);
                    Matrix original_rotation = new Matrix(f2d_2(rotation));
                    orientation_ori_invM = original_rotation.inverse();
                    orientationSet = true;
                }
                rotationReceived = true;
                break;
        }
//        Toast.makeText(MouseActivity.this, String.valueOf(event.timestamp), Toast.LENGTH_SHORT).show();
        if(!currentMode && orientationSet && acceleratorReceived && rotationReceived && started){
            Log.e("3D state", "Timestamp :: " + event.timestamp);
            acceleratorReceived = false;
            rotationReceived = false;
            updateState();
        } else if(currentMode && rotationReceived && acceleratorReceived){
            Log.e("2D state", "Timestamp :: " + event.timestamp);
            acceleratorReceived = false;
            rotationReceived = false;
            generate2d();
        }
    }

    private void generate2d(){
        try {
            float[] deltaAngle = new float[3];
            SensorManager.getAngleChange(deltaAngle, rotation, ((MyApplication) getApplication()).rotation_2d);
            toSend.put("positionX", (int)Math.toDegrees(deltaAngle[1])/10);
            toSend.put("positinoY", (int)Math.toDegrees(deltaAngle[0])/10);
            toSend.put("positinoZ", ld?1:0);
            toSend.put("angleX", lu?1:0);
            toSend.put("angleY", rd?1:0);
            toSend.put("angleZ", ru?1:0);
            toSend.put("stateZ", 2);
            ld = false;
            lu = false;
            rd = false;
            ru = false;
        }catch (JSONException e) {
            e.printStackTrace();
        }
        connectedThread.write(toSend.toString().getBytes());
    }

    @Override
    public void onBackPressed() {
        //do nothing.....
    }

    public static double[] f2d_1(float[] input)
    {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = input[i];
        }
        return output;
    }
    public static double[][] f2d_2(float[] input)
    {
        int n;
        if (input.length == 9)
        {
            n = 3;
        }
        else if(input.length == 16)
        {
            n = 4;
        }
        else
        {
            return null;
        }

        double[][] output = new double[n][n];
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                output[i][j] = input[n*i+j];
            }
        }
        return output;
    }

    private void updateState(){
        double[] real_acceleration;
        for(int i = 0; i < 3; i++)
        {
            acceleration[i] -= acceleration_error[i];
        }
        Matrix accelerationM = new Matrix(f2d_1(acceleration),3);
        Matrix body_orientationM = new Matrix(f2d_2(rotation));
        accelerationM = body_orientationM.times(accelerationM);
        accelerationM = orientation_ori_invM.times(accelerationM);
        real_acceleration = accelerationM.getColumnPackedCopy();
        float ts_ms = (timestamp/1000)/1000f;
        float threshold_start = 2.75f;
        float threshold = 3;
        float threshold_end = 2.0f;
        boolean make3 = false;
        boolean make4 = false;
        boolean make5 = false;
        boolean make0 = false;

        for (int i = 0; i < 3; i++)
        {
            switch(states[i]){
                case 0:
                    if(acceleration[i] > threshold_start)
                    {
                        states[i] = 1;
                        velocity[i] = (float)acceleration[i]*3f;
//                        velocity[i] = 3f;
                    }
                    else if(acceleration[i] < -threshold_start)
                    {
                        states[i] = 2;
                        velocity[i] = (float)acceleration[i]*3f;
//                        velocity[i] = -3f;
                    }
                    else
                    {
                        velocity[i] *= 0.75f;
//                        if(velocity[i] < 0) velocity[i] -= Math.max(velocity[i],-3f);
//                        else if (velocity[i] > 0) velocity[i] -= Math.min(velocity[i],3f);
                    }
                    break;
                case 1:
                    if(acceleration[i] > threshold)
                    {
                        states[i] = 1;
                        velocity[i] += acceleration[i] * 3f;
//                        velocity[i] += 15f;
                    }
                    else if(acceleration[i] < -threshold)
                    {
                        make3 = true;
//                        states[0] = 3;
//                        states[1] = 3;
//                        states[2] = 3;
                    }
                    else
                    {
                        velocity[i] *= 0.875f;
//                        velocity[i] += 9f;
                    }
                    break;
                case 2:
                    if(acceleration[i] > threshold)
                    {
                        make3 = true;
//                        states[0] = 3;
//                        states[1] = 3;
//                        states[2] = 3;
                    }
                    else if(acceleration[i] < -threshold)
                    {
                        states[i] = 2;
                        velocity[i] += acceleration[i] * 3f;
//                        velocity[i] -= 5f;
//                        velocity[i] -= 15f;
                    }
                    else
                    {
                        velocity[i] *= 0.875f;
//                        velocity[i] -= 3f;
//                        velocity[i] -= 9f;
                    }
                    break;
                case 3:
                    velocity[i] *= 0.875f;
//                    if(velocity[i] < 0) velocity[i] -= Math.max(velocity[i],-3f);
//                    else if (velocity[i] > 0) velocity[i] -= Math.min(velocity[i],3f);

                    if(acceleration[i] < threshold_end && acceleration[i] > -threshold_end) {
                        make4 = true;
//                        states[0] = 4;
//                        states[1] = 4;
//                        states[2] = 4;
                    }
                    break;
                case 4:
                    velocity[i] *= 0.875f;
//                    if(velocity[i] < 0) velocity[i] -= Math.max(velocity[i], -3f);
//                    else if (velocity[i] > 0) velocity[i] -= Math.min(velocity[i],3f);

                    if(acceleration[i] < threshold_end && acceleration[i] > -threshold_end) {
                        make5 = true;
//                        states[0] = 0;
//                        states[1] = 0;
//                        states[2] = 0;
                    }
                    break;
                case 5:
                    velocity[i] *= 0.875f;
//                    if(velocity[i] < 0) velocity[i] -= Math.max(velocity[i], -3f);
//                    else if (velocity[i] > 0) velocity[i] -= Math.min(velocity[i],3f);

                    if(acceleration[i] < threshold_end && acceleration[i] > -threshold_end) {
                        make0 = true;
//                        states[0] = 0;
//                        states[1] = 0;
//                        states[2] = 0;
                    }
                    break;
            }
        }
        if(make3){
            states[0] = 3;
            states[1] = 3;
            states[2] = 3;
            make3 = false;
        }
        if(make4){
            states[0] = 4;
            states[1] = 4;
            states[2] = 4;
            make4 = false;
        }
        if(make5){
            states[0] = 5;
            states[1] = 5;
            states[2] = 5;
            make4 = false;
        }
        if(make0){
            states[0] = 0;
            states[1] = 0;
            states[2] = 0;
            make0 = false;
        }


//        Log.e("TAG", "ax :: " + acceleration[0]);

        try {
            toSend.put("positionX", (traverseMode) ? (int) (velocity[0]) * linear_sensitivity: 0);
            toSend.put("positinoY", (traverseMode) ? (int) (velocity[1]) * linear_sensitivity: 0);
            toSend.put("positinoZ", (traverseMode) ? (int) (velocity[2]) * linear_sensitivity: 0);
            float[] deltaAngle = new float[3];
            SensorManager.getAngleChange(deltaAngle, rotation, rotation_ori);
            toSend.put("angleX", (rotationMode) ? (int)Math.toDegrees(deltaAngle[1])/angular_senitivity : 0);
            toSend.put("angleY", (rotationMode) ? (int)Math.toDegrees(deltaAngle[0])/angular_senitivity : 0);
            toSend.put("angleZ", (rotationMode) ? (int)Math.toDegrees(deltaAngle[2])/angular_senitivity : 0);
            toSend.put("stateZ", 3);
//            if(traverseMode){
//                toSend.put("positionX", 0);
//                toSend.put("positinoY", 0);
//                toSend.put("positinoZ", 0);
//                float[] deltaAngle = new float[3];
//                SensorManager.getAngleChange(deltaAngle, rotation, rotation_ori);
//                toSend.put("angleX", (int)Math.toDegrees(deltaAngle[1])/20);
//                toSend.put("angleY", (int)Math.toDegrees(deltaAngle[0])/20);
//                toSend.put("angleZ", (int)Math.toDegrees(deltaAngle[2])/20);
//                toSend.put("stateZ", 3);
//            } else {
//                toSend.put("positionX", (int) (velocity[0]));
//                toSend.put("positinoY", (int) (velocity[1]));
//                toSend.put("positinoZ", (int) (velocity[2]));
//                float[] deltaAngle = new float[3];
//                SensorManager.getAngleChange(deltaAngle, rotation, rotation_ori);
//                toSend.put("angleX", 0);
//                toSend.put("angleY", 0);
//                toSend.put("angleZ", 0);
//                toSend.put("stateZ", 3);
//            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        connectedThread.write(toSend.toString().getBytes());
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {e.printStackTrace();}
            mmSocket = tmp;
        }

        public void run() {

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.e("TAG", "enable to connect :::: 0 ");
                connectException.printStackTrace();
                try {
                    mmSocket.close();
                } catch (IOException closeException) {closeException.printStackTrace();}
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {e.printStackTrace();}
        }

        private void manageConnectedSocket(BluetoothSocket socket){
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        final int MESSAGE_READ = 9999;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {e.printStackTrace();}

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {e.printStackTrace();}
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {e.printStackTrace();}
        }

    }

    private static class MyHandler extends Handler {

        final int MESSAGE_READ = 9999;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    Log.d("TEST_MEESAGE_READ", msg.obj.toString());
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mouse, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem switchTo2D = menu.findItem(R.id.actinos_switch_2d);
        MenuItem switchTo3D = menu.findItem(R.id.actinos_switch_3d);
        if(currentMode){
            switchTo2D.setVisible(false);
            switchTo3D.setVisible(true);
        } else {
            switchTo2D.setVisible(true);
            switchTo3D.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MouseActivity.this, SettingActivity.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.actinos_switch_2d){
            currentMode = true;
            return true;
        } else if(id == R.id.actinos_switch_3d){
            currentMode = false;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
