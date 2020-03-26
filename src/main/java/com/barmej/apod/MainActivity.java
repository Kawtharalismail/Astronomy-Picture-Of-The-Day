package com.barmej.apod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.barmej.apod.fragments.AboutFragments;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RequestQueue mRequestQueue;
    private String url="https://api.nasa.gov/planetary/apod?api_key=fSL8o6mGokdxfCwo0yYhHonqU9XzKoM5lrfa3HlW";
    private String dateEndPoint="";
    private String hdUrl="";
    private TouchImageView picView;
    private WebView video;
    private ProgressBar progressBar;
    private LinearLayout detailsInfoLayer;
    private TextView titleText,detailText;
    private JSONObject jsonObject;
    private boolean havePermission;
    final Calendar myCalendar = Calendar.getInstance();
    DatePicker simpleDatePicker;
    DatePickerDialog.OnDateSetListener date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpView();
        mRequestQueue= Volley.newRequestQueue(this);
        requestAstronomyData(url);
        havePermission =haveStoragePermission();

    }

    private void setUpView(){

        picView=findViewById(R.id.img_picture_view);
        video=findViewById(R.id.wv_video_player);
        progressBar=findViewById(R.id.progressBar);
        detailsInfoLayer=findViewById(R.id.bottom_sheet);
        titleText=findViewById(R.id.title_text);
        detailText=findViewById(R.id.detail_text);
    }


    private void requestAstronomyData(final String url) {

        StringRequest astronomyRequest= new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                     jsonObject=new JSONObject(response);
                    if(jsonObject.getString("media_type").equals("image")){
                        if(video.getVisibility()==View.VISIBLE)
                        {
                            video.setVisibility(View.GONE);
                            picView.setVisibility(View.VISIBLE);}
                        Picasso.get().load(jsonObject.getString("url")).into(picView);
                        hdUrl=jsonObject.getString("hdurl");
                    }
                    else
                    {
                        if(picView.getVisibility()==View.VISIBLE)
                        {
                            picView.setVisibility(View.GONE);
                            video.setVisibility(View.VISIBLE);}
                        picView.setVisibility(View.GONE);
                        video.setVisibility(View.VISIBLE);
                        video.getSettings().setJavaScriptEnabled(true);
                        video.getSettings().setPluginState(WebSettings.PluginState.ON);
                        video.loadUrl(jsonObject.getString("url"));
                        video.setWebChromeClient(new WebChromeClient());                    }
                    titleText.setText(jsonObject.getString("title"));
                    detailText.setText(jsonObject.getString("explanation"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                 Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG).show();
            }
        });

        mRequestQueue.add(astronomyRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_pick_day)
        {
            ShowDatePickerDialog();
            return true;
        }
        if(item.getItemId()==R.id.action_download_hd)
        {
            DownloadHD();
            return true;

        }if(item.getItemId()==R.id.action_share)
        {
            ShareFile();
            return true;

        }if(item.getItemId()==R.id.action_about)
        {
            showAlertDialog();
            return true;

        }else

            return super.onOptionsItemSelected(item);
    }


    private void ShareFile() {

        PackageManager pm = getApplicationContext().getPackageManager();
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap lineABmp = ((BitmapDrawable)picView.getDrawable()).getBitmap();
            Bitmap copy = Bitmap.createBitmap(lineABmp);
            Bitmap tempBitmap =Bitmap.createBitmap(copy, 0, 0, copy.getWidth(), copy.getHeight(), matrix, true);
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), tempBitmap, "Title", null);
            Uri imageUri = Uri.parse(path);

            @SuppressWarnings("unused")
            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("image/*");
            //waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(android.content.Intent.EXTRA_STREAM, imageUri);
            waIntent.putExtra(Intent.EXTRA_TEXT, "My Photo");
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (Exception e) {
            Log.e("Error on sharing", e + " ");
            Toast.makeText(getApplicationContext(), "please check the text ", Toast.LENGTH_SHORT).show();
        }
    }

    private void DownloadHD() {

        if (havePermission)
        { DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri= Uri.parse(hdUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        // set title and description
        request.setTitle("Data Download");
        request.setDescription("Android Data download using DownloadManager.");
        //set the local destination for download file to a path within the application's external files directory
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,hdUrl.substring(hdUrl.lastIndexOf("/")));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("*/*");
        downloadManager.enqueue(request);}
        else{
            Toast.makeText(MainActivity.this,"error",Toast.LENGTH_LONG).show();
        }
    }

    private void ShowDatePickerDialog() {

        DatePickerDialog datePickerDialog=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
         // Toast.makeText(MainActivity.this,i+"/"+i1+"/"+i2,Toast.LENGTH_LONG).show();
         dateEndPoint=url+"&date="+i+"-"+i1+"-"+i2;
         requestAstronomyData(dateEndPoint);
            }
        },2019,8,01);
        datePickerDialog.show();
    }


    public  boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error","You have permission");
                return true;
            } else {

                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error","You already have the permission");
            return true;
        }
    }


    private void showAlertDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AboutFragments alertDialog = new AboutFragments();
        AlertDialog builder = new AlertDialog.Builder(this).create();
        View view=findViewById(R.id.img_nasa);
        builder.setView(view);
        Window window = builder.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        builder.show();
        alertDialog.show(fm, "fragment_alert");
    }
}
