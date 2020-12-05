package com.example.enix.videoeditor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

public class Gifpreview extends AppCompatActivity {

    ImageView pGif;
    private static final String FILEPATH = "filepath";

    ImageButton btnShare, create_done1;
    ImageButton btn_back, btn_reset;
    TextView toolbar_title;
    String filePath;
    ImageButton fb,insta,what;

    Context mContext = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_gifpreview);

      /*  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

         filePath = getIntent().getStringExtra(FILEPATH);

        pGif = (ImageView) findViewById(R.id.viewGif);

        toolbar_title = (TextView) findViewById(R.id.toolbar_title);

        toolbar_title.setText("Preview");
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        this.btnShare = (ImageButton) findViewById(R.id.create_done);
        this.create_done1 = (ImageButton) findViewById(R.id.create_done1);
        create_done1.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.GONE);

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
        this.create_done1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Share();
            }
        });


        this.btn_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                Intent start = new Intent(Gifpreview.this, Pick_video.class);
                start.setFlags(268468224);
                startActivity(start);
                finish();


            }
        });


        File giffile = new File(filePath);

        Uri uri = Uri.fromFile(giffile);

        Glide.with(Gifpreview.this)
                .load(uri)
                .into(pGif);
    }
    public void facebook() {
        // Uri uri = Uri.parse("file://" + path);

        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(mContext,
                mContext.getPackageName() + ".provider", file);


        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage("com.facebook.katana");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.setType("image/*");
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
        share.setType("image/*");
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
        share.setType("image/*");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share image File"));


    }

    private void Share() {

        File file=new File(filePath);


        try {
            Intent share = new Intent("android.intent.action.SEND");
            share.setType("video/*");
            share.putExtra("android.intent.extra.STREAM", Uri.fromFile(file));
            startActivity(Intent.createChooser(share, "Share File"));

        } catch (Exception e) {
        }
    }
}
