package com.example.enix.videoeditor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
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

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.enix.videoeditor.Editplayer.formatTimeUnit;

public class Videofilter extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private FFmpeg ffmpeg;
    private ArrayList<String> cmd;
    private VideoView videoView;

    private ProgressDialog progressDialog;
    RecyclerView option;
    private String filePath;
    int selectitem = 0;
    LinearLayout hiddenlayout;

    String tempoutPutFolder = (Environment.getExternalStorageDirectory() + "/TempVideoEffects/");
    private Effect_adapter option_adapter;

    private static final String FILEPATH = "filepath";
    String inputvideo;

    int videoend;

    int videowidth;
    int videoheight;
    FrameLayout overlayframeLayout;

    String outPutFolder = (Environment.getExternalStorageDirectory() + "/VideoEffects/");
    ImageButton btnPlayVideo;
    ImageButton btn_back;
    ImageButton create_done;
    String Effecnamesave;
    SeekBar seekVideo;
    TextView tvEndVideo;
    TextView tvStartVideo;
    int duration  ;
    Handler handler = new Handler();
    String tempoutput;
    int time;
    TextView toolbar_title;

    Runnable seekrunnable = new Runnable() {
        public void run() {
            if (Videofilter.this.videoView.isPlaying()) {
                int curPos = Videofilter.this.videoView.getCurrentPosition();
                Videofilter.this.seekVideo.setProgress(curPos);
                try {
                    Videofilter.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) curPos));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curPos == Videofilter.this.duration) {
                    Videofilter.this.seekVideo.setProgress(0);
                    Videofilter.this.tvStartVideo.setText("00:00");
                    Videofilter.this.handler.removeCallbacks(Videofilter.this.seekrunnable);
                    return;
                }
                Videofilter.this.handler.postDelayed(Videofilter.this.seekrunnable, 200);
                return;
            }
            Videofilter.this.seekVideo.setProgress(Videofilter.this.duration);
            try {
                Videofilter.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) Videofilter.this.duration));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            Videofilter.this.handler.removeCallbacks(Videofilter.this.seekrunnable);
        }
    };

    String[] effectName = new String[]{"color", "blacknwhite", "colorswing", "blur", "negative", "noise", "unsharp", "sharp", "vignette", "oldfilm", "sepia", "redboost", "blue", "contrast", "bright"};
   // int[] effects = new int[]{R.drawable.o1, R.drawable.o2, R.drawable.o3, R.drawable.o4, R.drawable.o5, R.drawable.o6, R.drawable.o7, R.drawable.o8, R.drawable.o9, R.drawable.o10, R.drawable.o11, R.drawable.o12, R.drawable.o13, R.drawable.o14, R.drawable.o15 };
    int[] effects = new int[]{R.drawable.color, R.drawable.bnw, R.drawable.colorswing, R.drawable.blur1, R.drawable.negative, R.drawable.noise, R.drawable.unsharp, R.drawable.sharp, R.drawable.vignette, R.drawable.oldfilm, R.drawable.sepia, R.drawable.redboost, R.drawable.blue, R.drawable.contrast, R.drawable.brightness};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_videofilter);

        inputvideo = getIntent().getStringExtra("tempfile");


        loadFFMpegBinary();
        option = (RecyclerView) findViewById(R.id.option_effect);
       /* overlayframeLayout = (FrameLayout) findViewById(R.id.overlaylayout);*/
        videoView = (VideoView) findViewById(R.id.videoView);
        hiddenlayout = (LinearLayout) findViewById(R.id.hiddenlayout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        option_adapter = new Effect_adapter(Videofilter.this, effects, effectName);
        LinearLayoutManager layoutManager = new LinearLayoutManager(Videofilter.this, LinearLayoutManager.HORIZONTAL, false);
        option.setHasFixedSize(true);
        option.setLayoutManager(layoutManager);
        option.setItemAnimator(new DefaultItemAnimator());
        option.setAdapter(option_adapter);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Video Effect");

        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        this.btn_back.setOnClickListener(this);
        this.create_done = (ImageButton) findViewById(R.id.create_done);
        this.create_done.setOnClickListener(this);
        this.btnPlayVideo = (ImageButton) findViewById(R.id.btnPlayVideo_overlay);
        this.seekVideo = (SeekBar) findViewById(R.id.sbVideo);
        this.tvStartVideo = (TextView) findViewById(R.id.tvStartVideo);
        this.tvEndVideo = (TextView) findViewById(R.id.tvEndVideo);
        this.seekVideo.setOnSeekBarChangeListener(this);
        this.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (Videofilter.this.videoView == null || !Videofilter.this.videoView.isPlaying()) {
                    Videofilter.this.videoView.seekTo(Videofilter.this.seekVideo.getProgress());
                    Videofilter.this.btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);
                    Videofilter.this.handler.postDelayed(Videofilter.this.seekrunnable, 200);

                    videoView.start();

                } else {
                    videoView.pause();
                    Videofilter.this.handler.removeCallbacks(Videofilter.this.seekrunnable);
                    Videofilter.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                }

            }
        });


        initvideoview();


        this.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {


                videoView.pause();
                Videofilter.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                Videofilter.this.btnPlayVideo.setVisibility(View.VISIBLE);
                Videofilter.this.videoView.seekTo(0);
                Videofilter.this.seekVideo.setProgress(0);
                Videofilter.this.tvStartVideo.setText("00:00");
                Videofilter.this.handler.removeCallbacks(Videofilter.this.seekrunnable);


            }
        });


        option.addOnItemTouchListener(new RecyclerItemClickListener(Videofilter.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                selectitem = 1;

                hiddenlayout.setVisibility(View.VISIBLE);

                Effecnamesave = effectName[position];


                File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Filtertemp");

                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {


                    }
                }


                String filePrefix = effectName[position];
                String fileExtn = ".mp4";

                File dest = new File(mediaStorageDir, filePrefix + fileExtn);
                int fileNo = 0;
                while (dest.exists()) {
                    fileNo++;
                    dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
                }


                Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());

                tempoutput = dest.getAbsolutePath();


                ArrayList<String> cmd1 = effects(inputvideo, effectName[position], 3, tempoutput);

                String[] stringArray = cmd1.toArray(new String[0]);


                execFFmpegBinary(stringArray);


            }
        }));

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
                Videofilter.this.duration = Videofilter.this.videoView.getDuration();
                Videofilter.this.seekVideo.setMax(Videofilter.this.duration);
                Videofilter.this.tvStartVideo.setText("00:00");
                try {
                    Videofilter.this.tvEndVideo.setText(VideoPlayer.formatTimeUnit((long) Videofilter.this.duration));
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
                    //Log.e("FFmpeg", "concat onSuccess():");


                    videoView.setVideoPath(tempoutput);

                    progressDialog.dismiss();


                }

                @Override
                public void onProgress(String message) {
                    progressDialog.setMessage("Processing...");
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

                    Videofilter.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
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

    private void execFFmpegBinary1(final String[] command) {
        try {
            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    //Log.e("FFmpeg", "concat onSuccess():");


                    progressDialog.dismiss();
                    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Filtertemp");
                    deleteFolder(mediaStorageDir.getAbsolutePath());

                    Intent intent = new Intent(Videofilter.this, Editplayer.class);
                    intent.putExtra("tempfile", filePath);
                    startActivity(intent);


                }

                @Override
                public void onProgress(String message) {
                    Pattern pattern = Pattern.compile("time=([\\d\\w:]+)");
                    if (message.contains("speed"))

                    {
                        Matcher matcher = pattern.matcher(message);
                        matcher.find();
                        String tempTime = String.valueOf(matcher.group(1));

                        String[] arrayTime = tempTime.split(":");
                        long currentTime =
                                TimeUnit.HOURS.toSeconds(Long.parseLong(arrayTime[0]))
                                        + TimeUnit.MINUTES.toSeconds(Long.parseLong(arrayTime[1]))
                                        + Long.parseLong(arrayTime[2]);

                        long percentage = 100 * currentTime / duration;

                        progressDialog.setMessage("Processing....."+percentage);
                    }
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
        new AlertDialog.Builder(Videofilter.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Videofilter.this.finish();
                    }
                })
                .create()
                .show();

    }


    private String twoDigitString(int i) {
        return i == 0 ? "00" : i / 10 == 0 ? "0" + i : String.valueOf(i);
    }


    private String getDurationString(int i) {
        return twoDigitString(i / 3600) + ":" + twoDigitString((i % 3600) / 60) + ":" + twoDigitString(i % 60);
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();

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

    @Override
    public void onClick(View view) {


        switch (view.getId()) {

            case R.id.btn_back:
                super.onBackPressed();
                break;
            case R.id.create_done:

                hiddenlayout.setVisibility(View.GONE);



                final Dialog dialog = showSingleOptionTextDialog(Videofilter.this);
                TextView tvDialogHeading = (TextView) dialog.findViewById(R.id.tvDialogHeading);
                TextView tvDialogText = (TextView) dialog.findViewById(R.id.tvDialogText);
                TextView tvDialogSubmit = (TextView) dialog.findViewById(R.id.tvDialogSubmit);
                tvDialogHeading.setText("Process in Progress");
                tvDialogText.setText(R.string.dialogMessage);
                tvDialogSubmit.setText("Okay");
                tvDialogSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                               /* String yourRealPath = getPath(getActivity(), selectedVideoUri);*/

                            if (selectitem == 1){
                                overlaybuild();
                            } else {
                                Toast.makeText(Videofilter.this, "Please Select Effect first", Toast.LENGTH_SHORT).show();
                            }


                        dialog.dismiss();
                    }

                });
                dialog.show();

                break;

        }


    }

    public static Dialog showSingleOptionTextDialog(Context mContext) {
        Dialog textDialog = new Dialog(mContext, R.style.DialogAnimation);
        textDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        textDialog.setContentView(R.layout.dialog_singleoption_text);
        textDialog.setCancelable(false);
        return textDialog;
    }

    private void overlaybuild() {


        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }

        String filePrefix = "Filter";
        String fileExtn = ".mp4";

        File dest = new File(mediaStorageDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
        }


        Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());

        filePath = dest.getAbsolutePath();

        ArrayList<String> cmd1 = effects(inputvideo, Effecnamesave, videoend, filePath);

        String[] stringArray = cmd1.toArray(new String[0]);


        execFFmpegBinary1(stringArray);

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

    public ArrayList<String> effects(String str, String str2, int i, String str4) {
        String str5 = BuildConfig.FLAVOR;
        this.cmd = new ArrayList<>();
        this.cmd.add("-i");
        this.cmd.add(str);
        this.cmd.add("-t");
        this.cmd.add(getDurationString(i));
        this.cmd.add("-vf");

        int obj = -1;
        switch (str2.hashCode()) {
            case -1380798726:
                if (str2.equals("bright")) {
                    obj = 14;
                    break;
                }
                break;
            case -1378863061:
                if (str2.equals("oldfilm")) {
                    obj = 9;
                    break;
                }
                break;
            case -782688846:
                if (str2.equals("redboost")) {
                    obj = 11;
                    break;
                }
                break;
            case -566947070:
                if (str2.equals("contrast")) {
                    obj = 13;
                    break;
                }
                break;
            case -277645071:
                if (str2.equals("unsharp")) {
                    obj = 6;
                    break;
                }
                break;
            case 3027034:
                if (str2.equals("blue")) {
                    obj = 12;
                    break;
                }
                break;
            case 3027047:
                if (str2.equals("blur")) {
                    obj = 3;
                    break;
                }
                break;
            case 104998682:
                if (str2.equals("noise")) {
                    obj = 5;
                    break;
                }
                break;
            case 109324790:
                if (str2.equals("sepia")) {
                    obj = 10;
                    break;
                }
                break;
            case 109400042:
                if (str2.equals("sharp")) {
                    obj = 7;
                    break;
                }
                break;
            case 921111605:
                if (str2.equals("negative")) {
                    obj = 4;
                    break;
                }
                break;
            case 1194865978:
                if (str2.equals("blacknwhite")) {
                    obj = 1;
                    break;
                }
                break;
            case 1245309242:
                if (str2.equals("vignette")) {
                    obj = 8;
                    break;
                }
                break;
            case 1308243323:
                if (str2.equals("colorswing")) {
                    obj = 2;
                    break;
                }
                break;
            case 1379043793:
                if (str2.equals("color")) {
                    obj = 0;
                    break;
                }
                break;
        }
        switch (obj) {
            case 0:
                this.cmd.add(str5 + "hue=h=0:s=2.5");
                break;
            case 1:
                this.cmd.add(str5 + "hue=h=0:s=0");
                break;
            case 2:
                this.cmd.add(str5 + "hue=H=2*PI*t: s=sin(2*PI*t)+1");
                break;
            case 3:
                this.cmd.add(str5 + "boxblur=2:1:cr=0.5:ar=0.5");
                break;
            case 4:
                this.cmd.add(str5 + "lutyuv=y=negval");
                break;
            case 5:
                this.cmd.add(str5 + "noise=alls=20:allf=t+u");
                break;
            case 6:
                this.cmd.add(str5 + "unsharp=7:7:-2:7:7:-2");
                break;
            case 7:
                this.cmd.add(str5 + "unsharp=luma_msize_x=7:luma_msize_y=7:luma_amount=2.5");
                break;
            case 8:
                this.cmd.add(str5 + "vignette='PI/4+random(1)*PI/50':eval=frame");
                break;
            case 9:
                this.cmd.add(str5 + "hue=h=0:s=-0.4");
                break;
            case 10:
                this.cmd.add(str5 + "hue=h=0.35:s=0.25");
                break;
            case 11:
                this.cmd.add(str5 + "hue=h=-0.05:s=0.20");
                break;
            case 12:
                this.cmd.add(str5 + "hue=h=2.15:s=0.25");
                break;
            case 13:
                this.cmd.add(str5 + "colorlevels=rimin=0.039:gimin=0.039:bimin=0.039:rimax=0.96:gimax=0.96:bimax=0.96");
                this.cmd.add("-pix_fmt");
                this.cmd.add("yuv420p");
                break;
            case 14:
                this.cmd.add(str5 + "colorlevels=romin=0.5:gomin=0.5:bomin=0.5");
                this.cmd.add("-pix_fmt");
                this.cmd.add("yuv420p");
                break;
        }
        this.cmd.add("-metadata:s:v");
        this.cmd.add("rotate=0");
        this.cmd.add("-c:v");
        this.cmd.add("libx264");
        this.cmd.add("-preset");
        this.cmd.add("ultrafast");
        this.cmd.add("-ar");
        this.cmd.add("44100");
        this.cmd.add("-c:a");
        this.cmd.add("aac");
        this.cmd.add("-ab");
        this.cmd.add("128k");
        this.cmd.add("-strict");
        this.cmd.add("-2");
        this.cmd.add(str4);
        return this.cmd;
    }


    @Override
    protected void onPause() {
        videoView.pause();

        Videofilter.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
        time = videoView.getCurrentPosition();
        videoView.seekTo(time);
        seekVideo.setProgress(time);
        super.onPause();

    }

    @Override
    protected void onResume() {

        try {
            tvStartVideo.setText(VideoPlayer.formatTimeUnit((long)  time));
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
    protected void onDestroy() {
        super.onDestroy();
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Filtertemp");
        if (mediaStorageDir.exists()) {
            deleteFolder(mediaStorageDir.getAbsolutePath());

        }



    }
}
