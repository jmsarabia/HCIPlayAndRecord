package com.example.hciplayandrecord;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.affectiva.android.affdex.sdk.detector.VideoFileDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * A thread to manage the VideoDetector.
 * Required since running the VideoDetector in the main thread will crash the application.
 */
public class VideoDetectorThread extends Thread implements Detector.ImageListener {

    private static String LOG_TAG = "Affectiva";
    private String filename;
    private VideoFileDetector detector;
    private Activity activity;
    private DrawingView drawingView;
    private MetricsPanel metricsPanel;
    private volatile boolean abortRequested;
    private Object completeSignal = new Object();

    private String outputFileLocation = "/storage/emulated/0/myDirectory/";



    public VideoDetectorThread(String file, Activity context) {
        filename = file;
        activity = context;

    }

    public String getOutputFileLocation(){
        return outputFileLocation;
    }
    @Override
    public void run() {
        detector = new VideoFileDetector(activity, filename, 1, Detector.FaceDetectorMode.LARGE_FACES);
        detector.setDetectAllEmotions(true);
        detector.setImageListener(this);
        try {
            detector.start();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (detector.isRunning()) {
                detector.stop();
            }
            // notify waiting threads that we're done
            synchronized (completeSignal) {
                completeSignal.notify();
            }
        }

    }

    @Override
    public void onImageResults(List<Face> list, Frame image, final float timestamp) {

        final Frame frame = image;
        final List<Face> faces = list;

        if (abortRequested) {
            detector.stop();
            abortRequested = false;
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @SuppressWarnings("SuspiciousNameCombination")
            @Override
            public void run() {
                //update metrics
                if (faces != null && faces.size() > 0) {
                    Face face = faces.get(0);
                    // process the numeric metrics (scored or measured)
                    Metrics[] allMetrics = Metrics.values();
                    for (int n = 0; n < Metrics.numberOfEmotions(); n++) {
                        Metrics metric = allMetrics[n];

                        // log out all the metrics and scores
                        float tmpScore = getScore(metric, face);
                        String tmpScoreStr = Float.toString(tmpScore);

                        //=========== writing to file txt file ===========//
                        writeToFile(tmpScoreStr, activity, metric);
                        Log.d(LOG_TAG, "Metric: "+ metric+"   Score: "+ tmpScoreStr);
                        // TODO: metricsPanel to be implemented in another activity - to improve appearance
//                        metricsPanel.setMetricValue(metric, getScore(metric, face));
                    }

                    // TODO: set the text for the appearance metrics for metricsPanel
                    int resId = 0;
//                    switch (face.appearance.getGender()) {
//                        case UNKNOWN:
//                            resId = R.string.unknown;
//                            break;
//                        case FEMALE:
//                            resId = R.string.gender_female;
//                            break;
//                        case MALE:
//                            resId = R.string.gender_male;
//                            break;
//                    }
//                    metricsPanel.setMetricText(Metrics.GENDER, resId);

//                    switch (face.appearance.getAge()) {
//                        case AGE_UNKNOWN:
//                            resId = R.string.unknown;
//                            break;
//                        case AGE_UNDER_18:
//                            resId = R.string.age_under_18;
//                            break;
//                        case AGE_18_24:
//                            resId = R.string.age_18_24;
//                            break;
//                        case AGE_25_34:
//                            resId = R.string.age_25_34;
//                            break;
//                        case AGE_35_44:
//                            resId = R.string.age_35_44;
//                            break;
//                        case AGE_45_54:
//                            resId = R.string.age_45_54;
//                            break;
//                        case AGE_55_64:
//                            resId = R.string.age_55_64;
//                            break;
//                        case AGE_65_PLUS:
//                            resId = R.string.age_65_plus;
//                            break;
//                    }
//                    metricsPanel.setMetricText(Metrics.AGE, resId);

//                    switch (face.appearance.getEthnicity()) {
//                        case UNKNOWN:
//                            resId = R.string.unknown;
//                            break;
//                        case CAUCASIAN:
//                            resId = R.string.ethnicity_caucasian;
//                            break;
//                        case BLACK_AFRICAN:
//                            resId = R.string.ethnicity_black_african;
//                            break;
//                        case EAST_ASIAN:
//                            resId = R.string.ethnicity_east_asian;
//                            break;
//                        case SOUTH_ASIAN:
//                            resId = R.string.ethnicity_south_asian;
//                            break;
//                        case HISPANIC:
//                            resId = R.string.ethnicity_hispanic;
//                            break;
//                    }
//                    metricsPanel.setMetricText(Metrics.ETHNICITY, resId);


                    PointF[] facePoints = face.getFacePoints();
                    int frameWidth = frame.getWidth();
                    int frameHeight = frame.getHeight();
                    Frame.ROTATE rotate = frame.getTargetRotation();

                    if (rotate == Frame.ROTATE.BY_90_CCW || rotate == Frame.ROTATE.BY_90_CW) {
                        int temp = frameWidth;
                        frameWidth = frameHeight;
                        frameHeight = temp;
                    }
//                    drawingView.drawFrame(frame, facePoints);
                } else {
                    for (Metrics metric : Metrics.values()) {
//                        metricsPanel.setMetricNA(metric);
                    }
//                    drawingView.drawFrame(frame, null);
                }

            }
        });

    }

    /**
     * If detection is in progress, stop detecting video
     */
    void abort() {
        if (isAlive()) {
            // set a flag which will be monitored in onImageResults
            abortRequested = true;

            // wait for background thread to finish before returning
            synchronized (completeSignal) {
                try {
                    completeSignal.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private float getScore(Metrics metric, Face face) {

        float score;

        switch (metric) {
            case ANGER:
                score = face.emotions.getAnger();
                break;
            case CONTEMPT:
                score = face.emotions.getContempt();
                break;
            case DISGUST:
                score = face.emotions.getDisgust();
                break;
            case FEAR:
                score = face.emotions.getFear();
                break;
            case JOY:
                score = face.emotions.getJoy();
                break;
            case SADNESS:
                score = face.emotions.getSadness();
                break;
            case SURPRISE:
                score = face.emotions.getSurprise();
                break;
            case ATTENTION:
                score = face.expressions.getAttention();
                break;
            case BROW_FURROW:
                score = face.expressions.getBrowFurrow();
                break;
            case BROW_RAISE:
                score = face.expressions.getBrowRaise();
                break;
            case CHEEK_RAISE:
                score = face.expressions.getCheekRaise();
                break;
            case CHIN_RAISER:
                score = face.expressions.getChinRaise();
                break;
            case DIMPLER:
                score = face.expressions.getDimpler();
                break;
            case ENGAGEMENT:
                score = face.emotions.getEngagement();
                break;
            case EYE_CLOSURE:
                score = face.expressions.getEyeClosure();
                break;
            case EYE_WIDEN:
                score = face.expressions.getEyeWiden();
                break;
            case INNER_BROW_RAISER:
                score = face.expressions.getInnerBrowRaise();
                break;
            case JAW_DROP:
                score = face.expressions.getJawDrop();
                break;
            case LID_TIGHTEN:
                score = face.expressions.getLidTighten();
                break;
            case LIP_DEPRESSOR:
                score = face.expressions.getLipCornerDepressor();
                break;
            case LIP_PRESS:
                score = face.expressions.getLipPress();
                break;
            case LIP_PUCKER:
                score = face.expressions.getLipPucker();
                break;
            case LIP_STRETCH:
                score = face.expressions.getLipStretch();
                break;
            case LIP_SUCK:
                score = face.expressions.getLipSuck();
                break;
            case MOUTH_OPEN:
                score = face.expressions.getMouthOpen();
                break;
            case NOSE_WRINKLER:
                score = face.expressions.getNoseWrinkle();
                break;
            case SMILE:
                score = face.expressions.getSmile();
                break;
            case SMIRK:
                score = face.expressions.getSmirk();
                break;
            case UPPER_LIP_RAISER:
                score = face.expressions.getUpperLipRaise();
                break;
            case VALENCE:
                score = face.emotions.getValence();
                break;
            case YAW:
                score = face.measurements.orientation.getYaw();
                break;
            case ROLL:
                score = face.measurements.orientation.getRoll();
                break;
            case PITCH:
                score = face.measurements.orientation.getPitch();
                break;
            case INTER_OCULAR_DISTANCE:
                score = face.measurements.getInterocularDistance();
                break;
            case BRIGHTNESS:
                score = face.qualities.getBrightness();
                break;
            default:
                score = Float.NaN;
                break;
        }
        return score;
    }

    //+++++++++++++++++ Writing to file code for analysis outside app +++++++++++++++++//
    /* Checks external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }



    /**
     * Takes video file, apply Affectiva to get data, and writes that output to text file with similar name
     * if metric is Joy, then make/append to happy file, otherwise make/append to sad file
     * File is written to a subdirectory created in the phone, "myDirectory" instead of the app data folders
    */
    private void writeToFile(String data, Context context, Metrics metric) {
//        byte[] br = new byte[0]; //used for printwriter and filewriter: turn strings to bytes, append
        if(isExternalStorageWritable()){
            try {

                // using substring functions - slightly faster than regex
                // to get the unique part of the file name to be used in naming the txt file

                String tmpFileName = filename.substring(filename.lastIndexOf("/") + 1);

                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/myDirectory" );
                dir.mkdir();
                outputFileLocation = dir.getAbsolutePath();
                data = data +",";
                boolean canAppend = true;
                if(metric == Metrics.JOY) {
                    // get the last part of for naming new txt file e.g. file/storage/emulated/0/DCIM/Camera/20190503_234551.mp4
                    tmpFileName = tmpFileName.replace(".", "") + "AnalyzeHappy.txt";
                    Log.d(LOG_TAG, "HAPPY TEXT FILE NAME: " + tmpFileName);
                    File outputFile = new File(dir,  tmpFileName);
                    FileOutputStream fw = new FileOutputStream(outputFile, canAppend);
                    Log.d(LOG_TAG, "Output: " + outputFile);
                    fw.write(data.getBytes());
                    outputFileLocation = outputFile.getAbsolutePath();
                    fw.close();
                }
                // otherwise, only analyzing sad
                else if(metric == Metrics.SADNESS){
                    tmpFileName = tmpFileName.replace(".", "") + "AnalyzeSad.txt";
                    Log.d(LOG_TAG, "SAD TEXT FILE NAME: " + tmpFileName);
                    File outputFile = new File(dir,  tmpFileName);
                    FileOutputStream fw = new FileOutputStream(outputFile, canAppend);
                    Log.d(LOG_TAG, "Output: " + outputFile);
                    fw.write(data.getBytes());
                    outputFileLocation = outputFile.getAbsolutePath();
                    fw.close();
                }
            }
            catch (Exception e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }
}