package com.example.picaso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.example.picaso.databinding.ActivityMainBinding;
import com.example.picaso.databinding.ActivityResultBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class resultActivity extends AppCompatActivity {
    ActivityResultBinding binding;
    private InterstitialAd mInterstitialAd;
    int IMAGE_REQUEST_CODE = 10;
    int IMAGE_EDITED_CODE = 20;
    Drawable drawable;
    Bitmap bitmap;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST_CODE){
            if(data.getData()!=null){
                Uri imagepath = data.getData();
                Intent dsPhotoEditorIntent = new Intent(this, DsPhotoEditorActivity.class);
                dsPhotoEditorIntent.setData(imagepath);
                dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Picaso");
                int[] toolsToHide = {DsPhotoEditorActivity.TOOL_ORIENTATION, DsPhotoEditorActivity.TOOL_CROP};
                dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE, toolsToHide);
                startActivityForResult(dsPhotoEditorIntent, IMAGE_EDITED_CODE);
            }
        }
        if(requestCode==IMAGE_EDITED_CODE){
            Intent intent = new Intent(resultActivity.this,resultActivity.class);
            intent.setData(data.getData());
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.image.setImageURI(getIntent().getData());
        getSupportActionBar().hide();
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.show(resultActivity.this);
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,IMAGE_REQUEST_CODE);
            }
        });
        binding.Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(resultActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        binding.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.image.getDrawable();
               Bitmap bitmap = bitmapDrawable.getBitmap();
               shareimage(bitmap);
            }
        });
        binding.whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable)binding.whatsapp.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                whatsappshare(bitmap);
            }
        });
    }
    private void whatsappshare(Bitmap bitmap) {
        Uri uri = getUri(bitmap);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM,uri);
        i.putExtra(Intent.EXTRA_TEXT,"Sharing image..");
        i.putExtra(Intent.EXTRA_SUBJECT,"see that ");
        i.setType("image/png");
        i.setPackage("com.whatsapp");
        startActivity(i);
    }
    private void shareimage(Bitmap bitmap) {
        Uri uri = getUri(bitmap);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM,uri);
        i.putExtra(Intent.EXTRA_TEXT,"Sharing image..");
        i.putExtra(Intent.EXTRA_SUBJECT,"see that ");
        i.setType("image/png");
        startActivity(Intent.createChooser(i,"share via"));
    }

    private Uri getUri(Bitmap bitmap) {
        File f = new File(getCacheDir(),"images");
        Uri uri = null;
        try{
            f.mkdirs();
            File file = new File(f,"images.png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            uri = FileProvider.getUriForFile(this,"com.example.picaso",file);

        } catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }
}
