package com.example.enix.videoeditor;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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

import static com.example.enix.videoeditor.Videofilter.showSingleOptionTextDialog;

public class Blurvideo extends Fragment {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private VideoView videoView;
    private RangeSeekBar rangeSeekBar;
    private ProgressDialog progressDialog;
    private Uri selectedVideoUri;
    private TextView tvLeft, tvRight;

    String yourRealPath;
    int videoend;
    int videowidth;
    int videoheight;
    RecyclerView option;
    private FFmpeg ffmpeg;
    private String filePath;
    private Blur_adapter option_adapter;
    String shareImageFileName;
    String outPutFolder = (Environment.getExternalStorageDirectory() + "/VideoEffects/");
    private static final String FILEPATH = "filepath";
    int[] option_menu = {R.drawable.boxblur, R.drawable.horizonatalbox, R.drawable.img_specificblur};
    String[] optionname = {"Box \nBlur", "Horizontal \n  Box Blur", "Specific \n    Blur"};
    String inputvideo;

    public interface OnDataPass1 {
        public void onDataPass(String data);
    }

    OnDataPass1 onDataPass;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_blurvideo, container, false);

        inputvideo = getArguments().getString("editfile");


        option = (RecyclerView) view.findViewById(R.id.option_blur);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);

        option_adapter = new Blur_adapter(getActivity(), option_menu, optionname);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        option.setHasFixedSize(true);
        option.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(option.getContext(),
                layoutManager.getOrientation());

        option.setItemAnimator(new DefaultItemAnimator());
        option.setAdapter(option_adapter);


        YoYo.with(Techniques.SlideInDown)
                .duration(700)
                .playOn(option);


       /* uploadVideo();*/

        loadFFMpegBinary();


        option.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {


                File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name) + "-Temp");

                String filePrefix = "Blur";
                String fileExtn = ".mp4";
               /* yourRealPath = getPath(getActivity(), selectedVideoUri);*/
                File dest = new File(mediaStorageDir, filePrefix + fileExtn);
                int fileNo = 0;
                while (dest.exists()) {
                    fileNo++;
                    dest = new File(mediaStorageDir, filePrefix + fileNo + fileExtn);
                }

             /*   Log.d("startTrim: src: ", "" + yourRealPath);*/
                //Log.d("startTrim: dest: ", "" + dest.getAbsolutePath());

                filePath = dest.getAbsolutePath();


                if (position == 0) {

                    //for simple Box blur
                    String[] cmd = {"-i", "" + inputvideo, "-vf", "boxblur=5:1", filePath};

                    execFFmpegBinary(cmd);
                }
                if (position == 1) {

                    //for horizontal Box blur


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


                            String[] cmd = {"-i", inputvideo, "-lavfi", "[0:v]scale=256/81*iw:256/81*ih,boxblur=luma_radius=min(h\\,w)/40:luma_power=3:chroma_radius=min(cw\\,ch)/40:chroma_power=1[bg];[bg][0:v]overlay=(W-w)/2:(H-h)/2,setsar=1,crop=w=iw*81/256", "-c:v", "libx264", "-preset", "ultrafast", filePath};

                            execFFmpegBinary(cmd);
                            dialog.dismiss();
                        }

                    });
                    dialog.show();



                }

                if (position == 2) {

                    Intent intent = new Intent(getActivity(), SpecificBlurvideo.class);
                    intent.putExtra("tempfile", inputvideo);
                    startActivity(intent);

                  /*  //for Vertical Box blur

                    String[] cmd = {"-i", inputvideo ,"-lavfi","[0:v]scale=ih*16/9:16/9*iw,boxblur=luma_radius=min(h\\,w)/20:luma_power=1:chroma_radius=min(cw\\,ch)/20:chroma_power=1[bg];[bg][0:v]overlay=(W-w)/2:(H-h)/2,crop=h=iw*9/16","-c:v","libx264","-preset","ultrafast",filePath};

                    execFFmpegBinary(cmd);*/

                }

            }


        }));


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onDataPass = (OnDataPass1) context;
    }

    public void passData(String data) {
        onDataPass.onDataPass(data);
    }


   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blurvideo);


        
    }*/

    private void execFFmpegBinary(final String[] command) {
        try {


            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    //Log.e("FFmpeg", "concat onSuccess():");

                    passData(filePath);
                    /*Intent intent = new Intent(getActivity(), Editplayer.class);
                    intent.putExtra("tempfile", filePath);
                    startActivity(intent);
*/
                }

                @Override
                public void onProgress(String s) {



                        progressDialog.setMessage("Processing.....");

                }

                @Override
                public void onFailure(String message) {
                    progressDialog.dismiss();
                    //Log.e("FFmpeg", "concat onFailure():" + message);
                }

                @Override
                public void onStart() {
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
                    Log.e("ffmpeg ", " correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
            Log.e("ffmpeg ", " correct Loaded" + e.toString());
        }


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
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
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

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();
                videoView.setVideoURI(selectedVideoUri);

                inputvideo = getRealPathFromURI(selectedVideoUri);

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                videoView.setLayoutParams(new RelativeLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels));
                videoView.start();


            }
        }
    }*/

    private void uploadVideo() {

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);

    }

    /*private void copyRawToSdcard() {
        Bitmap resizedBitmap = getResizedBitmap(((BitmapDrawable) overLaydrawable).getBitmap(), videowidth, videoheight);
        Bitmap createBitmap = Bitmap.createBitmap(videowidth, videoheight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint();
        paint.setAlpha(127);
        canvas.drawBitmap(resizedBitmap, 0.0f, 0.0f, paint);
        SaveImage(UUID.randomUUID().toString(), 100, createBitmap);
    }*/

    public Bitmap getResizedBitmap(Bitmap bitmap, int i, int i2) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float f = ((float) i) / ((float) width);
        float f2 = ((float) i2) / ((float) height);
        Matrix matrix = new Matrix();
        matrix.postScale(f, f2);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }


    public boolean SaveImage(String str, int i, Bitmap bitmap) {
        String str2 = outPutFolder + "temp/";
        File file = new File(str2);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            new BitmapFactory.Options().inSampleSize = 5;
            shareImageFileName = str2 + str + ".png";
            OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(shareImageFileName));
            bitmap.compress(Bitmap.CompressFormat.PNG, i, bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            MediaScannerConnection.scanFile(getActivity(), new String[]{shareImageFileName.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
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

}
