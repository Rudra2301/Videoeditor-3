package com.example.enix.videoeditor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Video.Media;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import static com.example.enix.videoeditor.R.id.videoView;

public class VideoPlayer extends Activity implements OnSeekBarChangeListener {


    ImageButton btnPlayVideo;
    ImageButton btnShare,create_done1;
    ImageButton btn_back,btn_reset;
    ImageButton fb,insta,what;
    int duration = 0;
    Handler handler = new Handler();
    boolean isPlay = false;
    /*ImageView ivScreen;*/
    int time;

    Context mContext = this;

    private static final String FILEPATH = "filepath";

    OnClickListener onclickplayvideo = new OnClickListener() {
        public void onClick(View v) {
            boolean z = false;
            //Log.e("", "play status " + VideoPlayer.this.isPlay);
            if (VideoPlayer.this.isPlay) {
                VideoPlayer.this.videoview.pause();
                VideoPlayer.this.handler.removeCallbacks(VideoPlayer.this.seekrunnable);
                VideoPlayer.this.btnPlayVideo.setVisibility(View.VISIBLE);
               /* VideoPlayer.this.ivScreen.setVisibility(View.VISIBLE);*/
                VideoPlayer.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
            } else {
                VideoPlayer.this.videoview.seekTo(VideoPlayer.this.seekVideo.getProgress());
                VideoPlayer.this.videoview.start();
                VideoPlayer.this.handler.postDelayed(VideoPlayer.this.seekrunnable, 200);
                VideoPlayer.this.videoview.setVisibility(View.VISIBLE);
               /* VideoPlayer.this.ivScreen.setVisibility(View.GONE);*/
                VideoPlayer.this.btnPlayVideo.setVisibility(View.VISIBLE);
                VideoPlayer.this.btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);
            }
            VideoPlayer videoPlayer = VideoPlayer.this;
            if (!VideoPlayer.this.isPlay) {
                z = true;
            }
            videoPlayer.isPlay = z;
        }
    };
    SeekBar seekVideo;
    Runnable seekrunnable = new Runnable() {
        public void run() {
            if (VideoPlayer.this.videoview.isPlaying()) {
                int curPos = VideoPlayer.this.videoview.getCurrentPosition();
                VideoPlayer.this.seekVideo.setProgress(curPos);
                try {
                    VideoPlayer.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) curPos));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curPos == VideoPlayer.this.duration) {
                    VideoPlayer.this.seekVideo.setProgress(0);
                    VideoPlayer.this.tvStartVideo.setText("00:00");
                    VideoPlayer.this.handler.removeCallbacks(VideoPlayer.this.seekrunnable);
                    return;
                }
                VideoPlayer.this.handler.postDelayed(VideoPlayer.this.seekrunnable, 200);
                return;
            }
            VideoPlayer.this.seekVideo.setProgress(VideoPlayer.this.duration);
            try {
                VideoPlayer.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) VideoPlayer.this.duration));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            VideoPlayer.this.handler.removeCallbacks(VideoPlayer.this.seekrunnable);
        }
    };
    Uri shareuri;
    TextView tvEndVideo;
    TextView tvStartVideo;
    String videoPath = "";
    VideoView videoview;
    TextView toolbar_title;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.act_videoplayer);


        this.videoPath = getIntent().getStringExtra(FILEPATH);

        //Log.e("My video path", videoPath);

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name)+"-Temp");
        deleteFolder(mediaStorageDir.getAbsolutePath());
       /* this.ivScreen = (ImageView) findViewById(R.id.ivScreen);*/
        this.videoview = (VideoView) findViewById(videoView);
        this.seekVideo = (SeekBar) findViewById(R.id.sbVideo);
        this.seekVideo.setOnSeekBarChangeListener(this);
        this.tvStartVideo = (TextView) findViewById(R.id.tvStartVideo);
        this.tvEndVideo = (TextView) findViewById(R.id.tvEndVideo);
        this.btnPlayVideo = (ImageButton) findViewById(R.id.btnPlayVideo);
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        toolbar_title=(TextView)findViewById(R.id.toolbar_title);

        this.fb = (ImageButton) findViewById(R.id.fb);
        this.insta = (ImageButton) findViewById(R.id.insta);
        this.what = (ImageButton) findViewById(R.id.what);


        fb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                facebook();
            }
        });



        insta.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                instagram();
            }
        });

        what.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                whatsup();

            }
        });

        toolbar_title.setText("Preview");
        this.videoview.setVideoPath(this.videoPath);
        this.videoview.seekTo(100);
       /* try {
            GetVideo(getApplicationContext(), this.videoPath);
        } catch (Exception e) {
        }*/
        this.videoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(VideoPlayer.this.getApplicationContext(), "Video Player Not Supproting", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        this.videoview.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                VideoPlayer.this.duration = VideoPlayer.this.videoview.getDuration();
                VideoPlayer.this.seekVideo.setMax(VideoPlayer.this.duration);
                VideoPlayer.this.tvStartVideo.setText("00:00");
                try {
                    VideoPlayer.this.tvEndVideo.setText(VideoPlayer.formatTimeUnit((long) VideoPlayer.this.duration));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        this.videoview.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                boolean z = false;
                VideoPlayer.this.videoview.setVisibility(View.GONE);
               /* VideoPlayer.this.ivScreen.setVisibility(View.VISIBLE);*/
                VideoPlayer.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                VideoPlayer.this.btnPlayVideo.setVisibility(View.VISIBLE);
                VideoPlayer.this.videoview.seekTo(0);
                VideoPlayer.this.seekVideo.setProgress(0);
                VideoPlayer.this.tvStartVideo.setText("00:00");
                VideoPlayer.this.handler.removeCallbacks(VideoPlayer.this.seekrunnable);
                VideoPlayer videoPlayer = VideoPlayer.this;
                if (!VideoPlayer.this.isPlay) {
                    z = true;
                }
                videoPlayer.isPlay = z;
            }
        });
        this.btnPlayVideo.setOnClickListener(this.onclickplayvideo);
        this.btn_back.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {

                Intent start = new Intent(VideoPlayer.this, Pick_video.class);
                start.setFlags(268468224);
                VideoPlayer.this.startActivity(start);
                VideoPlayer.this.finish();


            }
        });

        this.btnShare = (ImageButton) findViewById(R.id.create_done);
        this.create_done1 = (ImageButton) findViewById(R.id.create_done1);
        create_done1.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.GONE);
       /* this.btn_reset = (ImageButton) findViewById(R.id.btn_reset);
        this.btn_reset.setVisibility(View.GONE);*/
       /* btnShare.setImageResource(R.drawable.share_presed);*/

        this.create_done1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                VideoPlayer.this.Share();
            }
        });
    }

    public void facebook() {

        // Uri uri = Uri.parse("file://" + path);

        File file = new File(videoPath);
        Uri uri = FileProvider.getUriForFile(mContext,
                mContext.getPackageName() + ".provider", file);



        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage("com.facebook.katana");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("video/*");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share image File"));

    }

    public void instagram() {
        // Uri uri = Uri.parse("file://" + path);

        File file = new File(videoPath);
        Uri uri = FileProvider.getUriForFile(mContext,
                mContext.getPackageName() + ".provider", file);




        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage("com.instagram.android");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("video/*");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share image File"));

    }

    public void whatsup() {
        // Uri uri = Uri.parse("file://" + path);

        File file = new File(videoPath);
        Uri uri = FileProvider.getUriForFile(mContext,
                mContext.getPackageName() + ".provider", file);


        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage("com.whatsapp");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("video/*");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share image File"));


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

    public void GetVideo(Context c, String songPath) {
        Cursor cursor = managedQuery(Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "_data", "_display_name", "_size", "duration", "date_added", "album"}, "_data  like ?", new String[]{"%" + songPath + "%"}, " _id DESC");
        if (cursor.moveToFirst()) {
            Uri uri = Uri.withAppendedPath(Media.EXTERNAL_CONTENT_URI, String.valueOf(ContentUtill.getLong(cursor)));
            //Log.e("", "===Video View" + uri);
            this.shareuri = uri;
          //  this.ivScreen.setImageBitmap(Thumbnails.getThumbnail(getContentResolver(), Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("_id"))).longValue(), 1, null));
        }
    }

    private void Share() {

        File file=new File(videoPath);


        try {
            Intent share = new Intent("android.intent.action.SEND");
            share.setType("video/*");
            share.putExtra("android.intent.extra.STREAM", Uri.fromFile(file));
            startActivity(Intent.createChooser(share, "Share File"));

        } catch (Exception e) {
        }
    }


    public void onBackPressed() {
        super.onBackPressed();

        Intent start = new Intent(this, Pick_video.class);
        startActivity(start);
        finish();


    }


 @Override
 protected void onPause() {
     videoview.pause();

     VideoPlayer.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
     time = videoview.getCurrentPosition();
     videoview.seekTo(time);
     seekVideo.setProgress(time);
     super.onPause();

 }

    @Override
    protected void onResume() {
        videoview.seekTo(time);
        seekVideo.setProgress(time);
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (this.videoview.isPlaying()) {
            this.videoview.stopPlayback();
        }
        super.onStop();
    }

}
