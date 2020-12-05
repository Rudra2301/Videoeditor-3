package com.example.enix.videoeditor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import static com.example.enix.videoeditor.Editplayer.formatTimeUnit;
import static com.example.enix.videoeditor.Videofilter.showSingleOptionTextDialog;

public class MixVidAud extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    private VideoView videoView;

    private ProgressDialog progressDialog;

    private FFmpeg ffmpeg;
    Button select_audio, mute_video, record_audio;
    private String filePath;
    TextView audiofile;
    int selectitem = 0;
    String outPutFolder = (Environment.getExternalStorageDirectory() + "/VideoEffects/");
    private static final String FILEPATH = "filepath";
    String inputvideo;
    String inputaudio;
    ImageButton btnPlayVideo;
    ImageButton btn_back;
    ImageButton create_done;
    SeekBar seekVideo;
    TextView tvEndVideo;
    TextView tvStartVideo;
    int duration = 0;
    long seconds;
    String mutepath;
    Handler handler = new Handler();
    int time;
    public static final int RECORD_FILE = 113;


    Runnable seekrunnable = new Runnable() {
        public void run() {
            if (MixVidAud.this.videoView.isPlaying()) {
                int curPos = MixVidAud.this.videoView.getCurrentPosition();
                MixVidAud.this.seekVideo.setProgress(curPos);
                try {
                    MixVidAud.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) curPos));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curPos == MixVidAud.this.duration) {
                    MixVidAud.this.seekVideo.setProgress(0);
                    MixVidAud.this.tvStartVideo.setText("00:00");
                    MixVidAud.this.handler.removeCallbacks(MixVidAud.this.seekrunnable);
                    return;
                }
                MixVidAud.this.handler.postDelayed(MixVidAud.this.seekrunnable, 200);
                return;
            }
            MixVidAud.this.seekVideo.setProgress(MixVidAud.this.duration);
            try {
                MixVidAud.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) MixVidAud.this.duration));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            MixVidAud.this.handler.removeCallbacks(MixVidAud.this.seekrunnable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix_vid_aud);


        videoView = (VideoView) findViewById(R.id.mvdvideoView);
        select_audio = (Button) findViewById(R.id.select_audio);
        mute_video = (Button) findViewById(R.id.mute_video);
        record_audio = (Button) findViewById(R.id.record_audio);
        audiofile = (TextView) findViewById(R.id.audiofile);

        btn_back = (ImageButton) findViewById(R.id.btn_back);

        create_done = (ImageButton) findViewById(R.id.create_done);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);

        inputvideo = getIntent().getStringExtra("tempfile");

        /*uploadVideo();*/

        loadFFMpegBinary();


        this.btnPlayVideo = (ImageButton) findViewById(R.id.btnPlayVideo_overlay);
        this.seekVideo = (SeekBar) findViewById(R.id.sbVideo);
        this.tvStartVideo = (TextView) findViewById(R.id.tvStartVideo);
        this.tvEndVideo = (TextView) findViewById(R.id.tvEndVideo);
        this.seekVideo.setOnSeekBarChangeListener(MixVidAud.this);
        this.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (MixVidAud.this.videoView == null || !MixVidAud.this.videoView.isPlaying()) {
                    MixVidAud.this.videoView.seekTo(MixVidAud.this.seekVideo.getProgress());
                    MixVidAud.this.btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);
                    MixVidAud.this.handler.postDelayed(MixVidAud.this.seekrunnable, 200);

                    videoView.start();

                } else {
                    videoView.pause();
                    MixVidAud.this.handler.removeCallbacks(MixVidAud.this.seekrunnable);
                    MixVidAud.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                }

            }
        });

        initvideoview();

        this.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {


                videoView.pause();
                MixVidAud.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                MixVidAud.this.btnPlayVideo.setVisibility(View.VISIBLE);
                MixVidAud.this.videoView.seekTo(0);
                MixVidAud.this.seekVideo.setProgress(0);
                MixVidAud.this.tvStartVideo.setText("00:00");
                MixVidAud.this.handler.removeCallbacks(MixVidAud.this.seekrunnable);


            }
        });

        mute_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = showSingleOptionTextDialog(MixVidAud.this);
                TextView tvDialogHeading = (TextView) dialog.findViewById(R.id.tvDialogHeading);
                TextView tvDialogText = (TextView) dialog.findViewById(R.id.tvDialogText);
                TextView tvDialogSubmit = (TextView) dialog.findViewById(R.id.tvDialogSubmit);
                tvDialogHeading.setText("Process in Progress");
                tvDialogText.setText(R.string.mute);
                tvDialogSubmit.setText("Okay");
                tvDialogSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");
                        String filePrefix = "mute";
                        String fileExtn = ".mp4";
                        File dest = new File(mediaStorageDir, filePrefix + fileExtn);
                        int fileNo = 0;
                        while (dest.exists()) {
                            fileNo++;
                            dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
                        }

                        mutepath = dest.getAbsolutePath();

                        String[] cmd0 = new String[]{"-i", inputvideo, "-vcodec", "copy", "-an", mutepath};

                        execFFmpegBinary1(cmd0);

                        dialog.dismiss();
                    }

                });
                dialog.show();


            }
        });


        record_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MixVidAud.this, Recordvoice.class);
                startActivityForResult(intent, RECORD_FILE);
            }
        });

        select_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });


        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        create_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectitem == 1) {

                    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");
                    String filePrefix = "MIxaudvid";
                    String fileExtn = ".mp4";
                    File dest = new File(mediaStorageDir, filePrefix + fileExtn);
                    int fileNo = 0;
                    while (dest.exists()) {
                        fileNo++;
                        dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
                    }

                    filePath = dest.getAbsolutePath();

                    String[] cmd = new String[]{"-i", inputvideo, "-i", inputaudio, "-map", "1:a", "-map", "0:v", "-codec", "copy", "-shortest", filePath};
                    execFFmpegBinary(cmd);


                } else {

                    Toast.makeText(MixVidAud.this, "Please Select valid option", Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    private void initvideoview() {

        videoView.setVideoPath(inputvideo);
        videoView.pause();


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {


                MixVidAud.this.duration = MixVidAud.this.videoView.getDuration();
                seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
                MixVidAud.this.seekVideo.setMax(MixVidAud.this.duration);
                MixVidAud.this.tvStartVideo.setText("00:00");
                try {
                    MixVidAud.this.tvEndVideo.setText(VideoPlayer.formatTimeUnit((long) MixVidAud.this.duration));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    private void execFFmpegBinary(final String[] command) {
        try {


            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.e("FFmpeg", "concat onSuccess():");
                    Intent intent = new Intent(MixVidAud.this, Editplayer.class);
                    intent.putExtra("tempfile", filePath);
                    startActivity(intent);

                }

                @Override
                public void onProgress(String message) {


                    progressDialog.setMessage("Processing.....");

                }

                @Override
                public void onFailure(String message) {
                    progressDialog.dismiss();
                    Log.e("FFmpeg", "concat onFailure():" + message);
                }

                @Override
                public void onStart() {

                    videoView.pause();

                    MixVidAud.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                    time = videoView.getCurrentPosition();
                    videoView.seekTo(time);
                    seekVideo.setProgress(time);
                    Log.e("FFmpeg", "concat onStart():");
                    progressDialog.show();
                    progressDialog.setMessage("Processing...");
                }

                @Override
                public void onFinish() {
                    //Log.e("FFmpeg", "concat onFinish():");
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {

            Log.d("ex with output : ", "" + e);

        }
    }

    private void execFFmpegBinary1(final String[] command) {
        try {


            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    //Log.e("FFmpeg", "concat onSuccess():");
                    Intent intent = new Intent(MixVidAud.this, Editplayer.class);
                    intent.putExtra("tempfile", mutepath);
                    startActivity(intent);

                }

                @Override
                public void onProgress(String message) {


                    progressDialog.setMessage("Processing.........");


                }

                @Override
                public void onFailure(String message) {
                    //Log.e("FFmpeg", "concat onFailure():" + message);
                }

                @Override
                public void onStart() {
                    videoView.pause();
                    //Log.e("FFmpeg", "concat onStart():");
                    progressDialog.show();
                    progressDialog.setMessage("Processing...");
                }

                @Override
                public void onFinish() {
                    //Log.e("FFmpeg", "concat onFinish():");
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {

            Log.d("ex with output : ", "" + e);

        }
    }


    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {

                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.e("ffmpeg ", " not Loaded");
                    showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    Log.e("ffmpeg ", " correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
            Log.e("ffmpeg ", " correct Loaded" + e.toString());
        }


    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(MixVidAud.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MixVidAud.this.finish();
                    }
                })
                .create()
                .show();

    }

    /*private void uploadVideo() {

        Intent intent = new Intent();
        intent.setType("video*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);

    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


            if (resultCode == RECORD_FILE) {


                inputaudio = data.getStringExtra("audiofile");


                //Log.e("input", inputaudio);
                audiofile.setText(inputaudio);

            }




        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {

                selectitem = 1;
                Uri uri = data.getData();

                inputaudio = getRealPathFromURI(uri);

                audiofile.setText(inputaudio);

            }


        }
    }


    public String getRealPathFromURI(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b) {
            this.videoView.seekTo(i);
            try {
                this.tvStartVideo.setText(formatTimeUnit((long) i));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    protected void onPause() {
        videoView.pause();

        MixVidAud.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
        time = videoView.getCurrentPosition();
        videoView.seekTo(time);
        seekVideo.setProgress(time);
        super.onPause();

    }

    @Override
    protected void onResume() {

        //Log.e("onResume", "onResume");
        try {
            tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        videoView.seekTo(time);
        seekVideo.setProgress(time);
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (this.videoView.isPlaying()) {
            this.videoView.stopPlayback();
        }
        super.onStop();
    }
}
