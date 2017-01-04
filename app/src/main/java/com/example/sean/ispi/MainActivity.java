package com.example.sean.ispi;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final int PERMISSIONS_REQUEST_LOCATION = 0;
    private final int PERMISSIONS_REQUEST_CAMERA = 1;
    private final int REQUEST_TAKE_PHOTO = 1;
    private final int REQUEST_SETTINGS_ACTIVITY = 2;

    private final String TAG = "Main";

    private String imageFileName;

    private Bitmap bitmap;

    private GoogleApiClient mGoogleApiClient;

    protected Location mLast;

    private ImageView mImg;
    private TextView mLat;
    private TextView mLong;

    private float mRange = 0;
    private double latitude = 0.0;
    private double longitude = 0.0;

    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int permissionCheckLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheckCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if(permissionCheckLocation != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_REQUEST_LOCATION);
        }
        if(permissionCheckCamera != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},PERMISSIONS_REQUEST_CAMERA);
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {

            Intent i = new Intent(MainActivity.this, SettingsActivity.class);

            this.startActivityForResult(i,REQUEST_SETTINGS_ACTIVITY);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected");
        updateLocation();
    }

    protected void updateLocation(){
        try{
            mLast = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch (SecurityException se){
            Log.d(TAG, "Exception trying to set last location: " + se);
        }
    }

    protected Location getLocation() {return mLast;}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "DOES NOT COMPUTE! (location)", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "DOES NOT COMPUTE! (camera)", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();

            AlertDialog.Builder alertAdd = new AlertDialog.Builder(
                    MainActivity.this);
            LayoutInflater factory = LayoutInflater.from(MainActivity.this);
            final View view = factory.inflate(R.layout.image_preview, null);
            alertAdd.setView(view);
            mImg = (ImageView) view.findViewById(R.id.imageView);

            int targetW = 320;
            int targetH = 480;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            mImg.setImageBitmap(bitmap);

            alertAdd.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    try{
                        sendPhoto(bitmap);}
                    catch (Exception e){

                    }
                }
            });
            alertAdd.setNeutralButton("RETAKE", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    takePhoto();
                }
            });
            alertAdd.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    //close dialog
                }
            });
            alertAdd.setTitle("Submit Picture");

            alertAdd.show();

            mLat = (TextView) view.findViewById(R.id.latitude);
            mLong = (TextView) view.findViewById(R.id.longitude);

            //updateLocation();
            Location lastLocation = getLocation();
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
            if(lastLocation != null){
                mLat.setText(" " + Double.toString(latitude) + ", ");
                mLong.setText(" " + Double.toString(longitude) + " ");
            }
        }

        if(requestCode == REQUEST_SETTINGS_ACTIVITY && resultCode == RESULT_OK) {
            mRange = data.getFloatExtra("DIST",0);
            boolean reset = data.getBooleanExtra("RESET",false);
            if(mRange != 0){
                ((MainActivityFragment) getFragmentManager().findFragmentById(R.id.fragment)).setRange(mRange);
            }
            if(reset) {
                ((MainActivityFragment) getFragmentManager().findFragmentById(R.id.fragment)).resetStats();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID) + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void takePhoto(){
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "IOException trying to set last location: " + ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void sendPhoto(Bitmap bitmap) throws Exception {
        new MainActivity.UploadTask().execute(bitmap);
        new MainActivity.SubmitTask().execute(imageFileName);
    }

    protected void fetchPhoto() throws Exception{
        new MainActivity.FetchTask().execute();
    }

    private class UploadTask extends AsyncTask<Bitmap, Void, Void> {

        protected Void doInBackground(Bitmap... bitmaps) {
            if (bitmaps[0] == null)
                return null;
            setProgress(0);

            Bitmap bitmap = bitmaps[0];
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // convert Bitmap to ByteArrayOutputStream
            InputStream in = new ByteArrayInputStream(stream.toByteArray()); // convert ByteArrayOutputStream to ByteArrayInputStream

            HttpClient httpclient = new DefaultHttpClient();
            try {
                HttpPost httppost = new HttpPost(
                        "http://www.seanpmuir.com/ispi/savetofile.php"); // server http://www.seanpmuir.com/ispi/savetofile.php

                MultipartEntity reqEntity = new MultipartEntity();
                reqEntity.addPart("myFile",
                        imageFileName + ".jpg", in);
                httppost.setEntity(reqEntity);

                Log.i(TAG, "request " + httppost.getRequestLine());
                HttpResponse response = null;
                try {
                    response = httpclient.execute(httppost);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (response != null)
                        Log.i(TAG, "response " + response.getStatusLine().toString());
                } finally {

                }
            } finally {

            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Toast.makeText(MainActivity.this, "Uploaded!", Toast.LENGTH_LONG).show();
        }
    }

    private class SubmitTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            try {

                String link="http://www.seanpmuir.com/ispi/submit_image.php";
                String data  = URLEncoder.encode("image_path", "UTF-8") + "=" +
                        URLEncoder.encode(strings[0] + ".jpg", "UTF-8");
                data += "&" + URLEncoder.encode("latitude", "UTF-8") + "=" +
                        URLEncoder.encode(Double.toString(latitude), "UTF-8");
                data += "&" + URLEncoder.encode("longitude", "UTF-8") + "=" +
                        URLEncoder.encode(Double.toString(longitude), "UTF-8");

                URL url = new URL(link);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write( data );
                wr.flush();

                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

            } catch (Exception e){
                Log.d(TAG, "Exception: " + e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(MainActivity.this, "Data Saved!", Toast.LENGTH_LONG).show();
        }
    }

    public class FetchTask extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... strings) {

            try {

                String link="http://www.seanpmuir.com/ispi/request_image.php";
                String data = URLEncoder.encode("range", "UTF-8") + "=" +
                        URLEncoder.encode(Double.toString(mRange), "UTF-8");
                data += "&" + URLEncoder.encode("latitude", "UTF-8") + "=" +
                        URLEncoder.encode(Double.toString(latitude), "UTF-8");
                data += "&" + URLEncoder.encode("longitude", "UTF-8") + "=" +
                        URLEncoder.encode(Double.toString(longitude), "UTF-8");

                URL url = new URL(link);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write( data );
                wr.flush();

                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                ((MainActivityFragment) getFragmentManager().findFragmentById(R.id.fragment)).getData(sb.toString());

                return sb.toString();

            } catch (Exception e){
                Log.d(TAG, "Exception: " + e);
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(MainActivity.this, "Data Retrieved!", Toast.LENGTH_LONG).show();
        }
    }
}
