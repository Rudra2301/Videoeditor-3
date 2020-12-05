package com.example.enix.videoeditor;

import android.app.Activity;
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
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.text.ParseException;
import java.util.Arrays;

import static com.example.enix.videoeditor.Editplayer.formatTimeUnit;
import static com.example.enix.videoeditor.Videoeffect.REQUEST_TAKE_GALLERY_VIDEO;

public class Convertmp3 extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    private FFmpeg ffmpeg;
    private VideoView videoView;
    Uri selectedVideoUri;
    Button convert;
    private ProgressDialog progressDialog;
    private String filePath;
    LinearLayout hide_layout;
    private static final String FILEPATH = "filepath";
    String inputvideo;
    int videoend;
    int videowidth;
    int videoheight;
    private RangeSeekBar rangeSeekBar;

    ImageButton btnPlayVideo;
    SeekBar seekVideo;
    TextView tvEndVideo;
    TextView tvStartVideo;
    int duration;
    Handler handler = new Handler();
    int time;
    String[] fileNames;

    Runnable seekrunnable = new Runnable() {
        public void run() {
            if (Convertmp3.this.videoView.isPlaying()) {
                int curPos = Convertmp3.this.videoView.getCurrentPosition();
                Convertmp3.this.seekVideo.setProgress(curPos);
                try {
                    Convertmp3.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) curPos));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curPos == Convertmp3.this.duration) {
                    Convertmp3.this.seekVideo.setProgress(0);
                    Convertmp3.this.tvStartVideo.setText("00:00");
                    Convertmp3.this.handler.removeCallbacks(Convertmp3.this.seekrunnable);
                    return;
                }
                Convertmp3.this.handler.postDelayed(Convertmp3.this.seekrunnable, 200);
                return;
            }
            Convertmp3.this.seekVideo.setProgress(Convertmp3.this.duration);
            try {
                Convertmp3.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) Convertmp3.this.duration));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            Convertmp3.this.handler.removeCallbacks(Convertmp3.this.seekrunnable);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_convermp3);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        uploadVideo();


        loadFFMpegBinary();

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-MP3");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

            }
        }


        if (mediaStorageDir.exists()) {


            fileNames = mediaStorageDir.list();
        }

        videoView = (VideoView) findViewById(R.id.videoView);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setVisibility(View.GONE);

        this.hide_layout = (LinearLayout) findViewById(R.id.hide_layout);
        hide_layout.setVisibility(View.VISIBLE);
        this.btnPlayVideo = (ImageButton) findViewById(R.id.btnPlayVideo_overlay);
        this.convert = (Button) findViewById(R.id.convert);
        this.seekVideo = (SeekBar) findViewById(R.id.sbVideo);
        this.tvStartVideo = (TextView) findViewById(R.id.tvStartVideo);
        this.tvEndVideo = (TextView) findViewById(R.id.tvEndVideo);
        this.seekVideo.setOnSeekBarChangeListener(this);

        convert.setBackgroundResource(R.drawable.mp3converter_selector);
        this.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (Convertmp3.this.videoView.isPlaying()) {

                    videoView.pause();
                    Convertmp3.this.handler.removeCallbacks(Convertmp3.this.seekrunnable);
                    Convertmp3.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);


                } else {
                    videoView.start();
                    Convertmp3.this.videoView.seekTo(Convertmp3.this.seekVideo.getProgress());
                    Convertmp3.this.btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);
                    Convertmp3.this.handler.postDelayed(Convertmp3.this.seekrunnable, 200);
                }

            }
        });


        this.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {


                videoView.pause();
                Convertmp3.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                Convertmp3.this.btnPlayVideo.setVisibility(View.VISIBLE);
                Convertmp3.this.videoView.seekTo(0);
                Convertmp3.this.seekVideo.setProgress(0);
                Convertmp3.this.tvStartVideo.setText("00:00");
                Convertmp3.this.handler.removeCallbacks(Convertmp3.this.seekrunnable);


            }
        });


        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(Convertmp3.this);
                builder.setTitle("Song Name");
                final EditText input = new EditText(Convertmp3.this);


                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String m_Text = input.getText().toString();

                        if (Arrays.asList(fileNames).contains(m_Text+".mp3")) {

                            Toast.makeText(Convertmp3.this, "Folder Already Exit", Toast.LENGTH_LONG).show();


                        } else {

                            extractAudioVideo(m_Text);
                        }


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void extractAudioVideo(String filename) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-MP3");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }
        }


        String fileExtn = ".mp3";
        File dest = new File(mediaStorageDir, filename + fileExtn);

        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(mediaStorageDir, filename + fileExtn);
        }

        Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();

        String[] complexCommand = {"-i", inputvideo, "-b:a", "192K", "-vn", filePath};

        execFFmpegBinary1(complexCommand);

    }


    private void uploadVideo() {

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();

                inputvideo = getRealPathFromURI(selectedVideoUri);


                initvideoview();


            }

        } else {
            Intent intent = new Intent(Convertmp3.this, Pick_video.class);
            startActivity(intent);
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

    private void initvideoview() {

        videoView.setVideoPath(inputvideo);
        videoView.pause();


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                videoheight = mediaPlayer.getVideoHeight();
                videowidth = mediaPlayer.getVideoWidth();
                videoend = mediaPlayer.getDuration();

                Convertmp3.this.duration = Convertmp3.this.videoView.getDuration();
                Convertmp3.this.seekVideo.setMax(Convertmp3.this.duration);
                Convertmp3.this.tvStartVideo.setText("00:00");
                try {
                    Convertmp3.this.tvEndVideo.setText(VideoPlayer.formatTimeUnit((long) Convertmp3.this.duration));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    private void execFFmpegBinary1(final String[] command) {
        try {
            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    //Log.e("FFmpeg", "concat onSuccess():");

                    progressDialog.dismiss();

                    Intent intent = new Intent(Convertmp3.this, AudioPreviewActivity.class);
                    intent.putExtra(FILEPATH, filePath);
                    startActivity(intent);

                    File file = new File(filePath);
                    Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent1.setData(Uri.fromFile(file));
                    sendBroadcast(intent);


                }

                @Override
                public void onProgress(String message) {


                    progressDialog.setMessage("Processing......");

                }

                @Override
                public void onFailure(String message) {
                    //Log.e("FFmpeg", "concat onFailure():" + message);
                    progressDialog.dismiss();
                }

                @Override
                public void onStart() {
                    videoView.pause();

                    Convertmp3.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                    time = videoView.getCurrentPosition();
                    videoView.seekTo(time);
                    seekVideo.setProgress(time);
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
                    //Log.e("ffmpeg ", " not Loaded");
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
            //Log.e("ffmpeg ", " correct Loaded" + e.toString());
        }


    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(Convertmp3.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Convertmp3.this.finish();
                    }
                })
                .create()
                .show();

    }


    private String twoDigitString(int i) {
        return i == 0 ? "00" : i / 10 == 0 ? "0" + i : String.valueOf(i);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("Position", videoView.getCurrentPosition());
        videoView.pause();
    }

    @Override
    protected void onPause() {
        videoView.pause();


        Convertmp3.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
        time = videoView.getCurrentPosition();
        videoView.seekTo(time);
        seekVideo.setProgress(time);
        super.onPause();

    }

    @Override
    protected void onResume() {
        try {
            tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) duration));
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

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Convertmp3.this, Pick_video.class);
        startActivity(i);
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


}
