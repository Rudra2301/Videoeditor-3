package com.example.enix.videoeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Media;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
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

import static com.example.enix.videoeditor.Videoeffect.REQUEST_TAKE_GALLERY_VIDEO;


public class CropVideo extends AppCompatActivity {
    OnClickListener Custom = new OnClickListener() {
        public void onClick(View v) {
            CropVideo.this.ChangerIcon(1);
            CropVideo.this.cropView.setFixedAspectRatio(false);
        }
    };
    OnClickListener Landscape = new OnClickListener() {
        public void onClick(View v) {
            CropVideo.this.ChangerIcon(4);
            CropVideo.this.cropView.setFixedAspectRatio(true);
            CropVideo.this.cropView.setAspectRatio(16, 8);
        }
    };
    OnClickListener OnClickBack = new OnClickListener() {
        public void onClick(View v) {
            CropVideo.this.finish();
        }
    };
    OnClickListener OnClickDone = new OnClickListener() {
        public void onClick(View v) {
            //Log.e("", "== done ==");
            if (CropVideo.this.videoView != null && CropVideo.this.videoView.isPlaying()) {
                CropVideo.this.videoView.pause();
            }
            CropVideo.this.setRealPosition();
            new CompressTask().execute();
        }
    };
    OnClickListener Potrait = new OnClickListener() {
        public void onClick(View v) {
            CropVideo.this.ChangerIcon(3);
            CropVideo.this.cropView.setFixedAspectRatio(true);
            CropVideo.this.cropView.setAspectRatio(8, 16);
        }
    };
    OnClickListener Square = new OnClickListener() {
        public void onClick(View v) {
            CropVideo.this.ChangerIcon(2);
            CropVideo.this.cropView.setFixedAspectRatio(true);
            CropVideo.this.cropView.setAspectRatio(10, 10);
        }
    };
    Bundle b;
    int duration;
    ImageButton btnPlayVideo;
    ImageButton btn_back;
    ImageButton create_done;
    CropImageView cropView;
    String endTime;
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
    String outputformat;
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
    private FFmpeg ffmpeg;
    int time;
    private TextView textViewLeft;
    private TextView textViewRight;
    OnClickListener three = new OnClickListener() {
        public void onClick(View v) {
            CropVideo.this.ChangerIcon(5);
            CropVideo.this.cropView.setFixedAspectRatio(true);
            CropVideo.this.cropView.setAspectRatio(3, 2);
        }
    };
    private VideoPlayerState videoPlayerState = new VideoPlayerState();
    VideoSliceSeekBar videoSliceSeekBar;
    private StateObserver videoStateObserver = new StateObserver();
    VideoView videoView;
    String videopath;
    public static final String mypreference = "videoeditor";
    SharedPreferences sharedpreferences;

    @SuppressLint({"NewApi"})
    private class CompressTask extends AsyncTask<Void, Void, Void> {
        String inputFileName;
        private String outputformat;

        public CompressTask() {

            CropVideo.this.progress = new ProgressDialog(CropVideo.this);
            CropVideo.this.progress.setMessage("Please Wait");
            CropVideo.this.progress.setCancelable(false);

            videoView.pause();

            CropVideo.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
            time = videoView.getCurrentPosition();
            videoView.seekTo(time);
            videoSliceSeekBar.videoPlayingProgress(time);
            CropVideo.this.progress.show();

        }


        protected Void doInBackground(Void... urls) {
            int w;
            int h;
            String outPutPath;
            int leftTopy;
            if (CropVideo.this.rotatiobDegree == 90) {
                CropVideo.this.leftTopX = CropVideo.this.tempTop;
                leftTopy = CropVideo.this.tempLeft;
                CropVideo.this.rightTopX = CropVideo.this.tempTop;
                CropVideo.this.rightTopY = CropVideo.this.tempRight;
                CropVideo.this.leftBottomX = CropVideo.this.tempBottom;
                CropVideo.this.leftBottomY = CropVideo.this.tempLeft;
                CropVideo.this.rightBottomX = CropVideo.this.tempBottom;
                CropVideo.this.rightBottomY = CropVideo.this.tempRight;
                w = CropVideo.this.leftBottomX - CropVideo.this.leftTopX;
                h = CropVideo.this.rightTopY - leftTopy;
                CropVideo.this.leftTopY = CropVideo.this.oVHeight - (h + leftTopy);
            } else if (CropVideo.this.rotatiobDegree == 270) {
                int leftTopx = CropVideo.this.tempTop;
                leftTopy = CropVideo.this.tempLeft;
                CropVideo.this.rightTopX = CropVideo.this.tempTop;
                CropVideo.this.rightTopY = CropVideo.this.tempRight;
                CropVideo.this.leftBottomX = CropVideo.this.tempBottom;
                CropVideo.this.leftBottomY = CropVideo.this.tempLeft;
                CropVideo.this.rightBottomX = CropVideo.this.tempBottom;
                CropVideo.this.rightBottomY = CropVideo.this.tempRight;
                w = CropVideo.this.leftBottomX - leftTopx;
                h = CropVideo.this.rightTopY - leftTopy;
                CropVideo.this.leftTopX = CropVideo.this.oVWidth - (w + leftTopx);
                CropVideo.this.leftTopY = leftTopy;
            } else {
                CropVideo.this.leftTopX = CropVideo.this.tempLeft;
                CropVideo.this.leftTopY = CropVideo.this.tempTop;
                CropVideo.this.rightTopX = CropVideo.this.tempRight;
                CropVideo.this.rightTopY = CropVideo.this.tempTop;
                CropVideo.this.leftBottomX = CropVideo.this.tempLeft;
                CropVideo.this.leftBottomY = CropVideo.this.tempBottom;
                CropVideo.this.rightBottomX = CropVideo.this.tempRight;
                CropVideo.this.rightBottomY = CropVideo.this.tempBottom;
                w = CropVideo.this.rightTopX - CropVideo.this.leftTopX;
                h = CropVideo.this.leftBottomY - CropVideo.this.leftTopY;
            }
            String str = String.valueOf(CropVideo.this.videoPlayerState.getStart() / TimeUtils.MilliSeconds.ONE_SECOND);
            String st = String.valueOf(CropVideo.this.videoPlayerState.getDuration() / TimeUtils.MilliSeconds.ONE_SECOND);
            this.inputFileName = CropVideo.this.inputvideo;
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
            CropVideo.this.outputfile = com.example.enix.videoeditor.FileUtils.getTargetFileName(outPutPath);*/


            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name)+"-Temp");



            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {

                }
            }

            String filePrefix = "Crop";
            String fileExtn = ".mp4";
            File dest = new File(mediaStorageDir, filePrefix + fileExtn);
            int fileNo = 0;
            while (dest.exists()) {
                fileNo++;
                dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
            }

            filePath = dest.getAbsolutePath();


            try {
                CropVideo.this.make(str, st, inputvideo, CropVideo.this.filePath, w, h, CropVideo.this.leftTopX, CropVideo.this.leftTopY);
            } catch (Exception e) {
                File appmusic = new File(CropVideo.this.inputvideo);
                if (appmusic.exists()) {
                    appmusic.delete();
                    CropVideo.this.finish();
                }
            }
            return null;
        }

        protected void onProgressUpdate(Void... progress) {
        }

        protected void onPostExecute(Void result) {

        }
    }

    private class StateObserver extends Handler {
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
            CropVideo.this.videoSliceSeekBar.videoPlayingProgress(CropVideo.this.videoView.getCurrentPosition());
            if (!CropVideo.this.videoView.isPlaying() || CropVideo.this.videoView.getCurrentPosition() >= CropVideo.this.videoSliceSeekBar.getRightProgress()) {
                if (CropVideo.this.videoView.isPlaying()) {
                    CropVideo.this.videoView.pause();
                    CropVideo.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                    CropVideo.this.ivScreen.setVisibility(View.GONE);
                }
                CropVideo.this.videoSliceSeekBar.setSliceBlocked(false);
                CropVideo.this.videoSliceSeekBar.removeVideoStatusThumb();
                return;
            }
            postDelayed(this.observerWork, 50);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.actvity_videocropper);

        loadFFMpegBinary();
        uploadVideo();


       /* this.b = getIntent().getExtras();
        if (this.b != null) {
            this.videopath = this.b.getString("song");
        }*/
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        this.videoView = (VideoView) findViewById(R.id.videoview);
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        this.btn_back.setOnClickListener(this.OnClickBack);
        this.create_done = (ImageButton) findViewById(R.id.create_done);
        this.create_done.setOnClickListener(this.OnClickDone);
        RelativeLayout rlcontainer = (RelativeLayout) findViewById(R.id.rl_container);
        LayoutParams lparams = (LayoutParams) rlcontainer.getLayoutParams();
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
        this.btnPlayVideo.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (CropVideo.this.videoView == null || !CropVideo.this.videoView.isPlaying()) {
                    CropVideo.this.btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);
                    CropVideo.this.ivScreen.setVisibility(View.GONE);
                } else {
                    CropVideo.this.ivScreen.setVisibility(View.VISIBLE);
                    CropVideo.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                }
                CropVideo.this.performVideoViewClick();
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
        ChangerIcon(1);

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
        new AlertDialog.Builder(CropVideo.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CropVideo.this.finish();
                    }
                })
                .create()
                .show();

    }

    private void uploadVideo() {

        /*Intent intent = new Intent();
        intent.setType("video*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);*/

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();

                inputvideo = getRealPathFromURI(selectedVideoUri);




                initVideoView();


            }

        }else{

            Intent intent = new Intent(CropVideo.this,Pick_video.class);
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

    private void performVideoViewClick() {
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
    }

    private void initVideoView() {
        this.videoView.setVideoPath(this.inputvideo);
        this.endTime = getTimeForTrackFormat(this.videoView.getDuration(), true);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this.inputvideo);
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
            SearchVideo(getApplicationContext(), this.inputvideo, lp.width, lp.height);
        } catch (Exception e) {
        }
        this.videoView.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {

                duration=mp.getDuration();

                CropVideo.this.videoSliceSeekBar.setSeekBarChangeListener(new VideoSliceSeekBar.SeekBarChangeListener() {
                    public void SeekBarValueChanged(int leftThumb, int rightThumb) {
                        if (CropVideo.this.videoSliceSeekBar.getSelectedThumb() == 1) {
                            CropVideo.this.videoView.seekTo(CropVideo.this.videoSliceSeekBar.getLeftProgress());
                        }
                        CropVideo.this.textViewLeft.setText(CropVideo.getTimeForTrackFormat(leftThumb, true));
                        CropVideo.this.textViewRight.setText(CropVideo.getTimeForTrackFormat(rightThumb, true));
                        CropVideo.this.startTime = CropVideo.getTimeForTrackFormat(leftThumb, true);
                        CropVideo.this.videoPlayerState.setStart(leftThumb);
                        CropVideo.this.endTime = CropVideo.getTimeForTrackFormat(rightThumb, true);
                        CropVideo.this.videoPlayerState.setStop(rightThumb);
                    }
                });
                CropVideo.this.endTime = CropVideo.getTimeForTrackFormat(mp.getDuration(), true);
                CropVideo.this.videoSliceSeekBar.setMaxValue(mp.getDuration());
                CropVideo.this.videoSliceSeekBar.setLeftProgress(0);
                CropVideo.this.videoSliceSeekBar.setRightProgress(mp.getDuration());
                CropVideo.this.videoSliceSeekBar.setProgressMinDiff(0);
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
        Cursor cursor = managedQuery(Media.EXTERNAL_CONTENT_URI, new String[]{"_data", "_id"}, "_data  like ?", new String[]{"%" + songPath + "%"}, " _id DESC");
        int count = cursor.getCount();
        //Log.e("", "count" + count);
        if (count > 0) {
            cursor.moveToFirst();
            Bitmap b = Thumbnails.getThumbnail(getContentResolver(), Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("_id"))).longValue(), 1, null);
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
        String[] cmd = {"-ss", start, "-t", duretion, "-i", input, "-strict", "experimental", "-vf", "crop=w=" + w + ":h=" + h + ":x=" + x + ":y=" + y, "-r", "15", "-ab", "128k", "-vcodec", "mpeg4", "-acodec", "copy", "-b:v", "2500k", "-sample_fmt", "s16", "-ss", "0", "-t", duretion, filePath};
        execFFmpegBinary(cmd);

    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                public String outputformat;

                @Override
                public void onSuccess(String message) {
                    //Log.e("FFmpeg", "concat onSuccess():");

                    CropVideo.this.progress.dismiss();
                    this.outputformat = CropVideo.this.filePath.substring(CropVideo.this.filePath.lastIndexOf(".") + 1);
                    File f1 = new File(CropVideo.this.filePath);
                    MediaScannerConnection.scanFile(CropVideo.this, new String[]{f1.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String s, Uri uri) {


                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("Mainvideo", s);

                            editor.apply();

                            //Log.e("Scanned ", "" + s);
                            //Log.e("Scanneduri", "" + uri.toString());
                            Intent intent = new Intent(CropVideo.this, Editplayer.class);
                            intent.putExtra("tempfile", s);


                            CropVideo.this.startActivity(intent);
                        }
                    });
                    /*CropVideo.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(f1)));*/


                }

                @Override
                public void onProgress(String message) {


                        progress.setMessage("Processing......");

                }

                @Override
                public void onFailure(String message) {
                    //Log.e("FFmpeg", "concat onFailure():" + message);
                    CropVideo.this.progress.dismiss();
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
                  /*  progressDialog.dismiss();*/
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {

            Log.d("ex with output : ", "" + e);

        }
    }

    @Override
    protected void onPause() {
        videoView.pause();

        CropVideo.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
        time = videoView.getCurrentPosition();
        videoView.seekTo(time);
        videoSliceSeekBar.videoPlayingProgress(time);
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
        videoSliceSeekBar.videoPlayingProgress(time);
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
