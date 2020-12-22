package com.example.pixelart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Settings extends AppCompatActivity {
    float dpiStep = 0.03f;
    Button okButton, cancelButton;
    SeekBar cursorDPISeekBar, queueSizeSeekBar;
    TextView cursorDPITV, queueSizeTV;

    public int queueSize;
    public float cursorDpi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        okButton = (Button)findViewById(R.id.btn_settings_ok);
        cancelButton = (Button)findViewById(R.id.btn_settings_cancel);

        cursorDPISeekBar = (SeekBar)findViewById(R.id.cursorDPISeekBar);
        queueSizeSeekBar = (SeekBar)findViewById(R.id.queueSizeSeekBar);
        cursorDPITV = (TextView)findViewById(R.id.cursorDPiTV);
        queueSizeTV = (TextView)findViewById(R.id.QueueSizeTV);

        Intent intent = getIntent();
        cursorDpi = intent.getFloatExtra("cursorDPI", 0f);
        queueSize = intent.getIntExtra("queueSize",0);

        int progress = (int)((cursorDpi-1.0f)/0.03);

        cursorDPISeekBar.setProgress(progress);
        cursorDPITV.setText(Float.toString(cursorDpi));
        //10 200
        queueSizeSeekBar.setProgress(queueSize);
        queueSizeTV.setText(Integer.toString(queueSize));

        cursorDPISeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float result = (float)(cursorDPISeekBar.getProgress()*0.03)+1;
                cursorDpi = (Math.round(result *100))/100f;
                cursorDPITV.setText(Float.toString(cursorDpi));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        queueSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                queueSize = queueSizeSeekBar.getProgress();
                queueSizeTV.setText(Integer.toString(queueSize));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("reCursorDpi",cursorDpi);
                intent.putExtra("reQueueSize", queueSize);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

}
