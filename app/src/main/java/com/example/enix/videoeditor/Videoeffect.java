package com.example.enix.videoeditor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.enix.videoeditor.Editplayer.formatTimeUnit;
import static com.example.enix.videoeditor.Videofilter.showSingleOptionTextDialog;

public class Videoeffect extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    public static int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private FFmpeg ffmpeg;
    private boolean IsEffectApplied = false;
    private ArrayList<String> cmd;
    private VideoView videoView;
    private RangeSeekBar rangeSeekBar;
    private ProgressDialog progressDialog;
    private String mFfmpegInstallPath;
    private Uri selectedVideoUri;
    private TextView tvLeft, tvRight;
    private Runnable r;
    RecyclerView option;
    private String filePath;
    int overlayvalue;

    String tempoutPutFolder = (Environment.getExternalStorageDirectory() + "/TempVideoEffects/");
    private Effect_adapter option_adapter;
    String shareImageFileName;
    private static final String FILEPATH = "filepath";
    String inputvideo;
    String yourRealPath;
    MediaController mediaController;
    private String tempoutpueffects = null;
    int videoend;
    Drawable overLaydrawable;
    int videowidth;
    /* String outPutFolder = (Environment.getExternalStorageDirectory() + "/VideoEffects/");*/
    int videoheight;
    FrameLayout overlayframeLayout;
    String outPutFolder = (Environment.getExternalStorageDirectory() + "/VideoEffects/");
    ImageButton btnPlayVideo;
    ImageButton btn_back;
    ImageButton create_done;
    SeekBar seekVideo;
    TextView tvEndVideo;
    TextView tvStartVideo;
    int duration;
    Handler handler = new Handler();
    RelativeLayout relativeLayout;
    int time;
    TextView toolbar_title;
    SeekBar volumeseek;
    int selectitem = 0;

    Runnable seekrunnable = new Runnable() {
        public void run() {
            if (Videoeffect.this.videoView.isPlaying()) {
                int curPos = Videoeffect.this.videoView.getCurrentPosition();
                Videoeffect.this.seekVideo.setProgress(curPos);
                try {
                    Videoeffect.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) curPos));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (curPos == Videoeffect.this.duration) {
                    Videoeffect.this.seekVideo.setProgress(0);
                    Videoeffect.this.tvStartVideo.setText("00:00");
                    Videoeffect.this.handler.removeCallbacks(Videoeffect.this.seekrunnable);
                    return;
                }
                Videoeffect.this.handler.postDelayed(Videoeffect.this.seekrunnable, 200);
                return;
            }
            Videoeffect.this.seekVideo.setProgress(Videoeffect.this.duration);
            try {
                Videoeffect.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) Videoeffect.this.duration));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
            Videoeffect.this.handler.removeCallbacks(Videoeffect.this.seekrunnable);
        }
    };

    String[] effectName = new String[]{"o1.png", "o2.png", "o3.png", "o4.png", "o5.png", "o6.png", "o7.png", "o8.png", "o9.png", "o10.png", "o11.png", "o12.png", "o13.png", "o14.png", "o15.png", "o16.png", "o17.png", "o18.png", "o19.png", "o20.png", "o21.png", "o22.png", "o23.png", "o24.png", "o25.png", "o26.png", "o27.png", "o28.png", "o29.png", "o30.png", "o31.png", "o32.png", "o33.png", "o34.png", "o35.png", "o36.png", "o37.png", "o38.png", "o39.png", "o40.png", "o41.png", "o42.png", "o43.png", "o44.png", "o45.png", "o46.png", "o47.png", "o48.png", "o49.png", "o5.png", "o51.png"};
    int[] effects = new int[]{R.drawable.o1, R.drawable.o2, R.drawable.o3, R.drawable.o4, R.drawable.o5, R.drawable.o6, R.drawable.o7, R.drawable.o8, R.drawable.o9, R.drawable.o10, R.drawable.o11, R.drawable.o12, R.drawable.o13, R.drawable.o14, R.drawable.o15, R.drawable.o16, R.drawable.o17, R.drawable.o18, R.drawable.o19, R.drawable.o20, R.drawable.o21, R.drawable.o22, R.drawable.o23, R.drawable.o24, R.drawable.o25, R.drawable.o26, R.drawable.o27, R.drawable.o28, R.drawable.o29, R.drawable.o30, R.drawable.o31, R.drawable.o32, R.drawable.o33, R.drawable.o34, R.drawable.o35, R.drawable.o36, R.drawable.o37, R.drawable.o38, R.drawable.o39, R.drawable.o40, R.drawable.o41, R.drawable.o42, R.drawable.o43, R.drawable.o44, R.drawable.o45, R.drawable.o46, R.drawable.o47, R.drawable.o48, R.drawable.o49, R.drawable.o50, R.drawable.o51};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_videoeffect);

        inputvideo = getIntent().getStringExtra("tempfile");

        loadFFMpegBinary();
        option = (RecyclerView) findViewById(R.id.option_effect);
        overlayframeLayout = (FrameLayout) findViewById(R.id.overlaylayout);
        videoView = (VideoView) findViewById(R.id.videoView);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        option_adapter = new Effect_adapter(Videoeffect.this, effects, effectName);
        LinearLayoutManager layoutManager = new LinearLayoutManager(Videoeffect.this, LinearLayoutManager.HORIZONTAL, false);
        option.setHasFixedSize(true);
        option.setLayoutManager(layoutManager);
        option.setItemAnimator(new DefaultItemAnimator());
        option.setAdapter(option_adapter);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Video Overlay");

        volumeseek = (SeekBar) findViewById(R.id.volumeseek);

        this.volumeseek.setProgress(50);
        this.volumeseek.setMax(255);
        this.volumeseek.setPadding(10, 0, 10, 10);

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
                if (Videoeffect.this.videoView == null || !Videoeffect.this.videoView.isPlaying()) {
                    Videoeffect.this.videoView.seekTo(Videoeffect.this.seekVideo.getProgress());
                    Videoeffect.this.btnPlayVideo.setBackgroundResource(R.drawable.pause_btn);
                    Videoeffect.this.handler.postDelayed(Videoeffect.this.seekrunnable, 200);

                    videoView.start();

                } else {
                    videoView.pause();
                    Videoeffect.this.handler.removeCallbacks(Videoeffect.this.seekrunnable);
                    Videoeffect.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                }

            }
        });


        initvideoview();


        this.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {


                videoView.pause();
                Videoeffect.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
                Videoeffect.this.btnPlayVideo.setVisibility(View.VISIBLE);
                Videoeffect.this.videoView.seekTo(0);
                Videoeffect.this.seekVideo.setProgress(0);
                Videoeffect.this.tvStartVideo.setText("00:00");
                Videoeffect.this.handler.removeCallbacks(Videoeffect.this.seekrunnable);


            }
        });


        option.addOnItemTouchListener(new RecyclerItemClickListener(Videoeffect.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                selectitem = 1;
                volumeseek.setVisibility(View.VISIBLE);
                overLaydrawable = getResources().getDrawable(effects[position]);

                overLaydrawable.mutate().setAlpha(127);

                overlayframeLayout.setBackground(overLaydrawable);

                copyRawToSdcard();


            }
        }));

        this.volumeseek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {

                overLaydrawable.mutate().setAlpha(i);
                overlayvalue = i;

                overlayframeLayout.setBackground(overLaydrawable);


            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

                copyRawToSdcard();
            }
        });

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


                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(inputvideo);
                int width = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                int height = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
              /*  overlayframeLayout.setLayoutParams(new FrameLayout.LayoutParams(width,height));*/
                mediaMetadataRetriever.release();


                Videoeffect.this.duration = Videoeffect.this.videoView.getDuration();
                Videoeffect.this.seekVideo.setMax(Videoeffect.this.duration);
                Videoeffect.this.tvStartVideo.setText("00:00");
                try {
                    Videoeffect.this.tvEndVideo.setText(VideoPlayer.formatTimeUnit((long) Videoeffect.this.duration));
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

                    Intent intent = new Intent(Videoeffect.this, Editplayer.class);
                    intent.putExtra("tempfile", filePath);
                    startActivity(intent);

                    File file = new File(outPutFolder);
                    if (file.exists()) {
                        deleteFolder(file.getAbsolutePath());
                    }

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

                        progressDialog.setMessage("Processing....." + percentage);
                    }


                }

                @Override
                public void onFailure(String message) {
                    //Log.e("FFmpeg", "concat onFailure():" + message);
                }

                @Override
                public void onStart() {
                    videoView.pause();

                    Videoeffect.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
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


    private String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
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
        new AlertDialog.Builder(Videoeffect.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Videoeffect.this.finish();
                    }
                })
                .create()
                .show();

    }


   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();



            }
        }
    }*/

    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
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


    private String twoDigitString(int i) {
        return i == 0 ? "00" : i / 10 == 0 ? "0" + i : String.valueOf(i);
    }

    @Override
    protected void onPause() {
        videoView.pause();

        Videoeffect.this.btnPlayVideo.setBackgroundResource(R.drawable.play_btn);
        time = videoView.getCurrentPosition();
        videoView.seekTo(time);
        seekVideo.setProgress(time);
        super.onPause();

    }

    @Override
    protected void onResume() {

        try {
            Videoeffect.this.tvStartVideo.setText(VideoPlayer.formatTimeUnit((long) Videoeffect.this.time));
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

    public static final Uri getUriToDrawable(@NonNull Context context,
                                             @AnyRes int drawableId) {
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId));
        return imageUri;
    }

    private String getDurationString(int i) {
        return twoDigitString(i / 3600) + ":" + twoDigitString((i % 3600) / 60) + ":" + twoDigitString(i % 60);
    }

    public boolean SaveImage(String str, int i, Bitmap bitmap) {
        String str2 = outPutFolder;
        File file = new File(str2);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            new BitmapFactory.Options().inSampleSize = 5;
            this.shareImageFileName = str2 + str + ".png";
            OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.shareImageFileName));
            bitmap.compress(Bitmap.CompressFormat.PNG, i, bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            MediaScannerConnection.scanFile(this, new String[]{this.shareImageFileName.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String str, Uri uri) {
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return true;
    }

    private void copyRawToSdcard() {
        Bitmap resizedBitmap = getResizedBitmap(((BitmapDrawable) overLaydrawable).getBitmap(), videowidth, videoheight);
        Bitmap createBitmap = Bitmap.createBitmap(videowidth, videoheight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint();
        paint.setAlpha(this.overlayvalue);
        canvas.drawBitmap(resizedBitmap, 0.0f, 0.0f, paint);
        SaveImage(UUID.randomUUID().toString(), 100, createBitmap);
    }

    public Bitmap getResizedBitmap(Bitmap bitmap, int i, int i2) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float f = ((float) i) / ((float) width);
        float f2 = ((float) i2) / ((float) height);
        Matrix matrix = new Matrix();
        matrix.postScale(f, f2);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
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


                final Dialog dialog = showSingleOptionTextDialog(Videoeffect.this);
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

                        if (selectitem == 1) {
                            overlaybuild();


                        } else {
                            Toast.makeText(Videoeffect.this, "Please Select Overlay design", Toast.LENGTH_SHORT).show();
                        }


                        dialog.dismiss();
                    }

                });
                dialog.show();
                break;

        }


    }

    private void overlaybuild() {

/*
        File myDirectory = new File(Environment.getExternalStorageDirectory() + "/Videoeditor/");

        if (!myDirectory.exists()) {
            myDirectory.mkdirs();
        }
        File imageFile = new File(Environment.getExternalStorageDirectory() + "/Videoeditor/" + effectName[position]);
        if (!imageFile.exists()) try {

            InputStream is = getAssets().open(effectName[position]);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

       /* File myDirectory = new File(Environment.getExternalStorageDirectory(), "Temp");

        if (!myDirectory.exists()) {
            myDirectory.mkdirs();
        }*/
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");
       /* File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);*/

        String filePrefix = "overlay";
        String fileExtn = ".mp4";

        File dest = new File(mediaStorageDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
        }


        Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());

        filePath = dest.getAbsolutePath();

        String[] cmd = {"-i", "" + inputvideo, "-i", "" + shareImageFileName, "-filter_complex", "overlay", filePath};
                /*String[] cmd = {"-i", "" + inputvideo, "-i", "" +shareImageFileName, "-filter_complex","[1:v]format=argb,geq=r='r(X,Y)':a='0.5*alpha(X,Y)'[zork];[0:v][zork]overlay", filePath};*/
              /*  if (rotationAngle.length() != 0) {
                    String[] cmd = {"-i", "" + inputvideo, "-i", "" + imageFile.getPath(),shareImageFileName,"-filter_complex",rotationAngle + ",scale=" +videowidth + "x" + videoheight + ",overlay", "-t", getDurationString(videoend), "-c:v", "libx264", "-preset", "ultrafast", "-ar", "44100", "-ac", "2", "-ab", "128k", "-strict", "-2", filePath};
                    execFFmpegBinary(cmd);
                }else {

                    String[] cmd = {"-i", "" + inputvideo, "-i", "" + imageFile.getPath(),"-filter_complex", "scale=" + videowidth + "x" + videoheight + ",overlay", "-t", getDurationString(videoend), "-c:v", "libx264", "-preset", "ultrafast", "-ar", "44100", "-ac", "2", "-ab", "128k", "-strict", "-2", filePath};
                    execFFmpegBinary(cmd);
                }
*/
               /* String[] cmd = {"-i", "" + inputvideo, "-i", shareImageFileName, "-filter_complex", "scale=720:-1,overlay", "-t", getDurationString(5), "-c:v", "libx264", "-preset", "ultrafast", "-ar", "44100", "-ac", "2", "-ab", "128k", "-strict", "-2", filePath};*/
        execFFmpegBinary(cmd);

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
    protected void onDestroy() {
        super.onDestroy();
        //Log.e("videoeffect", "onDestroy");
        File file = new File(outPutFolder);

        if (file.exists()) {
            //Log.e("in", "in");
            deleteFolder(file.getAbsolutePath());

        }


    }
}
