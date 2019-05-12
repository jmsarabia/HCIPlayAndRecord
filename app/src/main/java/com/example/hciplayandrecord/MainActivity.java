package com.example.hciplayandrecord;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;
import android.widget.Toast;


import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import android.content.Context;

import com.affectiva.android.affdex.sdk.detector.VideoFileDetector;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    VideoView videoView;
    Uri videoFileUri;
    Button captureVideoButton;
    Button playVideoButton;
    Button analyzeVideoButton;
    public static int VIDEO_CAPTURED = 1;
    private static final int VIDEO_CAPTURE = 101;
    String fileName;

    //=========== implementing videoDetector ==========//
    VideoDetectorThread videoThread;
    private static final int PICK_VIDEO = 100;
    private static String LOG_TAG = "Affectiva";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureVideoButton = (Button)findViewById(R.id.CaptureVideoButton);
        playVideoButton = (Button) findViewById(R.id.PlayVideoButton);
        analyzeVideoButton = (Button)findViewById(R.id.AnalyzeVideoButton);

        if (!hasCamera()) {
            captureVideoButton.setEnabled(false);
            playVideoButton.setEnabled(false);
            analyzeVideoButton.setEnabled(false);
        }
        videoView = (VideoView)this.findViewById(R.id.VideoView);
        captureVideoButton.setOnClickListener(this);
        playVideoButton.setOnClickListener(this);
        analyzeVideoButton.setOnClickListener(this);

        playVideoButton.setEnabled(false);


    }
    private boolean hasCamera() { //
        return (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY));
    }

    public void startRecording(View view) { //
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_CAPTURE);
    }


    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

//        videoFileUri = data.getData();
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video saved to:\n" +  videoFileUri, Toast.LENGTH_SHORT).show();
                videoFileUri = data.getData();
                playVideoButton.setEnabled(true);
                String tag = "InsideResultCode";
                Log.d(tag, videoFileUri.toString());
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to record video", Toast.LENGTH_SHORT).show();
            }
        }

        if (resultCode == RESULT_OK && requestCode == VIDEO_CAPTURED) {
            playVideoButton.setEnabled(true);
            videoFileUri = data.getData();
            String tag = "InsideResultCode";
            Log.d(tag, "STRING HERE: " + videoFileUri.toString());

            Uri videoUri = data.getData();
            fileName = getPath(this, videoUri);
            Toast.makeText(this,"STRING HERE: " + videoFileUri.toString(),Toast.LENGTH_LONG).show();
        }


        Log.d(LOG_TAG, "onActivityForResult");
        if (resultCode == RESULT_OK && requestCode == PICK_VIDEO) {
            Uri videoUri = data.getData();
            String path = getPath(this,videoUri);
            fileName = path;
            String outputFileLocation = processVideo(path);
            Toast.makeText(this, "Video saved to:\n" +  outputFileLocation, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        if (v == captureVideoButton) {
            Intent captureVideoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
            startActivityForResult(captureVideoIntent, VIDEO_CAPTURED);
        }
        if (v == analyzeVideoButton) {
            playVideoButton.setEnabled(false);
            Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
            mediaChooser.setType("video/*");
            startActivityForResult(mediaChooser, PICK_VIDEO);


            videoThread = new VideoDetectorThread(fileName, this);

        } else if (v == playVideoButton) {
            videoView.setVideoURI(videoFileUri);
            videoView.start();
        }
    }



//========== implementing VideoDetectorThread altered from Affectiva video analysis example=======//

    public String processVideo(String filename) {
        videoThread = new VideoDetectorThread(filename,this);
        videoThread.start();
        return videoThread.getOutputFileLocation();
    }



    /**
     * Get path to video file
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

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
                final String[] selectionArgs = new String[] {
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

        return null;
    }

    /**
     * return data column from selection by user, to get video file metadata
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
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
     * checks Uri, returns whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * checks Uri, returns whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * checks Uri whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}