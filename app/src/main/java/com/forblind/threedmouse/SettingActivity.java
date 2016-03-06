package com.forblind.threedmouse;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.forblind.threedmouse.utils.TDPreferences;

/**
 * Created by user on 2015-12-03.
 */
public class SettingActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{

    TextView linearValue, angularValue;
    SeekBar linear, angular;
    Button recalibrateButton, reselectButton, quitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        SharedPreferences preferences = TDPreferences.getPreference(this);
        int linear_sensitivity = preferences.getInt("linearSensitivity", 1);
        int angular_senitivity = preferences.getInt("angularSensitivity", 15);

        linearValue = (TextView)findViewById(R.id.linearValue);
        linearValue.setText("linear : " + linear_sensitivity);
        angularValue = (TextView)findViewById(R.id.angularValue);
        angularValue.setText("angular : " + angular_senitivity);

        linear_sensitivity -= 1;

        angular_senitivity /= 5;
        angular_senitivity -= 3;

        recalibrateButton = (Button)findViewById(R.id.recalibrateButton);
        recalibrateButton.setOnClickListener(this);

        reselectButton = (Button)findViewById(R.id.reselectButton);
        reselectButton.setOnClickListener(this);

        quitButton = (Button)findViewById(R.id.quitButton);
        quitButton.setOnClickListener(this);

        linear = (SeekBar)findViewById(R.id.sensitivityLinearBar);
        linear.setProgress(linear_sensitivity);
        linear.setOnSeekBarChangeListener(this);

        angular = (SeekBar)findViewById(R.id.sensitivityAngularBar);
        angular.setProgress(angular_senitivity);
        angular.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.sensitivityLinearBar:
                linearValue.setText("linear : " + (progress + 1));
                SharedPreferences preferences1 = TDPreferences.getPreference(SettingActivity.this);
                SharedPreferences.Editor editor1 = preferences1.edit();
                editor1.putInt("linearSensitivity", progress + 1);
                editor1.apply();
                break;
            case R.id.sensitivityAngularBar:
                angularValue.setText("angular : " + (5*progress + 15));
                SharedPreferences preferences2 = TDPreferences.getPreference(SettingActivity.this);
                SharedPreferences.Editor editor2 = preferences2.edit();
                editor2.putInt("angularSensitivity", 5*progress + 15);
                editor2.apply();
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.recalibrateButton:
                SettingActivity.this.finish();
                Intent recalibrate = new Intent(SettingActivity.this, CalibrateActivity.class);
                startActivity(recalibrate);
                break;
            case R.id.reselectButton:
                SettingActivity.this.finish();
                Intent reselect = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(reselect);
                break;
            case R.id.quitButton:
                SettingActivity.this.finish();
                MouseActivity.activity.finish();
                break;
        }
    }
}
