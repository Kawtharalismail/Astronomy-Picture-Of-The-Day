package com.barmej.apod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.barmej.apod.fragments.AboutFragments;
import com.barmej.apod.network.NetworkUtils;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 100;
    public static final String TAG=MainActivity.class.getSimpleName();
    private String dateEndPoint="";
    private String hdUrl="";
    private TouchImageView picView;
    private WebView video;
    private ProgressBar progressBar;
    private LinearLayout detailsInfoLayer;
    private TextView titleText,detailText;
    private JSONObject jsonObject;
    private boolean havePermission;
    private final Calendar myCalendar = Calendar.getInstance();
    private DatePicker simpleDatePicker;
    private DatePickerDialog.OnDateSetListener date;
    private NetworkUtils mNetworkUtils;
    String imgUri;
    private boolean isVideo=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpView();
        mNetworkUtils= NetworkUtils.getInstance(this);
        requestAstronomyData(mNetworkUtils.getAstronomyUri());
        checkPermission();

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
                        isVideo=true;
                        picView.setVisibility(View.GONE);
                        video.setVisibility(View.VISIBLE);
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
        astronomyRequest.setTag(TAG);
        mNetworkUtils.addToRequestQueue(astronomyRequest);
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
        if(item.getItemId()==R.id.action_download_hd )
        {
            if(isVideo)Toast.makeText(MainActivity.this,"OOps..Video Download Is Not Supported ",Toast.LENGTH_LONG).show();
            else DownloadHD();
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

       // PackageManager pm = getApplicationContext().getPackageManager();
        try {
           /* ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap lineABmp = ((BitmapDrawable)picView.getDrawable()).getBitmap();
            Bitmap copy = Bitmap.createBitmap(lineABmp);
            Bitmap tempBitmap =Bitmap.createBitmap(copy, 0, 0, copy.getWidth(), copy.getHeight(), matrix, true);
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), tempBitmap, "Title", null);
           */
            Uri imageUri = Uri.parse(hdUrl);

            @SuppressWarnings("unused")
          //  PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, imageUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "My Photo");
            startActivity(Intent.createChooser(shareIntent, "Share with"));

        } catch (Exception e) {
            Log.e("Error on sharing", e + " ");
            Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void DownloadHD() {

        if (havePermission)
        {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
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
            Toast.makeText(MainActivity.this,"error"+havePermission,Toast.LENGTH_LONG).show();
        }
    }

    private void ShowDatePickerDialog() {

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR); // get the current year
        int month = cal.get(Calendar.MONTH); // month...
        int day = cal.get(Calendar.DAY_OF_MONTH); // current day in the month
        DatePickerDialog datePickerDialog=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
         // Toast.makeText(MainActivity.this,i+"/"+i1+"/"+i2,Toast.LENGTH_LONG).show();
         dateEndPoint=mNetworkUtils.getAstronomyUri()+"&date="+i+"-"+i1+"-"+i2;
         requestAstronomyData(dateEndPoint);
            }
        },year,month,day);
        datePickerDialog.show();
    }


    public  void checkPermission() {

            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                havePermission=true;
                Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
            } else {
                //Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
            }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                havePermission=true;
                Toast.makeText(MainActivity.this, "Write External Storage Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(MainActivity.this, "Write External Storage Denied", Toast.LENGTH_SHORT) .show();
            }
        }

    }

    private void showAlertDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AboutFragments aboutFragment = new AboutFragments();
        aboutFragment.show(fm, "fragment_alert");

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
}
