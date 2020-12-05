package com.example.enix.videoeditor;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;


public class AudioPreviewActivity extends AppCompatActivity {

    private VisualizerView mVisualizerView;

    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private static final String FILEPATH = "filepath";
    ImageButton btnShare, create_done1;
    ImageButton btn_back, btn_reset;
    TextView toolbar_title;
    String filePath;
    ImageButton fb, insta, what;

    Context mContext = this;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_preview);
       /* getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
*/
        mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);

        toolbar_title = (TextView) findViewById(R.id.toolbar_title);

        toolbar_title.setText("Preview");
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        this.btnShare = (ImageButton) findViewById(R.id.create_done);
        this.create_done1 = (ImageButton) findViewById(R.id.create_done1);
        create_done1.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.GONE);


        this.create_done1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Share();
            }
        });


        this.btn_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                Intent start = new Intent(AudioPreviewActivity.this, Pick_video.class);
                start.setFlags(268468224);
                startActivity(start);
                finish();


            }
        });

        this.fb = (ImageButton) findViewById(R.id.fb);
        this.insta = (ImageButton) findViewById(R.id.insta);
        this.what = (ImageButton) findViewById(R.id.what);

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                facebook();
            }
        });


        insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                instagram();
            }
        });

        what.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                whatsup();

            }
        });


    }

    public void facebook() {
        // Uri uri = Uri.parse("file://" + path);

        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(mContext,
                mContext.getPackageName() + ".provider", file);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage("com.facebook.katana");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("audio/*");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share image File"));

    }

    public void instagram() {
        // Uri uri = Uri.parse("file://" + path);

        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(mContext,
                mContext.getPackageName() + ".provider", file);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage("com.instagram.android");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("audio/*");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share image File"));

    }

    public void whatsup() {

        // Uri uri = Uri.parse("file://" + path);

        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(mContext,
                mContext.getPackageName() + ".provider", file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage("com.whatsapp");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("audio/*");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share image File"));


    }

    private void Share() {

        // Uri uri = Uri.parse("file://" + path);

        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(mContext,
                mContext.getPackageName() + ".provider", file);


        try {
            Intent share = new Intent("android.intent.action.SEND");
            share.setType("audio/*");
            share.putExtra("android.intent.extra.STREAM", uri);
            startActivity(Intent.createChooser(share, "Share File"));

        } catch (Exception e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initAudio();
    }

    /* @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // handle arrow click here
         if (item.getItemId() == android.R.id.home) {
             finish(); // close this activity and return to preview activity (if there is any)
         }

         return super.onOptionsItemSelected(item);
     }
 */
    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void initAudio() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        filePath = getIntent().getStringExtra(FILEPATH);
        TextView tvInstruction = (TextView) findViewById(R.id.tvInstruction);
        tvInstruction.setText("Audio stored at path " + filePath);
        mMediaPlayer = MediaPlayer.create(this, Uri.parse(filePath));

        setupVisualizerFxAndUI();
        // Make sure the visualizer is enabled only when you actually want to
        // receive data, and
        // when it makes sense to receive data.
        mVisualizer.setEnabled(true);
        // When the stream ends, we don't need to collect any more data. We
        // don't do this in
        // setupVisualizerFxAndUI because we likely  to have more,
        // non-Visualizer related code
        // in this callback.
        mMediaPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mVisualizer.setEnabled(false);
                    }
                });
        mMediaPlayer.start();
        mMediaPlayer.setLooping(true);

    }

    private void setupVisualizerFxAndUI() {

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }
}
