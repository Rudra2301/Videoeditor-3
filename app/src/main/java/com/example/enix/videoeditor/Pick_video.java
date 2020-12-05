package com.example.enix.videoeditor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Pick_video extends AppCompatActivity {


    Button rate_us, video_effect, overlay_effect, blur_video, crop_video, mp3_converter, mixaudvid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_pick_video);

        rate_us = (Button) findViewById(R.id.rate_us);
       /* video_effect = (Button) findViewById(R.id.video_effect);
        overlay_effect = (Button) findViewById(R.id.overlay_effect);*/
        blur_video = (Button) findViewById(R.id.blur_video);
        crop_video = (Button) findViewById(R.id.crop_video);
        mp3_converter = (Button) findViewById(R.id.mp3_converter);
        mixaudvid = (Button) findViewById(R.id.mixaudvid);

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name)+"-Temp");
        if (mediaStorageDir.exists()){
            //Log.e("picvideo","in");
            deleteFolder(mediaStorageDir.getAbsolutePath());
        }


        rate_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +getPackageName()));
                startActivity(rateIntent);

            }
        });


        blur_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Pick_video.this,Convetgif.class);
                startActivity(intent);

            }
        });
        crop_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= 23) {
                    getPermission();
                }else {

                    Intent intent = new Intent(Pick_video.this, CropVideo.class);
                    startActivity(intent);

                }



            }
        });

        mp3_converter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= 23) {
                    getAudioPermission();
                } else {
                    Intent intent = new Intent(Pick_video.this, Convertmp3.class);
                    startActivity(intent);

                }



            }
        });
        mixaudvid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Pick_video.this, Videotoimage.class);
                startActivity(intent);

            }
        });


    }

    public void deleteFolder(String str) {

        File file = new File(str);
        int i = Build.VERSION.SDK_INT;
        if (file.exists()) {
            String[] list = file.list();
            for (String file2 : list) {
                File file3 = new File(file, file2);
                file3.delete();
                if (i > 18) {
                   /* MediaScannerConnection.scanFile(this, new String[]{file3.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String str, Uri uri) {
                        }
                    });*/
                    Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                    intent.setData(Uri.fromFile(file3));
                    sendBroadcast(intent);
                }
            }
            file.delete();
            if (i <= 18) {
                sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                return;
            }
            Intent intent2 = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            intent2.setData(Uri.fromFile(new File(file.toString())));
            sendBroadcast(intent2);
        }
    }
    private void getAudioPermission() {
        String[] params = null;
        String recordAudio = Manifest.permission.RECORD_AUDIO;
        String modifyAudio = Manifest.permission.MODIFY_AUDIO_SETTINGS;

        int hasRecordAudioPermission = ActivityCompat.checkSelfPermission(Pick_video.this, recordAudio);
        int hasModifyAudioPermission = ActivityCompat.checkSelfPermission(Pick_video.this, modifyAudio);
        List<String> permissions = new ArrayList<String>();

        if (hasRecordAudioPermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(recordAudio);
        if (hasModifyAudioPermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(modifyAudio);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }
        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(Pick_video.this,
                    params,
                    200);
        } else {

            Intent intent = new Intent(Pick_video.this, Convertmp3.class);
            startActivity(intent);


        }
    }


    private void getPermission() {
        String[] params = null;
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        List<String> permissions = new ArrayList<String>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }
        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(Pick_video.this,
                    params,
                    100);
        } else {
            Intent intent = new Intent(Pick_video.this, CropVideo.class);
            startActivity(intent);
        }

    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name)+"-Temp");
        if (mediaStorageDir.exists()){
            deleteFolder(mediaStorageDir.getAbsolutePath());
        }
        super.onResume();
    }






}
