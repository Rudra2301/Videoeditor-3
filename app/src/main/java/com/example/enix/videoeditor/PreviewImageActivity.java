package com.example.enix.videoeditor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Bhuvnesh on 09-03-2017.
 */

public class PreviewImageActivity extends AppCompatActivity {

    private static final String FILEPATH = "filepath";
    ImageButton btnShare, create_done1;
    ImageButton btn_back, btn_reset;
    TextView toolbar_title;
    File[] listFile;

    Context mContext = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        TextView tvInstruction = (TextView) findViewById(R.id.tvInstruction);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);

        toolbar_title.setText("Preview");
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        this.btnShare = (ImageButton) findViewById(R.id.create_done);
        this.create_done1 = (ImageButton) findViewById(R.id.create_done1);
        create_done1.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.GONE);

        GridLayoutManager lLayoutlLayout = new GridLayoutManager(PreviewImageActivity.this, 4);
        RecyclerView rView = (RecyclerView) findViewById(R.id.recycler_view);
        rView.setHasFixedSize(true);
        rView.setLayoutManager(lLayoutlLayout);
        YoYo.with(Techniques.Wave)
                .duration(700)
                .playOn(rView);
        String filePath = getIntent().getStringExtra(FILEPATH);
        ArrayList<String> f = new ArrayList<String>();

        File dir = new File(filePath);
        tvInstruction.setText("Images stored at path " + filePath);


        listFile = dir.listFiles();


        for (File e : listFile) {
            f.add(e.getAbsolutePath());
        }

        PreviewImageAdapter rcAdapter = new PreviewImageAdapter(f);
        rView.setAdapter(rcAdapter);


        this.create_done1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Share();
            }
        });


        this.btn_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                Intent start = new Intent(PreviewImageActivity.this, Pick_video.class);
                start.setFlags(268468224);
                startActivity(start);
                finish();


            }
        });


    }

    private void Share() {


        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
        intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

        ArrayList<Uri> files = new ArrayList<Uri>();

        for (File path : listFile /* List of the files you want to send */) {

            Uri uri = FileProvider.getUriForFile(mContext,
                    mContext.getPackageName() + ".provider", path);

            //Uri uri = Uri.fromFile(path);
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        startActivity(intent);
    }


}
