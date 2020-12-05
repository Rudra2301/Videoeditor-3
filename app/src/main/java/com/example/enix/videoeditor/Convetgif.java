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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.Arrays;

import static com.example.enix.videoeditor.Editplayer.formatTimeUnit;
import static com.example.enix.videoeditor.Videoeffect.REQUEST_TAKE_GALLERY_VIDEO;

public class Convetgif extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


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
    RecyclerView option;
    Handler handler = new Handler();
    int time;
    String framefile;
    File dest1;
    int totaltime = 3;

    Option_time option_adapter;
    String[] fileNames;
    String[] effectName = {"1sec", "2sec", "3sec", "4sec", "5sec", "6sec", "7sec", "8sec", "9sec", "10sec"};

    Runnable seekrunnable = new Runnable() {
        public void run() {
            if (Convetgif.this.videoView.isPlaying()) {
                int curPos = Convetgif.this.videoView.getCurrentPosition();
                Convetgif.this.seekVideo.setProgress(curPos);
                try {
                    Convetgif.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) curPos));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curPos == Convetgif.this.duration) {
                    Convetgif.this.seekVideo.setProgress(0);
                    Convetgif.this.tvStartVideo.setText("00:00");
                    Convetgif.this.handler.removeCallbacks(Convetgif.this.seekrunnable);
                    return;
                }
                Convetgif.this.handler.postDelayed(Convetgif.this.seekrunnable, 200);
                return;
            }
            Convetgif.this.seekVideo.setProgress(Convetgif.this.duration);
            try {
                Convetgif.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) Convetgif.this.duration));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            Convetgif.this.handler.removeCallbacks(Convetgif.this.seekrunnable);
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

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Gif");


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

        option = (RecyclerView) findViewById(R.id.option_effect);
        option.setVisibility(View.VISIBLE);
        option_adapter = new Option_time(Convetgif.this, effectName);
        LinearLayoutManager layoutManager = new LinearLayoutManager(Convetgif.this, LinearLayoutManager.HORIZONTAL, false);
        option.setHasFixedSize(true);
        option.setLayoutManager(layoutManager);
        option.setItemAnimator(new DefaultItemAnimator());
        option.setAdapter(option_adapter);


        this.btnPlayVideo = (ImageButton) findViewById(R.id.btnPlayVideo_overlay);
        this.convert = (Button) findViewById(R.id.convert);
        convert.setBackgroundResource(R.drawable.gifconverter_selector);
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
                if (Convetgif.this.videoView == null || !Convetgif.this.videoView.isPlaying()) {
                    Convetgif.this.videoView.seekTo(Convetgif.this.seekVideo.getProgress());
                    Convetgif.this.btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);
                    Convetgif.this.handler.postDelayed(Convetgif.this.seekrunnable, 200);

                    videoView.start();

                } else {
                    videoView.pause();
                    Convetgif.this.handler.removeCallbacks(Convetgif.this.seekrunnable);
                    Convetgif.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                }

            }
        });


        option.addOnItemTouchListener(new RecyclerItemClickListener(Convetgif.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {


                totaltime = position + 1;


            }
        }));

        this.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {


                videoView.pause();
                Convetgif.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                Convetgif.this.btnPlayVideo.setVisibility(View.VISIBLE);
                Convetgif.this.videoView.seekTo(0);
                Convetgif.this.seekVideo.setProgress(0);
                Convetgif.this.tvStartVideo.setText("00:00");
                Convetgif.this.handler.removeCallbacks(Convetgif.this.seekrunnable);


            }
        });


        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(Convetgif.this);
                builder.setTitle("Gif Name");
                final EditText input = new EditText(Convetgif.this);




                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String m_Text = input.getText().toString();

                        if (Arrays.asList(fileNames).contains(m_Text+".gif")) {

                            Toast.makeText(Convetgif.this, "Folder Already Exit", Toast.LENGTH_LONG).show();



                        } else {

                            Videotogif(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000,m_Text);
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


    private void Videotogif(int startMs, int endMs, String name) {

      /*  File dest = new File(dir, filePrefix + "%03d" + fileExtn);

        Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());*/

        File mediaStorageDir1 = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Image");

        if (!mediaStorageDir1.exists()) {
            if (!mediaStorageDir1.mkdirs()) {

            }
        }
        String filePrefix1 = getString(R.string.app_name);
        String fileExtn1 = ".jpg";


        File dir = new File(mediaStorageDir1, "VideoEditor");
        int fileNo1 = 0;
        while (dir.exists()) {
            fileNo1++;
            dir = new File(mediaStorageDir1, "VideoEditor" + fileNo1);

        }
        dir.mkdir();
        framefile = dir.getAbsolutePath();

        dest1 = new File(dir, filePrefix1 + "%03d" + fileExtn1);


        String[] complexCommand1 = {"-y", "-ss", "" + startMs / 1000, "-t", "" + totaltime, "-i", inputvideo, "-vf", "fps=10,scale=320:-1:flags=lanczos,palettegen", dest1.getAbsolutePath()};
        execFFmpegBinary2(complexCommand1,name);


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
            Intent intent = new Intent(Convetgif.this, Pick_video.class);
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
                Convetgif.this.duration = Convetgif.this.videoView.getDuration();
                Convetgif.this.seekVideo.setMax(Convetgif.this.duration);
                Convetgif.this.tvStartVideo.setText("00:00");
                try {
                    Convetgif.this.tvEndVideo.setText(VideoPlayer.formatTimeUnit((long) Convetgif.this.duration));
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


                  /*  String[] complexCommand = {"-ss", "" + rangeSeekBar.getSelectedMinValue().intValue() * 1000 / 1000, "-t", ""+totaltime, "-i",inputvideo,"-i",dest1.getAbsolutePath(),"-filter_complex","fps=10,scale=320:-1:flags=lanczos[x];[x][1:v]paletteuse",filePath};
                    execFFmpegBinary2(complexCommand);
*/
                    Intent intent = new Intent(Convetgif.this, Gifpreview.class);
                    intent.putExtra(FILEPATH, filePath);
                    startActivity(intent);

                    File file = new File(filePath);
                    Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent1.setData(Uri.fromFile(file));
                    sendBroadcast(intent);


                }

                @Override
                public void onProgress(String message) {



                        progressDialog.setMessage("Processing.....");

                }

                @Override
                public void onFailure(String message) {
                    //Log.e("FFmpeg", "concat onFailure():" + message);
                    progressDialog.dismiss();
                }

                @Override
                public void onStart() {
                   /* videoView.pause();

                    Convetgif.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                    time = videoView.getCurrentPosition();
                    videoView.seekTo(time);
                    seekVideo.setProgress(time);
                    //Log.e("FFmpeg", "concat onStart():");*/

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

    private void execFFmpegBinary2(final String[] command, final String name) {
        try {
            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    //Log.e("FFmpeg", "concat onSuccess():");

                    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Gif");

                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {

                        }
                    }
                   /* String filePrefix = getString(R.string.app_name);*/
                    String fileExtn = ".gif";


                    File dest = new File(mediaStorageDir, name + fileExtn);
                    int fileNo = 0;
                    while (dest.exists()) {
                        fileNo++;
                        dest = new File(mediaStorageDir, name + fileExtn);
                    }
                    filePath = dest.getAbsolutePath();


                    String[] complexCommand = {"-ss", "" + rangeSeekBar.getSelectedMinValue().intValue() * 1000 / 1000, "-t", "" + totaltime, "-i", inputvideo, "-i", dest1.getAbsolutePath(), "-filter_complex", "fps=10,scale=320:-1:flags=lanczos[x];[x][1:v]paletteuse", filePath};
                    execFFmpegBinary1(complexCommand);


                }

                @Override
                public void onProgress(String message) {
                    progressDialog.setMessage("Processing.....");
                    //Log.e("FFmpeg", "concat onProgress():");
                }

                @Override
                public void onFailure(String message) {
                    //Log.e("FFmpeg", "concat onFailure():" + message);
                    progressDialog.dismiss();
                }

                @Override
                public void onStart() {
                    videoView.pause();

                    Convetgif.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
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

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {

            Log.d("ex with output : ", "" + e);

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
            //Log.e("ffmpeg ", " correct Loaded" + e.toString());
        }


    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(Convetgif.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Convetgif.this.finish();
                    }
                })
                .create()
                .show();

    }


    private String twoDigitString(int i) {
        return i == 0 ? "00" : i / 10 == 0 ? "0" + i : String.valueOf(i);
    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent(Convetgif.this, Pick_video.class);
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

    @Override
    protected void onPause() {
        videoView.pause();

        Convetgif.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
        time = videoView.getCurrentPosition();
        videoView.seekTo(time);
        seekVideo.setProgress(time);
        super.onPause();

    }

    @Override
    protected void onResume() {

        videoView.start();
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


}
