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
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.enix.videoeditor.Editplayer.formatTimeUnit;
import static com.example.enix.videoeditor.Videoeffect.REQUEST_TAKE_GALLERY_VIDEO;

public class Videotoimage extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    private FFmpeg ffmpeg;
    private VideoView videoView;
    Uri selectedVideoUri;
    Button convert;
    private RangeSeekBar rangeSeekBar;
    private ProgressDialog progressDialog;
    private String filePath;
    private static final String FILEPATH = "filepath";
    String inputvideo;
    int videoend;
    int videowidth;
    int videoheight;
    ImageButton btnPlayVideo;
    SeekBar seekVideo;
    TextView tvEndVideo;
    private Runnable r;
    private TextView tvLeft, tvRight;
    TextView tvStartVideo;
    int duration = 0;
    int d = 0;
    String[] fileNames;
    Handler handler = new Handler();

    ArrayList<String> foldernameloist = new ArrayList<>();

    Runnable seekrunnable = new Runnable() {
        public void run() {
            if (Videotoimage.this.videoView.isPlaying()) {
                int curPos = Videotoimage.this.videoView.getCurrentPosition();
                Videotoimage.this.seekVideo.setProgress(curPos);
                try {
                    Videotoimage.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) curPos));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curPos == Videotoimage.this.duration) {
                    Videotoimage.this.seekVideo.setProgress(0);
                    Videotoimage.this.tvStartVideo.setText("00:00");
                    Videotoimage.this.handler.removeCallbacks(Videotoimage.this.seekrunnable);
                    return;
                }
                Videotoimage.this.handler.postDelayed(Videotoimage.this.seekrunnable, 200);
                return;
            }
            Videotoimage.this.seekVideo.setProgress(Videotoimage.this.duration);
            try {
                Videotoimage.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) Videotoimage.this.duration));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            Videotoimage.this.handler.removeCallbacks(Videotoimage.this.seekrunnable);
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

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Image");

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


        this.btnPlayVideo = (ImageButton) findViewById(R.id.btnPlayVideo_overlay);
        this.convert = (Button) findViewById(R.id.convert);
        convert.setBackgroundResource(R.drawable.imageconverter_selector);
        this.seekVideo = (SeekBar) findViewById(R.id.sbVideo);
        this.tvStartVideo = (TextView) findViewById(R.id.tvStartVideo);
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setEnabled(false);
        tvLeft = (TextView) findViewById(R.id.tvLeft);
        tvRight = (TextView) findViewById(R.id.tvRight);
        this.tvEndVideo = (TextView) findViewById(R.id.tvEndVideo);
        this.seekVideo.setOnSeekBarChangeListener(this);
        this.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (Videotoimage.this.videoView == null || !Videotoimage.this.videoView.isPlaying()) {
                    Videotoimage.this.videoView.seekTo(Videotoimage.this.seekVideo.getProgress());
                    Videotoimage.this.btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);
                    Videotoimage.this.handler.postDelayed(Videotoimage.this.seekrunnable, 200);

                    videoView.start();

                } else {
                    videoView.pause();
                    Videotoimage.this.handler.removeCallbacks(Videotoimage.this.seekrunnable);
                    Videotoimage.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                }

            }
        });


        this.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {


                videoView.pause();
                Videotoimage.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                Videotoimage.this.btnPlayVideo.setVisibility(View.VISIBLE);
                Videotoimage.this.videoView.seekTo(0);
                Videotoimage.this.seekVideo.setProgress(0);
                Videotoimage.this.tvStartVideo.setText("00:00");
                Videotoimage.this.handler.removeCallbacks(Videotoimage.this.seekrunnable);


            }
        });


        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(Videotoimage.this);
                builder.setTitle("Folder Name");
                final EditText input = new EditText(Videotoimage.this);




                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String m_Text = input.getText().toString();

                        if (Arrays.asList(fileNames).contains(m_Text)) {

                            Toast.makeText(Videotoimage.this, "Folder Already Exit", Toast.LENGTH_LONG).show();



                        } else {

                            extractImageVideo(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000, m_Text);
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

    private void extractImageVideo(int startMs, int endMs, String foldername) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Image");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

            }
        }
        String filePrefix = getString(R.string.app_name);
        String fileExtn = ".jpg";


        File dir = new File(mediaStorageDir, "VideoEditor");
        int fileNo = 0;
        while (dir.exists()) {
            fileNo++;
            dir = new File(mediaStorageDir, foldername);

        }
        dir.mkdir();
        filePath = dir.getAbsolutePath();
        File dest = new File(dir, filePrefix + "%03d" + fileExtn);

        Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());

        String[] complexCommand = {"-y", "-i", inputvideo, "-an", "-r", "1", "-ss", "" + startMs / 1000, "-t", "" + (endMs - startMs) / 1000, dest.getAbsolutePath()};
  /*   Remove -r 1 if you want to extract all video frames as images from the specified time duration.*/
        execFFmpegBinary1(complexCommand);


    }


    private void uploadVideo() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
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
            Intent intent = new Intent(Videotoimage.this, Pick_video.class);
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
        videoView.start();


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                d = mediaPlayer.getDuration() / 1000;

                tvLeft.setText("00:00:00");

                /*mediaPlayer.start();*/
                tvRight.setText(getTime(mediaPlayer.getDuration() / 1000));

                videoheight = mediaPlayer.getVideoHeight();
                videowidth = mediaPlayer.getVideoWidth();
                videoend = mediaPlayer.getDuration();
                Videotoimage.this.duration = Videotoimage.this.videoView.getDuration();
                Videotoimage.this.seekVideo.setMax(Videotoimage.this.duration);
                Videotoimage.this.tvStartVideo.setText("00:00");
                try {
                    Videotoimage.this.tvEndVideo.setText(VideoPlayer.formatTimeUnit((long) Videotoimage.this.duration));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                rangeSeekBar.setRangeValues(0, d);
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar.setSelectedMaxValue(d);
                rangeSeekBar.setEnabled(true);

                rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        videoView.seekTo((int) minValue * 1000);


                        tvLeft.setText(getTime((int) bar.getSelectedMinValue()));


                        tvRight.setText(getTime((int) bar.getSelectedMaxValue()));


                    }
                });

                final Handler handler = new Handler();
                handler.postDelayed(r = new Runnable() {
                    @Override
                    public void run() {

                        if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000)
                            videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                        handler.postDelayed(r, 1000);
                    }
                }, 1000);


            }

            private String getTime(int seconds) {
                int hr = seconds / 3600;
                int rem = seconds % 3600;
                int mn = rem / 60;
                int sec = rem % 60;
                return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
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

                    Intent intent = new Intent(Videotoimage.this, PreviewImageActivity.class);
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
        new AlertDialog.Builder(Videotoimage.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Videotoimage.this.finish();
                    }
                })
                .create()
                .show();

    }


    private String twoDigitString(int i) {
        return i == 0 ? "00" : i / 10 == 0 ? "0" + i : String.valueOf(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    protected void onResume() {
        videoView.start();
        super.onResume();
    }

    protected void onStop() {
        if (this.videoView.isPlaying()) {
            this.videoView.stopPlayback();
        }
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent(Videotoimage.this, Pick_video.class);
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
