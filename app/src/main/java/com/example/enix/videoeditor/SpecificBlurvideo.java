package com.example.enix.videoeditor;

/**
 * Created by eNIX on 04-Sep-17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.edmodo.cropper.CropImageView;
import com.edmodo.cropper.cropwindow.edge.Edge;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.text.ParseException;

import static com.example.enix.videoeditor.Editplayer.formatTimeUnit;
import static com.example.enix.videoeditor.R.id.videoview;
import static com.example.enix.videoeditor.Videoeffect.REQUEST_TAKE_GALLERY_VIDEO;
import static com.example.enix.videoeditor.Videofilter.showSingleOptionTextDialog;


public class SpecificBlurvideo extends Activity implements SeekBar.OnSeekBarChangeListener {
    View.OnClickListener Custom = new View.OnClickListener() {
        public void onClick(View v) {
            SpecificBlurvideo.this.ChangerIcon(1);
            SpecificBlurvideo.this.cropView.setFixedAspectRatio(false);
        }
    };
    View.OnClickListener Landscape = new View.OnClickListener() {
        public void onClick(View v) {
            SpecificBlurvideo.this.ChangerIcon(4);
            SpecificBlurvideo.this.cropView.setFixedAspectRatio(true);
            SpecificBlurvideo.this.cropView.setAspectRatio(16, 8);
        }
    };
    View.OnClickListener OnClickBack = new View.OnClickListener() {
        public void onClick(View v) {
           onBackPressed();
        }
    };
    View.OnClickListener OnClickDone = new View.OnClickListener() {
        public void onClick(View v) {
            //Log.e("", "== done ==");
            if (SpecificBlurvideo.this.videoView != null && SpecificBlurvideo.this.videoView.isPlaying()) {
                SpecificBlurvideo.this.videoView.pause();
            }


            final Dialog dialog = showSingleOptionTextDialog(SpecificBlurvideo.this);
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
                    new CompressTask().execute();
                    dialog.dismiss();
                }

            });
            dialog.show();
            SpecificBlurvideo.this.setRealPosition();

        }
    };
    View.OnClickListener Potrait = new View.OnClickListener() {
        public void onClick(View v) {
            SpecificBlurvideo.this.ChangerIcon(3);
            SpecificBlurvideo.this.cropView.setFixedAspectRatio(true);
            SpecificBlurvideo.this.cropView.setAspectRatio(8, 16);
        }
    };
    View.OnClickListener Square = new View.OnClickListener() {
        public void onClick(View v) {
            SpecificBlurvideo.this.ChangerIcon(2);
            SpecificBlurvideo.this.cropView.setFixedAspectRatio(true);
            SpecificBlurvideo.this.cropView.setAspectRatio(10, 10);
        }
    };
    Bundle b;
    TextView toolbar_title;
    ImageButton btnPlayVideo;
    ImageButton btn_back;
    ImageButton create_done;
    CropImageView cropView;
    String endTime;
    int time;
    ImageButton imbtn_custom;
    ImageButton imgbtn_cland;
    ImageButton imgbtn_port;
    ImageButton imgbtn_square;
    ImageButton imgbtn_three;
    ImageView ivScreen;
    int leftBottomX;
    int leftBottomY;
    int leftTopX;
    int leftTopY;
    int oVHeight;
    int oVWidth;
    String outPutPath;
    String outputfile;
    ProgressDialog progress;
    int rightBottomX;
    int rightBottomY;
    int rightTopX;
    int rightTopY;
    int rotatiobDegree;
    int screenWidth;
    String startTime = "00";
    int tempBottom;
    int tempLeft;
    int tempRight;
    int tempTop;
    
    private Uri selectedVideoUri;
    private String filePath;
    private static final String FILEPATH = "filepath";
    String inputvideo;
    int videoend;
    int videowidth;
    int videoheight;
    SeekBar seekVideo;
    int duration = 0;
    Handler handler = new Handler();
    Runnable seekrunnable = new Runnable() {
        public void run() {
            if (videoView.isPlaying()) {
                int curPos = videoView.getCurrentPosition();
                seekVideo.setProgress(curPos);
                try {
                    textViewLeft.setText(VideoPlayer.formatTimeUnit((long) curPos));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curPos == duration) {
                    seekVideo.setProgress(0);
                    textViewRight.setText("00:00");
                    handler.removeCallbacks(seekrunnable);
                    return;
                }
                handler.postDelayed(seekrunnable, 200);
                return;
            }
            seekVideo.setProgress(duration);
            try {
                textViewLeft.setText(VideoPlayer.formatTimeUnit((long) duration));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            handler.removeCallbacks(seekrunnable);
        }
    };
    private FFmpeg ffmpeg;
    private TextView textViewLeft;
    private TextView textViewRight;
    View.OnClickListener three = new View.OnClickListener() {
        public void onClick(View v) {
            SpecificBlurvideo.this.ChangerIcon(5);
            SpecificBlurvideo.this.cropView.setFixedAspectRatio(true);
            SpecificBlurvideo.this.cropView.setAspectRatio(3, 2);
        }
    };
    private VideoPlayerState videoPlayerState = new VideoPlayerState();
    VideoSliceSeekBar videoSliceSeekBar;
   // private StateObserver videoStateObserver = new StateObserver();
    VideoView videoView;
    String videopath;

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        if (b) {
            this.videoView.seekTo(i);
            try {
                this.textViewRight.setText(formatTimeUnit((long) i));
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

    @SuppressLint({"NewApi"})
    private class CompressTask extends AsyncTask<Void, Void, Void> {
        String inputFileName;
        private String outputformat;

        public CompressTask() {

            SpecificBlurvideo.this.progress = new ProgressDialog(SpecificBlurvideo.this);
            SpecificBlurvideo.this.progress.setMessage("Please Wait");
            SpecificBlurvideo.this.progress.setCancelable(false);


            videoView.pause();

            btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
            time = videoView.getCurrentPosition();
            videoView.seekTo(time);
            seekVideo.setProgress(time);
            SpecificBlurvideo.this.progress.show();

        }



        protected Void doInBackground(Void... urls) {
            int w;
            int h;
            String outPutPath;
            int leftTopy;
            if (SpecificBlurvideo.this.rotatiobDegree == 90) {
                SpecificBlurvideo.this.leftTopX = SpecificBlurvideo.this.tempTop;
                leftTopy = SpecificBlurvideo.this.tempLeft;
                SpecificBlurvideo.this.rightTopX = SpecificBlurvideo.this.tempTop;
                SpecificBlurvideo.this.rightTopY = SpecificBlurvideo.this.tempRight;
                SpecificBlurvideo.this.leftBottomX = SpecificBlurvideo.this.tempBottom;
                SpecificBlurvideo.this.leftBottomY = SpecificBlurvideo.this.tempLeft;
                SpecificBlurvideo.this.rightBottomX = SpecificBlurvideo.this.tempBottom;
                SpecificBlurvideo.this.rightBottomY = SpecificBlurvideo.this.tempRight;
                w = SpecificBlurvideo.this.leftBottomX - SpecificBlurvideo.this.leftTopX;
                h = SpecificBlurvideo.this.rightTopY - leftTopy;
                SpecificBlurvideo.this.leftTopY = SpecificBlurvideo.this.oVHeight - (h + leftTopy);
            } else if (SpecificBlurvideo.this.rotatiobDegree == 270) {
                int leftTopx = SpecificBlurvideo.this.tempTop;
                leftTopy = SpecificBlurvideo.this.tempLeft;
                SpecificBlurvideo.this.rightTopX = SpecificBlurvideo.this.tempTop;
                SpecificBlurvideo.this.rightTopY = SpecificBlurvideo.this.tempRight;
                SpecificBlurvideo.this.leftBottomX = SpecificBlurvideo.this.tempBottom;
                SpecificBlurvideo.this.leftBottomY = SpecificBlurvideo.this.tempLeft;
                SpecificBlurvideo.this.rightBottomX = SpecificBlurvideo.this.tempBottom;
                SpecificBlurvideo.this.rightBottomY = SpecificBlurvideo.this.tempRight;
                w = SpecificBlurvideo.this.leftBottomX - leftTopx;
                h = SpecificBlurvideo.this.rightTopY - leftTopy;
                SpecificBlurvideo.this.leftTopX = SpecificBlurvideo.this.oVWidth - (w + leftTopx);
                SpecificBlurvideo.this.leftTopY = leftTopy;
            } else {
                SpecificBlurvideo.this.leftTopX = SpecificBlurvideo.this.tempLeft;
                SpecificBlurvideo.this.leftTopY = SpecificBlurvideo.this.tempTop;
                SpecificBlurvideo.this.rightTopX = SpecificBlurvideo.this.tempRight;
                SpecificBlurvideo.this.rightTopY = SpecificBlurvideo.this.tempTop;
                SpecificBlurvideo.this.leftBottomX = SpecificBlurvideo.this.tempLeft;
                SpecificBlurvideo.this.leftBottomY = SpecificBlurvideo.this.tempBottom;
                SpecificBlurvideo.this.rightBottomX = SpecificBlurvideo.this.tempRight;
                SpecificBlurvideo.this.rightBottomY = SpecificBlurvideo.this.tempBottom;
                w = SpecificBlurvideo.this.rightTopX - SpecificBlurvideo.this.leftTopX;
                h = SpecificBlurvideo.this.leftBottomY - SpecificBlurvideo.this.leftTopY;
            }
            String str = String.valueOf(SpecificBlurvideo.this.videoPlayerState.getStart() / TimeUtils.MilliSeconds.ONE_SECOND);
            String st = String.valueOf(SpecificBlurvideo.this.videoPlayerState.getDuration() / TimeUtils.MilliSeconds.ONE_SECOND);
            this.inputFileName = SpecificBlurvideo.this.inputvideo;
    /*        if (this.inputFileName.contains(".3gp") || this.inputFileName.contains(".3GP")) {
                outPutPath = com.example.enix.videoeditor.FileUtils.getTargetFileName(this.inputFileName.replace(".3gp", ".mp4"));
            } else if (this.inputFileName.contains(".flv") || this.inputFileName.contains(".FLv")) {
                outPutPath = com.example.enix.videoeditor.FileUtils.getTargetFileName(this.inputFileName.replace(".flv", ".mp4"));
            } else if (this.inputFileName.contains(".mov") || this.inputFileName.contains(".MOV")) {
                outPutPath = com.example.enix.videoeditor.FileUtils.getTargetFileName(this.inputFileName.replace(".mov", ".mp4"));
            } else if (this.inputFileName.contains(".wmv") || this.inputFileName.contains(".WMV")) {
                outPutPath = com.example.enix.videoeditor.FileUtils.getTargetFileName(this.inputFileName.replace(".wmv", ".mp4"));
            } else {
                outPutPath = com.example.enix.videoeditor.FileUtils.getTargetFileName(this.inputFileName);
            }
            SpecificBlurvideo.this.outputfile = com.example.enix.videoeditor.FileUtils.getTargetFileName(outPutPath);*/
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");

            String filePrefix = "BlurArea";
            String fileExtn = ".mp4";
            File dest = new File(mediaStorageDir, filePrefix + fileExtn);
            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
            }

            filePath = dest.getAbsolutePath();



            try {
                SpecificBlurvideo.this.make(str, st, inputvideo, SpecificBlurvideo.this.filePath, w, h, SpecificBlurvideo.this.leftTopX, SpecificBlurvideo.this.leftTopY);
            } catch (Exception e) {
                File appmusic = new File(SpecificBlurvideo.this.inputvideo);
                if (appmusic.exists()) {
                    appmusic.delete();
                    SpecificBlurvideo.this.finish();
                }
            }
            return null;
        }

        protected void onProgressUpdate(Void... progress) {
        }

        protected void onPostExecute(Void result) {

        }
    }

    /*private class StateObserver extends Handler {
        private boolean alreadyStarted;
        private Runnable observerWork;

        private StateObserver() {
            this.alreadyStarted = false;
            this.observerWork = new Runnable() {
                public void run() {
                    StateObserver.this.startVideoProgressObserving();
                }
            };
        }

        private void startVideoProgressObserving() {
            if (!this.alreadyStarted) {
                this.alreadyStarted = true;
                sendEmptyMessage(0);
            }
        }

        public void handleMessage(Message msg) {
            this.alreadyStarted = false;
            SpecificBlurvideo.this.videoSliceSeekBar.videoPlayingProgress(SpecificBlurvideo.this.videoView.getCurrentPosition());
            if (!SpecificBlurvideo.this.videoView.isPlaying() || SpecificBlurvideo.this.videoView.getCurrentPosition() >= SpecificBlurvideo.this.videoSliceSeekBar.getRightProgress()) {
                if (SpecificBlurvideo.this.videoView.isPlaying()) {
                    SpecificBlurvideo.this.videoView.pause();
                    SpecificBlurvideo.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                    SpecificBlurvideo.this.ivScreen.setVisibility(View.GONE);
                }
                SpecificBlurvideo.this.videoSliceSeekBar.setSliceBlocked(false);
                SpecificBlurvideo.this.videoSliceSeekBar.removeVideoStatusThumb();
                return;
            }
            postDelayed(this.observerWork, 50);
        }
    }*/

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_videocropper);

        loadFFMpegBinary();



       /* uploadVideo();*/


       /* this.b = getIntent().getExtras();
        if (this.b != null) {
            this.videopath = this.b.getString("song");
        }*/
        this.videoView = (VideoView) findViewById(videoview);
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        this.btn_back.setOnClickListener(this.OnClickBack);
        this.create_done = (ImageButton) findViewById(R.id.create_done);
        this.create_done.setOnClickListener(this.OnClickDone);
        RelativeLayout rlcontainer = (RelativeLayout) findViewById(R.id.rl_container);
        LinearLayout.LayoutParams lparams = (LinearLayout.LayoutParams) rlcontainer.getLayoutParams();
        this.screenWidth = Utils.getScreenWidth();
        lparams.width = this.screenWidth;
        lparams.height = this.screenWidth;
        rlcontainer.setLayoutParams(lparams);
        this.cropView = (CropImageView) findViewById(R.id.cropperView);
        this.textViewLeft = (TextView) findViewById(R.id.left_pointer);
        this.textViewRight = (TextView) findViewById(R.id.right_pointer);
        this.videoSliceSeekBar = (VideoSliceSeekBar) findViewById(R.id.seekBar1);
        this.ivScreen = (ImageView) findViewById(R.id.ivScreen);
        this.btnPlayVideo = (ImageButton) findViewById(R.id.btnPlayVideo);

        this.seekVideo = (SeekBar) findViewById(R.id.sbVideo);
        this.seekVideo.setOnSeekBarChangeListener(this);
        seekVideo.setVisibility(View.VISIBLE);

        videoSliceSeekBar.setVisibility(View.GONE);
        this.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (SpecificBlurvideo.this.videoView == null || !SpecificBlurvideo.this.videoView.isPlaying()) {
                    SpecificBlurvideo.this.btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);

                    handler.postDelayed(seekrunnable, 200);
                    videoView.start();
                    SpecificBlurvideo.this.ivScreen.setVisibility(View.GONE);
                } else {
                    videoView.pause();
                    SpecificBlurvideo.this.ivScreen.setVisibility(View.VISIBLE);
                    handler.removeCallbacks(seekrunnable);
                    SpecificBlurvideo.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                }
               /* SpecificBlurvideo.this.performVideoViewClick();*/
            }
        });

        this.imbtn_custom = (ImageButton) findViewById(R.id.imbtn_android);
        this.imgbtn_square = (ImageButton) findViewById(R.id.imgbtn_square);
        this.imgbtn_port = (ImageButton) findViewById(R.id.imgbtn_port);
        this.imgbtn_cland = (ImageButton) findViewById(R.id.imgbtn_cland);
        this.imgbtn_three = (ImageButton) findViewById(R.id.imgbtn_three);
        this.imbtn_custom.setOnClickListener(this.Custom);
        this.imgbtn_square.setOnClickListener(this.Square);
        this.imgbtn_port.setOnClickListener(this.Potrait);
        this.imgbtn_cland.setOnClickListener(this.Landscape);
        this.imgbtn_three.setOnClickListener(this.three);

        toolbar_title=(TextView)findViewById(R.id.toolbar_title);

        toolbar_title.setText("Specific Blur");
        ChangerIcon(1);

        inputvideo = getIntent().getStringExtra("tempfile");
        //Log.e("inoutvideo spec",inputvideo);

        if (getIntent().hasExtra("tempfile")){


            initVideoView(inputvideo);
        }


        this.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {


                videoView.pause();
                btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                btnPlayVideo.setVisibility(View.VISIBLE);
                videoView.seekTo(0);
                seekVideo.setProgress(0);
                textViewLeft.setText("00:00");
                handler.removeCallbacks(seekrunnable);


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
        new AlertDialog.Builder(SpecificBlurvideo.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SpecificBlurvideo.this.finish();
                    }
                })
                .create()
                .show();

    }

    private void uploadVideo() {

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
    }

  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();

                inputvideo = getRealPathFromURI(selectedVideoUri);

                initVideoView();


            }

        }
    }*/

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

   /* private void performVideoViewClick() {
        if (this.videoView.isPlaying()) {
            this.videoView.pause();
            this.videoSliceSeekBar.setSliceBlocked(false);
            this.videoSliceSeekBar.removeVideoStatusThumb();
            return;
        }
        this.videoView.seekTo(this.videoSliceSeekBar.getLeftProgress());
        this.videoView.start();
        this.videoSliceSeekBar.videoPlayingProgress(this.videoSliceSeekBar.getLeftProgress());
        this.videoStateObserver.startVideoProgressObserving();
    }*/

    private void initVideoView(String inputvideofrom) {
        this.videoView.setVideoPath(inputvideofrom);
        this.endTime = getTimeForTrackFormat(this.videoView.getDuration(), true);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(inputvideofrom);
        this.oVWidth = Integer.valueOf(retriever.extractMetadata(18)).intValue();
        this.oVHeight = Integer.valueOf(retriever.extractMetadata(19)).intValue();
        if (VERSION.SDK_INT > 16) {
            this.rotatiobDegree = Integer.valueOf(retriever.extractMetadata(24)).intValue();
        } else {
            this.rotatiobDegree = 0;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.cropView.getLayoutParams();
        if (this.rotatiobDegree == 90 || this.rotatiobDegree == 270) {
            if (this.oVWidth >= this.oVHeight) {
                if (this.oVWidth >= this.screenWidth) {
                    lp.height = this.screenWidth;
                    lp.width = (int) (((float) this.screenWidth) / (((float) this.oVWidth) / ((float) this.oVHeight)));
                } else {
                    lp.width = this.screenWidth;
                    lp.height = (int) (((float) this.oVHeight) * (((float) this.screenWidth) / ((float) this.oVWidth)));
                }
            } else if (this.oVHeight >= this.screenWidth) {
                lp.width = this.screenWidth;
                lp.height = (int) (((float) this.screenWidth) / (((float) this.oVHeight) / ((float) this.oVWidth)));
            } else {
                lp.width = (int) (((float) this.oVWidth) * (((float) this.screenWidth) / ((float) this.oVHeight)));
                lp.height = this.screenWidth;
            }
        } else if (this.oVWidth >= this.oVHeight) {
            if (this.oVWidth >= this.screenWidth) {
                lp.width = this.screenWidth;
                lp.height = (int) (((float) this.screenWidth) / (((float) this.oVWidth) / ((float) this.oVHeight)));
            } else {
                lp.width = this.screenWidth;
                lp.height = (int) (((float) this.oVHeight) * (((float) this.screenWidth) / ((float) this.oVWidth)));
            }
        } else if (this.oVHeight >= this.screenWidth) {
            lp.width = (int) (((float) this.screenWidth) / (((float) this.oVHeight) / ((float) this.oVWidth)));
            lp.height = this.screenWidth;
        } else {
            lp.width = (int) (((float) this.oVWidth) * (((float) this.screenWidth) / ((float) this.oVHeight)));
            lp.height = this.screenWidth;
        }
        //Log.e("", "== width ==" + lp.width);
        //Log.e("", "== height ==" + lp.height);
        this.cropView.setLayoutParams(lp);
        this.cropView.setImageBitmap(Bitmap.createBitmap(lp.width, lp.height, Config.ARGB_8888));
        try {
            SearchVideo(getApplicationContext(), inputvideofrom, lp.width, lp.height);
        } catch (Exception e) {
        }
        this.videoView.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {


               duration =videoView.getDuration();
               seekVideo.setMax(duration);
               textViewLeft.setText("00:00");
                try {
                   textViewRight.setText(VideoPlayer.formatTimeUnit((long)duration));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
               /* SpecificBlurvideo.this.videoSliceSeekBar.setSeekBarChangeListener(new VideoSliceSeekBar.SeekBarChangeListener() {
                    public void SeekBarValueChanged(int leftThumb, int rightThumb) {
                        if (SpecificBlurvideo.this.videoSliceSeekBar.getSelectedThumb() == 1) {
                            SpecificBlurvideo.this.videoView.seekTo(SpecificBlurvideo.this.videoSliceSeekBar.getLeftProgress());
                        }
                        SpecificBlurvideo.this.textViewLeft.setText(SpecificBlurvideo.getTimeForTrackFormat(leftThumb, true));
                        SpecificBlurvideo.this.textViewRight.setText(SpecificBlurvideo.getTimeForTrackFormat(rightThumb, true));
                        SpecificBlurvideo.this.startTime = SpecificBlurvideo.getTimeForTrackFormat(leftThumb, true);
                        SpecificBlurvideo.this.videoPlayerState.setStart(leftThumb);
                        SpecificBlurvideo.this.endTime = SpecificBlurvideo.getTimeForTrackFormat(rightThumb, true);
                        SpecificBlurvideo.this.videoPlayerState.setStop(rightThumb);
                    }
                });
                SpecificBlurvideo.this.endTime = SpecificBlurvideo.getTimeForTrackFormat(mp.getDuration(), true);
                SpecificBlurvideo.this.videoSliceSeekBar.setMaxValue(mp.getDuration());
                SpecificBlurvideo.this.videoSliceSeekBar.setLeftProgress(0);
                SpecificBlurvideo.this.videoSliceSeekBar.setRightProgress(mp.getDuration());
                SpecificBlurvideo.this.videoSliceSeekBar.setProgressMinDiff(0);*/
            }
        });
    }

    public static String getTimeForTrackFormat(int timeInMills, boolean display2DigitsInMinsSection) {
        int minutes = timeInMills / TimeUtils.MilliSeconds.ONE_MINUTE;
        int seconds = (timeInMills - ((minutes * 60) * TimeUtils.MilliSeconds.ONE_SECOND)) / TimeUtils.MilliSeconds.ONE_SECOND;
        String result = (!display2DigitsInMinsSection || minutes >= 10) ? "" : "0";
        result = new StringBuilder(String.valueOf(result)).append(minutes % 60).append(":").toString();
        if (seconds < 10) {
            result = new StringBuilder(String.valueOf(result)).append("0").append(seconds).toString();
        } else {
            result = new StringBuilder(String.valueOf(result)).append(seconds).toString();
        }
        //Log.e("", "Display Result" + result);
        return result;
    }

    public void SearchVideo(Context c, String songPath, int lwidth, int lheight) {
        Cursor cursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"_data", "_id"}, "_data  like ?", new String[]{"%" + songPath + "%"}, " _id DESC");
        int count = cursor.getCount();
        //Log.e("", "count" + count);
        if (count > 0) {
            cursor.moveToFirst();
            Bitmap b = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("_id"))).longValue(), 1, null);
            ViewGroup.LayoutParams ll = this.ivScreen.getLayoutParams();
            ll.width = lwidth;
            ll.height = lheight;
            this.ivScreen.setLayoutParams(ll);
            this.ivScreen.setImageBitmap(b);
            cursor.moveToNext();
        }
    }

    public void ChangerIcon(int i) {
        if (i == 1) {
            this.imbtn_custom.setBackgroundResource(R.drawable.custom_presed);
            this.imgbtn_square.setBackgroundResource(R.drawable.square_unpresed);
            this.imgbtn_port.setBackgroundResource(R.drawable.portrait_unpresed);
            this.imgbtn_cland.setBackgroundResource(R.drawable.landscape_unpresed);
            this.imgbtn_three.setBackgroundResource(R.drawable.three_unpresed);
        } else if (i == 2) {
            this.imbtn_custom.setBackgroundResource(R.drawable.custom_unpresed);
            this.imgbtn_square.setBackgroundResource(R.drawable.square_presed);
            this.imgbtn_port.setBackgroundResource(R.drawable.portrait_unpresed);
            this.imgbtn_cland.setBackgroundResource(R.drawable.landscape_unpresed);
            this.imgbtn_three.setBackgroundResource(R.drawable.three_unpresed);
        } else if (i == 3) {
            this.imbtn_custom.setBackgroundResource(R.drawable.custom_unpresed);
            this.imgbtn_square.setBackgroundResource(R.drawable.square_unpresed);
            this.imgbtn_port.setBackgroundResource(R.drawable.portrait_presed);
            this.imgbtn_cland.setBackgroundResource(R.drawable.landscape_unpresed);
            this.imgbtn_three.setBackgroundResource(R.drawable.three_unpresed);
        } else if (i == 4) {
            this.imbtn_custom.setBackgroundResource(R.drawable.custom_unpresed);
            this.imgbtn_square.setBackgroundResource(R.drawable.square_unpresed);
            this.imgbtn_port.setBackgroundResource(R.drawable.portrait_unpresed);
            this.imgbtn_cland.setBackgroundResource(R.drawable.landscape_presed);
            this.imgbtn_three.setBackgroundResource(R.drawable.three_unpresed);
        } else if (i == 5) {
            this.imbtn_custom.setBackgroundResource(R.drawable.custom_unpresed);
            this.imgbtn_square.setBackgroundResource(R.drawable.square_unpresed);
            this.imgbtn_port.setBackgroundResource(R.drawable.portrait_unpresed);
            this.imgbtn_cland.setBackgroundResource(R.drawable.landscape_unpresed);
            this.imgbtn_three.setBackgroundResource(R.drawable.three_presed);
        }
    }

    private void setRealPosition() {
        float videowidth;
        float videoheight;
        float videoviewwidth;
        float videoviewheight;
        if (this.rotatiobDegree == 90 || this.rotatiobDegree == 270) {
            videowidth = (float) this.oVHeight;
            videoheight = (float) this.oVWidth;
            videoviewwidth = (float) this.cropView.getWidth();
            videoviewheight = (float) this.cropView.getHeight();
            this.tempLeft = (int) ((Edge.LEFT.getCoordinate() * videowidth) / videoviewwidth);
            this.tempRight = (int) ((Edge.RIGHT.getCoordinate() * videowidth) / videoviewwidth);
            this.tempTop = (int) ((Edge.TOP.getCoordinate() * videoheight) / videoviewheight);
            this.tempBottom = (int) ((Edge.BOTTOM.getCoordinate() * videoheight) / videoviewheight);
            return;
        }
        videowidth = (float) this.oVWidth;
        videoheight = (float) this.oVHeight;
        videoviewwidth = (float) this.cropView.getWidth();
        videoviewheight = (float) this.cropView.getHeight();
        this.tempLeft = (int) ((Edge.LEFT.getCoordinate() * videowidth) / videoviewwidth);
        this.tempRight = (int) ((Edge.RIGHT.getCoordinate() * videowidth) / videoviewwidth);
        this.tempTop = (int) ((Edge.TOP.getCoordinate() * videoheight) / videoviewheight);
        this.tempBottom = (int) ((Edge.BOTTOM.getCoordinate() * videoheight) / videoviewheight);
    }

    void make(String start, String duretion, String input, String output, int w, int h, int x, int y) {
        String[] cmd = {"-i", input, "-filter_complex","[0:v]crop="+w+":"+ h +":"+x+":"+y+",boxblur=10[fg];[0:v][fg]overlay=60:30[v]","-map", "[v]", "-map", "0:a", "-c:v", "libx264", "-c:a", "copy", "-movflags", "+faststart",filePath};
        execFFmpegBinary(cmd);

    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    //Log.e("FFmpeg", "concat onSuccess():");

                    SpecificBlurvideo.this.progress.dismiss();
            /*this.outputformat = SpecificBlurvideo.this.filePath.substring(SpecificBlurvideo.this.filePath.lastIndexOf(".") + 1);
            File f1 = new File(SpecificBlurvideo.this.filePath);
            MediaScannerConnection.scanFile(SpecificBlurvideo.this, new String[]{f1.getAbsolutePath()}, new String[]{this.outputformat}, null);
            SpecificBlurvideo.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(f1)));*/
                    Intent intent = new Intent(SpecificBlurvideo.this, Editplayer.class);
                    intent.putExtra("tempfile", filePath);
                    SpecificBlurvideo.this.startActivity(intent);

                }

                @Override
                public void onProgress(String message) {


                        progress.setMessage("Processing.....");

                }

                @Override
                public void onFailure(String message) {

                    progress.dismiss();
                    //Log.e("FFmpeg", "concat onFailure():" + message);
                }

                @Override
                public void onStart() {

                    //Log.e("FFmpeg", "concat onStart():");
                    /*progressDialog.show();
                    progressDialog.setMessage("Processing...");*/
                }

                @Override
                public void onFinish() {
                    //Log.e("FFmpeg", "concat onFinish():");
                    progress.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {

            Log.d("ex with output : ", "" + e);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        videoView.pause();

        btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
        time = videoView.getCurrentPosition();
        videoView.seekTo(time);
        seekVideo.setProgress(time);
        super.onPause();

    }

    @Override
    protected void onResume() {

        try {
            textViewLeft.setText(VideoPlayer.formatTimeUnit((long)  time));
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

