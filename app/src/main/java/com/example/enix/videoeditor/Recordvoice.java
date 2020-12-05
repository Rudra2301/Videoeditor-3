package com.example.enix.videoeditor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.enix.videoeditor.MixVidAud.RECORD_FILE;

public class Recordvoice extends AppCompatActivity {


    Button buttonStart, buttonStop, buttonPlayLastRecordAudio,
            buttonStopPlayingRecording, button5;
    String AudioSavePathInDevice = null;
    MediaRecorder recorder;
    int resultcode = 5;
    ImageView imageView;

    Random random;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordvoice);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        buttonStart = (Button) findViewById(R.id.button);
        buttonStop = (Button) findViewById(R.id.button2);
        imageView=(ImageView)findViewById(R.id.imageView);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.button3);
        buttonStopPlayingRecording = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);

        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);
        button5.setEnabled(false);

      /*  random = new Random();*/

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();

                //Log.e("inputrecvvf", AudioSavePathInDevice);
                intent.putExtra("audiofile", AudioSavePathInDevice);
                setResult(RECORD_FILE, intent);
                finish();


            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imageView.setImageResource(R.drawable.record_press);
                ChangerIcon(1);

                if (checkPermission()) {

                    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");


                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {

                        }
                    }

                    String filePrefix = getString(R.string.app_name);
                    String fileExtn = ".mp3";
                    File dest = new File(mediaStorageDir, filePrefix + fileExtn);

                    int fileNo = 0;
                    while (dest.exists()) {
                        fileNo++;
                        dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
                    }

                    Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());
                    AudioSavePathInDevice = dest.getAbsolutePath();


                    MediaRecorderReady();

                    try {
                        recorder.prepare();
                        recorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);

                    Toast.makeText(Recordvoice.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageResource(R.drawable.recordingaudio);
                ChangerIcon(2);
                recorder.stop();
                buttonStop.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                button5.setEnabled(true);
                Toast.makeText(Recordvoice.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {
                imageView.setImageResource(R.drawable.record_press);
                ChangerIcon(3);
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(Recordvoice.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageResource(R.drawable.recordingaudio);
                ChangerIcon(4);
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
            }
        });

    }

    public void ChangerIcon(int i) {
        if (i == 1) {
            this.buttonStart.setBackgroundResource(R.drawable.recording_on);
            this.buttonStop.setBackgroundResource(R.drawable.stop_unpress);
            this.buttonPlayLastRecordAudio.setBackgroundResource(R.drawable.recordplay_unpress);
            this.buttonStopPlayingRecording.setBackgroundResource(R.drawable.stopplaying_unpress);

        } else if (i == 2) {
            this.buttonStart.setBackgroundResource(R.drawable.recording_off);
            this.buttonStop.setBackgroundResource(R.drawable.stop_press);
            this.buttonPlayLastRecordAudio.setBackgroundResource(R.drawable.recordplay_unpress);
            this.buttonStopPlayingRecording.setBackgroundResource(R.drawable.stopplaying_unpress);

        } else if (i == 3) {
            this.buttonStart.setBackgroundResource(R.drawable.recording_off);
            this.buttonStop.setBackgroundResource(R.drawable.stop_unpress);
            this.buttonPlayLastRecordAudio.setBackgroundResource(R.drawable.recordplay_press);
            this.buttonStopPlayingRecording.setBackgroundResource(R.drawable.stopplaying_unpress);

        } else if (i == 4) {
            this.buttonStart.setBackgroundResource(R.drawable.recording_off);
            this.buttonStop.setBackgroundResource(R.drawable.stop_unpress);
            this.buttonPlayLastRecordAudio.setBackgroundResource(R.drawable.recordplay_unpress);
            this.buttonStopPlayingRecording.setBackgroundResource(R.drawable.stopplaying_press);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void MediaRecorderReady() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(AudioSavePathInDevice);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(Recordvoice.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(Recordvoice.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Recordvoice.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}



