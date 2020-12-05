package com.example.enix.videoeditor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.util.Arrays;

import static android.R.attr.duration;

public class Video_editor extends android.app.Fragment {


    private ProgressDialog progressDialog;

    private Runnable r;
    RecyclerView option;
    private FFmpeg ffmpeg;
    private String filePath;
    private Option_adapter option_adapter;
    private int choice = 0;
    private String[] lastReverseCommand;
    private static final String FILEPATH = "filepath";
    String yourRealPath;
    int[] option_menu = {R.drawable.compress, R.drawable.fadin, R.drawable.fastmostion, R.drawable.slowmotion, R.drawable.reverse};
    String[] optionname = {"Compress \n     Video", "Fadein \nFadout", "    Fast\n Motion", "   Slow \n Motion", "Reverse \n  Video"};

    public interface OnDataPass {
        public void onDataPass(String data);
    }

    OnDataPass onDataPass;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_video_editor, container, false);

        yourRealPath = getArguments().getString("editfile");

      /*  videoView = (VideoView) findViewById(R.id.videoView);
       rangeSeekBar = (RangeSeekBar)view.findViewById(R.id.rangeSeekBar);
        option = (RecyclerView) view.findViewById(R.id.option);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);
        /*rangeSeekBar.setEnabled(false);
*/

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);
        option = (RecyclerView) view.findViewById(R.id.option);
        option_adapter = new Option_adapter(getActivity(), option_menu, optionname);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        option.setHasFixedSize(true);
        option.setLayoutManager(layoutManager);
        option.setItemAnimator(new DefaultItemAnimator());
        option.setAdapter(option_adapter);

        YoYo.with(Techniques.SlideInDown)
                .duration(700)
                .playOn(option);

/*
        uploadVideo();*/

        loadFFMpegBinary();


        option.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (position == 0) {

                    choice = 1;

                    executeCompressCommand();


                } else if (position == 1) {
                    choice = 5;


                    executeFadeInFadeOutCommand();


                } else if (position == 2) {

                    choice = 6;

                    executeFastMotionVideoCommand();




                } else if (position == 3) {
                    choice = 7;

                    executeSlowMotionVideoCommand();




                } else if (position == 4) {
                    choice = 8;

                    final Dialog dialog = showSingleOptionTextDialog(getActivity());
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
                            splitVideoCommand(yourRealPath);
                            dialog.dismiss();
                        }

                    });
                    dialog.show();


                }

            }


        }));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onDataPass = (OnDataPass) context;
    }

    private void splitVideoCommand(String path) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");
        String filePrefix = "split_video";
        String fileExtn = ".mp4";
       /* String yourRealPath = path;*/

        File dir = new File(mediaStorageDir, ".VideoSplit");
        if (dir.exists())
            deleteDir(dir);
        dir.mkdir();
        File dest = new File(dir, filePrefix + "%03d" + fileExtn);

        String[] complexCommand = {"-i", yourRealPath, "-c:v", "libx264", "-crf", "22", "-map", "0", "-segment_time", "6", "-g", "9", "-sc_threshold", "0", "-force_key_frames", "expr:gte(t,n_forced*6)", "-f", "segment", dest.getAbsolutePath()};
        execFFmpegBinary(complexCommand);
    }

    private void executeSlowMotionVideoCommand() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");

        String filePrefix = "slowmotion_video";
        String fileExtn = ".mp4";
        //String yourRealPath = getPath(getActivity(), selectedVideoUri);


        File dest = new File(mediaStorageDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
        }


        Log.d("startTrim: src: ", "" + yourRealPath);
        Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        String[] complexCommand = {"-y", "-i", yourRealPath, "-filter_complex", "[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", filePath};
        execFFmpegBinary(complexCommand);

    }


    private void executeFastMotionVideoCommand() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");

        String filePrefix = "speed_video";
        String fileExtn = ".mp4";
        //String yourRealPath = getPath(getActivity(), selectedVideoUri);


        File dest = new File(mediaStorageDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
        }


        Log.d("startTrim: src: ", "" + yourRealPath);
        Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        String[] complexCommand = {"-y", "-i", yourRealPath, "-filter_complex", "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", filePath};
        execFFmpegBinary(complexCommand);

    }

    private void executeFadeInFadeOutCommand() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");

        String filePrefix = "fade_video";
        String fileExtn = ".mp4";


        File dest = new File(mediaStorageDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
        }


        Log.d("startTrim: src: ", "" + yourRealPath);
        Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        String[] complexCommand = {"-y", "-i", yourRealPath, "-acodec", "copy", "-vf", "fade=t=in:st=0:d=5,fade=t=out:st=" + String.valueOf(duration - 5) + ":d=5", filePath};
        execFFmpegBinary(complexCommand);

    }

    public void passData(String data) {
        onDataPass.onDataPass(data);
    }


    private void executeCompressCommand() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");

        String filePrefix = "compress_video";
        String fileExtn = ".mp4";
        //String yourRealPath = getPath(getActivity(), selectedVideoUri);


        File dest = new File(mediaStorageDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
        }


        Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();
        String[] complexCommand = {"-y", "-i", yourRealPath, "-s", "160x120", "-r", "25", "-vcodec", "mpeg4", "-b:v", "150k", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
        execFFmpegBinary(complexCommand);

    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    //Log.e("FAILED with output : ", "" + s);

                    progressDialog.dismiss();
                }

                @Override
                public void onSuccess(String s) {

                    if (choice == 1 || choice == 5 || choice == 6 || choice == 7) {
                      /*  Intent intent = new Intent(getActivity(), Editplayer.class);
                        intent.putExtra("tempfile", filePath);*/
                        /*getActivity().startActivity(intent);*/

                        passData(filePath);

                    } else if (choice == 8) {
                        choice = 9;
                        reverseVideoCommand();
                    } else if (Arrays.equals(command, lastReverseCommand)) {
                        choice = 10;
                        concatVideoCommand();
                    } else if (choice == 10) {
                        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");
                        File destDir = new File(mediaStorageDir, ".VideoPartsReverse");
                        File dir = new File(mediaStorageDir, ".VideoSplit");
                        if (dir.exists())
                            deleteDir(dir);
                        if (destDir.exists())
                            deleteDir(destDir);
                        choice = 11;

                        passData(filePath);
                       /* Intent intent = new Intent(getActivity(), Editplayer.class);
                        intent.putExtra("tempfile", filePath);
                        getActivity().startActivity(intent);*/
                    }
                }

                @Override
                public void onProgress(String s) {


                        progressDialog.setMessage("Processing..........");



                }

                @Override
                public void onStart() {

                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {

                    if (choice != 8 && choice != 9 && choice != 10) {
                        progressDialog.dismiss();
                    }

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void concatVideoCommand() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");
        File srcDir = new File(mediaStorageDir, ".VideoPartsReverse");
        File[] files = srcDir.listFiles();
        if (files != null && files.length > 1) {
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        }
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder filterComplex = new StringBuilder();
        filterComplex.append("-filter_complex,");
        for (int i = 0; i < files.length; i++) {
            stringBuilder.append("-i" + "," + files[i].getAbsolutePath() + ",");
            filterComplex.append("[").append(i).append(":v").append(i).append("] [").append(i).append(":a").append(i).append("] ");

        }
        filterComplex.append("concat=n=").append(files.length).append(":v=1:a=1 [v] [a]");
        String[] inputCommand = stringBuilder.toString().split(",");
        String[] filterCommand = filterComplex.toString().split(",");

        String filePrefix = "reverse_video";
        String fileExtn = ".mp4";
        File dest = new File(mediaStorageDir, filePrefix + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
        }
        filePath = dest.getAbsolutePath();
        String[] destinationCommand = {"-map", "[v]", "-map", "[a]", dest.getAbsolutePath()};
        execFFmpegBinary(combine(inputCommand, filterCommand, destinationCommand));
    }

    public static String[] combine(String[] arg1, String[] arg2, String[] arg3) {
        String[] result = new String[arg1.length + arg2.length + arg3.length];
        System.arraycopy(arg1, 0, result, 0, arg1.length);
        System.arraycopy(arg2, 0, result, arg1.length, arg2.length);
        System.arraycopy(arg3, 0, result, arg1.length + arg2.length, arg3.length);
        return result;
    }

    private void reverseVideoCommand() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");
        File srcDir = new File(mediaStorageDir, ".VideoSplit");
        File[] files = srcDir.listFiles();
        String filePrefix = "reverse_video";
        String fileExtn = ".mp4";
        File destDir = new File(mediaStorageDir, ".VideoPartsReverse");
        if (destDir.exists())
            deleteDir(destDir);
        destDir.mkdir();
        for (int i = 0; i < files.length; i++) {
            File dest = new File(destDir, filePrefix + i + fileExtn);
            String command[] = {"-i", files[i].getAbsolutePath(), "-vf", "reverse", "-af", "areverse", dest.getAbsolutePath()};
            if (i == files.length - 1)
                lastReverseCommand = command;
            execFFmpegBinary(command);
        }


    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
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


    private Dialog showSingleOptionTextDialog(Context mContext) {
        Dialog textDialog = new Dialog(mContext, R.style.DialogAnimation);
        textDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        textDialog.setContentView(R.layout.dialog_singleoption_text);
        textDialog.setCancelable(false);
        return textDialog;
    }


    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {

                ffmpeg = FFmpeg.getInstance(getActivity());
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    //Log.e("ffmpeg ", " not Loaded");
                    showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    //Log.e("ffmpeg ", " correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
            //Log.e("ffmpeg ", " correct Loaded" + e.toString());
        }


    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .create()
                .show();

    }


   /* private void uploadVideo() {

        Intent intent = new Intent();
        intent.setType("video*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);

    }*/

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();
                videoView.setVideoURI(selectedVideoUri);

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                videoView.setLayoutParams(new RelativeLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels));
                videoView.start();


                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        duration = mp.getDuration() / 1000;
                        tvLeft.setText("00:00:00");

                        tvRight.setText(getTime(mp.getDuration() / 1000));
                        mp.setLooping(true);
                        rangeSeekBar.setRangeValues(0, duration);
                        rangeSeekBar.setSelectedMinValue(0);
                        rangeSeekBar.setSelectedMaxValue(duration);
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
        }
    }
*/
}
