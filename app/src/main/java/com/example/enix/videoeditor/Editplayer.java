package com.example.enix.videoeditor;

/**
 * Created by eNIX on 04-Sep-17.
 */

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.example.enix.videoeditor.CropVideo.mypreference;
import static com.example.enix.videoeditor.R.id.videoView;


public class Editplayer extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, Video_editor.OnDataPass, Blurvideo.OnDataPass1 {


    ImageButton btnPlayVideo;
    ImageButton btnShare;
    ImageButton btn_back, btn_reset;
    int duration;
    Handler handler = new Handler();
    boolean isPlay = false;
    String mainvideopath;
    int time;
    String outPutFolder = (Environment.getExternalStorageDirectory() + "/VideoEffects/");
    private static final String FILEPATH = "filepath";

    SeekBar seekVideo;
    Runnable seekrunnable = new Runnable() {
        public void run() {
            if (Editplayer.this.videoview.isPlaying()) {
                int curPos = Editplayer.this.videoview.getCurrentPosition();
                Editplayer.this.seekVideo.setProgress(curPos);
                try {
                    Editplayer.this.tvStartVideo.setText(Editplayer.formatTimeUnit((long) curPos));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curPos == Editplayer.this.duration) {
                    Editplayer.this.seekVideo.setProgress(0);
                    Editplayer.this.tvStartVideo.setText("00:00");
                    Editplayer.this.handler.removeCallbacks(Editplayer.this.seekrunnable);
                    return;
                }
                Editplayer.this.handler.postDelayed(Editplayer.this.seekrunnable, 200);
                return;
            }
            Editplayer.this.seekVideo.setProgress(Editplayer.this.duration);
            try {
                Editplayer.this.tvStartVideo.setText(Editplayer.formatTimeUnit((long) Editplayer.this.duration));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            Editplayer.this.handler.removeCallbacks(Editplayer.this.seekrunnable);
        }
    };
    Uri shareuri;
    TextView tvEndVideo;
    TextView tvStartVideo;
    String videoPath = "";
    VideoView videoview;
    ImageButton effect, blur, edit, overlay, mix;
    ProgressDialog progress;
    private FFmpeg ffmpeg;
    SharedPreferences sharedpreferences;
    private String filePath;
    TextView toolbar_title;
    LinearLayout editor_layout;
    String[] fileNames;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.editplayer);

        loadFFMpegBinary();


        //Log.e("oncreate edit", "player");
        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);


        if (getIntent().hasExtra("tempfile")) {

            this.videoPath = getIntent().getStringExtra("tempfile");

            //Log.e("tempfile", videoPath);

        }

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

            }
        }


        if (mediaStorageDir.exists()) {

            fileNames = mediaStorageDir.list();
        }


     /*   this.ivScreen = (ImageView) findViewById(R.id.ivScreen);*/
        this.videoview = (VideoView) findViewById(videoView);
        this.seekVideo = (SeekBar) findViewById(R.id.sbVideo);
        this.seekVideo.setOnSeekBarChangeListener(this);
        this.tvStartVideo = (TextView) findViewById(R.id.tvStartVideo);
        this.tvEndVideo = (TextView) findViewById(R.id.tvEndVideo);
        this.btnPlayVideo = (ImageButton) findViewById(R.id.btnPlayVideo);
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        this.btn_reset = (ImageButton) findViewById(R.id.btn_reset);
        this.effect = (ImageButton) findViewById(R.id.effect_button);
        this.mix = (ImageButton) findViewById(R.id.mix);
        this.overlay = (ImageButton) findViewById(R.id.overlay_button);
        this.edit = (ImageButton) findViewById(R.id.edit_button);
        this.blur = (ImageButton) findViewById(R.id.blur_button);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setVisibility(View.GONE);
        editor_layout = (LinearLayout) findViewById(R.id.editor_layout);
       /* this.option = (RecyclerView) findViewById(R.id.option);*/

        Editplayer.this.progress = new ProgressDialog(Editplayer.this);
        Editplayer.this.progress.setMessage("Please Wait");
        Editplayer.this.progress.setCancelable(false);


        overlay.setOnClickListener(this);
        effect.setOnClickListener(this);
        edit.setOnClickListener(this);
        blur.setOnClickListener(this);
        mix.setOnClickListener(this);

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor_layout.setVisibility(View.GONE);
                videoPath = sharedpreferences.getString("Mainvideo", "");

                Log.e("My video path", videoPath);

                videoview.setVideoPath(videoPath);
            }
        });


        this.videoview.setVideoPath(this.videoPath);
        this.videoview.seekTo(100);
      /*  try {
            GetVideo(getApplicationContext(), this.videoPath);
        } catch (Exception e) {
        }*/
        this.videoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(Editplayer.this.getApplicationContext(), "Video Player Not Supproting", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        this.videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                Editplayer.this.duration = Editplayer.this.videoview.getDuration();
                Editplayer.this.seekVideo.setMax(Editplayer.this.duration);
                Editplayer.this.tvStartVideo.setText("00:00");
                try {
                    Editplayer.this.tvEndVideo.setText(Editplayer.formatTimeUnit((long) Editplayer.this.duration));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        this.videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                boolean z = false;


                Editplayer.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                Editplayer.this.btnPlayVideo.setVisibility(View.VISIBLE);
                Editplayer.this.videoview.seekTo(0);
                Editplayer.this.seekVideo.setProgress(0);
                Editplayer.this.tvStartVideo.setText("00:00");
                Editplayer.this.handler.removeCallbacks(Editplayer.this.seekrunnable);
                Editplayer Editplayer = Editplayer.this;
                if (!Editplayer.this.isPlay) {
                    z = true;
                }
                Editplayer.isPlay = z;
            }
        });
        this.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (videoview == null || !videoview.isPlaying()) {
                    videoview.seekTo(seekVideo.getProgress());
                    btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);
                    handler.postDelayed(seekrunnable, 200);

                    videoview.start();

                } else {
                    videoview.pause();
                    handler.removeCallbacks(seekrunnable);
                    btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                }

            }
        });
        this.btn_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                Intent start = new Intent(Editplayer.this, Pick_video.class);

                Editplayer.this.startActivity(start);
              /*  Editplayer.this.finish();*/


            }
        });

        this.btnShare = (ImageButton) findViewById(R.id.btnShare);
        this.btnShare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                final AlertDialog.Builder builder = new AlertDialog.Builder(Editplayer.this);
                builder.setTitle("Video Name");
                final EditText input = new EditText(Editplayer.this);


                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String m_Text = input.getText().toString();

                        if (Arrays.asList(fileNames).contains(m_Text + ".mp4")) {

                            Toast.makeText(Editplayer.this, "Folder Already Exit", Toast.LENGTH_LONG).show();


                        } else {

                            specialtext(m_Text);
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
        new AlertDialog.Builder(Editplayer.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Editplayer.this.finish();
                    }
                })
                .create()
                .show();

    }


    public void ChangerIcon(int i) {
        if (i == 1) {
            this.effect.setBackgroundResource(R.drawable.effect_press);
            this.overlay.setBackgroundResource(R.drawable.overlay);
            this.mix.setBackgroundResource(R.drawable.mix);
            this.blur.setBackgroundResource(R.drawable.blur);
            this.edit.setBackgroundResource(R.drawable.edit);
        } else if (i == 2) {
            this.effect.setBackgroundResource(R.drawable.effect);
            this.overlay.setBackgroundResource(R.drawable.overlay_press);
            this.mix.setBackgroundResource(R.drawable.mix);
            this.blur.setBackgroundResource(R.drawable.blur);
            this.edit.setBackgroundResource(R.drawable.edit);
        } else if (i == 3) {
            this.effect.setBackgroundResource(R.drawable.effect);
            this.overlay.setBackgroundResource(R.drawable.overlay);
            this.mix.setBackgroundResource(R.drawable.addmusic_press);
            this.blur.setBackgroundResource(R.drawable.blur);
            this.edit.setBackgroundResource(R.drawable.edit);
        } else if (i == 4) {
            this.effect.setBackgroundResource(R.drawable.effect);
            this.overlay.setBackgroundResource(R.drawable.overlay);
            this.mix.setBackgroundResource(R.drawable.mix);
            this.blur.setBackgroundResource(R.drawable.blur_press);
            this.edit.setBackgroundResource(R.drawable.edit);
        } else if (i == 5) {
            this.effect.setBackgroundResource(R.drawable.effect);
            this.overlay.setBackgroundResource(R.drawable.overlay);
            this.mix.setBackgroundResource(R.drawable.mix);
            this.blur.setBackgroundResource(R.drawable.blur);
            this.edit.setBackgroundResource(R.drawable.edit_press);
        }
    }

    private void specialtext(String m_Text) {


        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");

        String Prefix = "font";
        String Extn = ".ttf";
        File nik = new File(mediaStorageDir, Prefix + Extn);
        InputStream is = null;
        try {
            is = getAssets().open("font.ttf");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(nik);
            fos.write(buffer);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        String filePrefix = "Watermark";
        String fileExtn = ".mp4";
        File dest = new File(mediaStorageDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
        }

        filePath = dest.getAbsolutePath();


        String[] cmd = {"-i", videoPath, "-vf", "experimental", "-vf", "drawtext=text='" + getString(R.string.app_name) + "':x=w-tw-10:y=h-th-10:fontfile=" + nik.getAbsolutePath() + ":fontsize=16:fontcolor=white:shadowcolor=black:shadowx=2:shadowy=2", filePath};
        execFFmpegBinary(cmd, m_Text);


    }


    private void execFFmpegBinary(final String[] command, final String m_Text) {
        try {
            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                public String outputformat;

                @Override
                public void onSuccess(String message) {
                    //Log.e("FFmpeg", "concat onSuccess():");


                    Editplayer.this.progress.dismiss();
                    this.outputformat = Editplayer.this.filePath.substring(Editplayer.this.filePath.lastIndexOf(".") + 1);
                    File f1 = new File(Editplayer.this.filePath);
                    MediaScannerConnection.scanFile(Editplayer.this, new String[]{f1.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String s, Uri uri) {

                          /*  Intent intent = new Intent(Editplayer.this, Editplayer.class);
                            intent.putExtra("tempfile", s);
                            Editplayer.this.startActivity(intent);*/


                            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));

                            if (!mediaStorageDir.exists()) {
                                if (!mediaStorageDir.mkdirs()) {

                                }
                            }

                            copyFileOrDirectory(s, mediaStorageDir.getAbsolutePath(), m_Text);

                            Intent intent = new Intent(Editplayer.this, VideoPlayer.class);
                            intent.putExtra(FILEPATH, mainvideopath);
                            startActivity(intent);
                        }
                    });


                }

                @Override
                public void onProgress(String message) {
                    /*progressDialog.setMessage("Processing...");*/


                    progress.setMessage("Processing......");


                }

                @Override
                public void onFailure(String message) {
                    //Log.e("FFmpeg", "concat onFailure():" + message);
                    Editplayer.this.progress.dismiss();
                }

                @Override
                public void onStart() {

                    Editplayer.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                    time = videoview.getCurrentPosition();
                    videoview.seekTo(time);
                    seekVideo.setProgress(time);
                    videoview.pause();
                    //Log.e("FFmpeg", "concat onStart():");
                    progress.show();
                    /*progressDialog.show();
                    progressDialog.setMessage("Processing...");*/
                }

                @Override
                public void onFinish() {
                    //Log.e("FFmpeg", "concat onFinish():");
                    progress.dismiss();
                  /*  progressDialog.dismiss();*/
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {

            Log.d("ex with output : ", "" + e);

        }
    }

    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromTouch) {
        if (fromTouch) {
            this.videoview.seekTo(progress);
            try {
                this.tvStartVideo.setText(formatTimeUnit((long) progress));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void onStartTrackingTouch(SeekBar arg0) {
    }

    public void onStopTrackingTouch(SeekBar arg0) {
    }

    public static String formatTimeUnit(long millis) throws ParseException {
        return String.format("%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(millis)), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))});
    }

  /*  public void GetVideo(Context c, String songPath) {
        Cursor cursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "_data", "_display_name", "_size", "duration", "date_added", "album"}, "_data  like ?", new String[]{"%" + songPath + "%"}, " _id DESC");
        if (cursor.moveToFirst()) {
            Uri uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(ContentUtill.getLong(cursor)));
            Log.e("", "===Video View" + uri);
            this.shareuri = uri;
            this.ivScreen.setImageBitmap(MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("_id"))).longValue(), 1, null));
        }
    }*/

    /*private void Share() {
        try {
            Intent share = new Intent("android.intent.action.SEND");
            share.setType("video*//*");
            share.putExtra("android.intent.extra.STREAM", this.shareuri);
            startActivity(Intent.createChooser(share, "Share File"));

        } catch (Exception e) {
        }
    }*/


    public void onBackPressed() {
        super.onBackPressed();

        Intent start = new Intent(this, Pick_video.class);
        startActivity(start);
      /*  finish();*/


    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.effect_button:
                ChangerIcon(1);
                Intent intent1 = new Intent(Editplayer.this, Videofilter.class);
                intent1.putExtra("tempfile", videoPath);

                startActivity(intent1);
                break;

            case R.id.overlay_button:
                ChangerIcon(2);
                Intent intent = new Intent(Editplayer.this, Videoeffect.class);
                intent.putExtra("tempfile", videoPath);
                startActivity(intent);
                break;
            case R.id.mix:
                ChangerIcon(3);
                Intent intent2 = new Intent(Editplayer.this, MixVidAud.class);
                intent2.putExtra("tempfile", videoPath);
                startActivity(intent2);
                break;
            case R.id.blur_button:
                editor_layout.setVisibility(View.VISIBLE);
                ChangerIcon(4);
                changeFragment(new Blurvideo());

                break;
            case R.id.edit_button:
                ChangerIcon(5);
                editor_layout.setVisibility(View.VISIBLE);
                changeFragment(new Video_editor());
                break;


        }


    }


    public void changeFragment(android.app.Fragment targetFragment) {
        Bundle bundle = new Bundle();
        bundle.putString("editfile", videoPath);
        targetFragment.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment, targetFragment);
        transaction.commit();
    }


    public void copyFileOrDirectory(String srcDir, String dstDir, String m_Text) {

        try {
            File src = new File(srcDir);

            /*String filePrefix = getString(R.string.app_name);*/
            String fileExtn = ".mp4";


            File dst = new File(dstDir, m_Text + fileExtn);


            while (dst.exists()) {

                dst = new File(dstDir, m_Text + fileExtn);
            }

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1, m_Text);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());

            if (destFile.exists()) {

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(destFile));
                sendBroadcast(intent);

                mainvideopath = destFile.getAbsolutePath();
            }


        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {

                destination.close();
            }
        }
    }

    @Override
    protected void onPause() {
        videoview.pause();
        //Log.e("MainActivity", "onPause");


        Editplayer.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
        time = videoview.getCurrentPosition();
        videoview.seekTo(time);
        seekVideo.setProgress(time);
        super.onPause();

    }

    @Override
    protected void onResume() {


        try {
            Editplayer.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Log.e("MainActivity", "onresume");

        File file = new File(outPutFolder);

        if (file.exists()) {

            deleteFolder(file.getAbsolutePath());

        }

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Filtertemp");

        if (mediaStorageDir.exists()) {
            deleteFolder(mediaStorageDir.getAbsolutePath());

        }


        videoview.setVideoPath(videoPath);
        videoview.seekTo(time);
        seekVideo.setProgress(time);
        super.onResume();
    }

    @Override
    protected void onStop() {
        //Log.e("MainActivity", "onStop");
        if (this.videoview.isPlaying()) {
            this.videoview.stopPlayback();
        }
        super.onStop();

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
    protected void onDestroy() {
        super.onDestroy();
        //Log.e("MainActivity", "onDestroy");


    }

    @Override
    public void onDataPass(String data) {

        videoPath = data;

        videoview.setVideoPath(videoPath);

        //Log.e("data",videoPath);

    }
}

