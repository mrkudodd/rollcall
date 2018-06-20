package com.example.kdar.rollcall2.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.kdar.rollcall2.R;
import com.example.kdar.rollcall2.model.Classes;
import com.example.kdar.rollcall2.model.Student;
import com.example.kdar.rollcall2.utils.GlobalHelper;
import com.example.kdar.rollcall2.utils.Labels;
import com.example.kdar.rollcall2.utils.PersonRecognizer;
import com.example.kdar.rollcall2.utils.PreferenceHelper;
import com.example.kdar.rollcall2.view.CameraView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecognizeActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    @BindView(R.id.cameraview_recognize)
    CameraView cameraview_recognize;
    @BindView(R.id.results)
    TextView results;
    @BindView(R.id.btnScan)
    ToggleButton btnScan;
    @BindView(R.id.tool_bar)
    Toolbar tool_bar;

    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;
    public static final int SEARCHING = 1;
    public static final int IDLE = 2;
    private int faceState = IDLE;
    private int numberOfCameras = Camera.getNumberOfCameras();
    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private int mLikely = 999;
    String mPath = "";
    private boolean mIsFrontCamera = false;
    Bitmap mBitmap;
    Handler mHandler;

    private PreferenceHelper preferenceHelper;
    PersonRecognizer fr;
    Labels labelsFile;

    static {
        OpenCVLoader.initDebug();
        System.loadLibrary("opencv_java");
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);
        preferenceHelper = new PreferenceHelper(getApplicationContext(), GlobalHelper.PREFERENCE_NAME_ROLLCALL);
        ButterKnife.bind(this);

        setSupportActionBar(tool_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        cameraview_recognize.setCvCameraViewListener(this);

        mPath = Environment.getExternalStorageDirectory() + "/rollcall/";

        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        labelsFile = new Labels(mPath);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String tempName = msg.obj.toString();
//                if (!(tempName.equals("Unknown"))) {
                btnScan.setChecked(false);
//                    results.setText(tempName);
                showResult(tempName);
//                }
//                else {
//                    btnScan.setChecked(false);
//                    results.setText(tempName);
//                }
            }
        };

        btnScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!fr.canPredict()) {
                        btnScan.setChecked(false);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.ScanntoPredict), Toast.LENGTH_LONG).show();
                        return;
                    }
                    faceState = SEARCHING;
                } else {
                    faceState = IDLE;
                }
            }
        });

        boolean success = (new File(mPath)).mkdirs();
        if (!success) {
            Log.e("Error", "Error creating directory");
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.menu_camera, menu);
//
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
//            case R.id.switchCam:
//                if (numberOfCameras == 1) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setTitle("Switch Camera").setMessage("Your device have one camera").setNeutralButton("Close", null);
//                    AlertDialog alert = builder.create();
//                    alert.show();
//                    return true;
//                }
//                mIsFrontCamera = !mIsFrontCamera;
//                if (mIsFrontCamera)
//                    cameraview_recognize.setCamFront();
//                else cameraview_recognize.setCamBack();
//                return true;
        }
        return true;
    }

    public RecognizeActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        if (mIsFrontCamera) {
            Core.flip(mRgba, mRgba, 1);
        }
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT| Objdetect.CASCADE_SCALE_IMAGE, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        } else if (mDetectorType == NATIVE_DETECTOR) {
            /*if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);*/
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();

        if ((facesArray.length > 0) && (faceState == SEARCHING)) {
            Mat m;
            m = mGray.submat(facesArray[0]);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);
            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            //mHandler.sendMessage(msg);

            textTochange = fr.predict(m);
            mLikely = fr.getProb();
            msg = new Message();
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

        }
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    fr = new PersonRecognizer(mPath);
                    fr.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        mJavaDetector.load(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    cameraview_recognize.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;


            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraview_recognize != null)
            cameraview_recognize.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraview_recognize.disableView();
    }

    public void showResult(String number) {
        String name = "Không tìm thấy";
        int position = -1;
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_result, null);

        String jsonClass = preferenceHelper.getClassList();
        Type listType = new TypeToken<ArrayList<Classes>>() {}.getType();
        final List<Classes> classList = new Gson().fromJson(jsonClass, listType);
        Classes classes = classList.get(preferenceHelper.getClassPosition());
        final ArrayList<Student> students = classes.getStudents();
        for (Student student:students){
            position ++;
            if (student.getNumber().equals(number)){
                name = student.getName();
                break;
            }
        }

        final TextView tvResult = alertLayout.findViewById(R.id.tvResult);
        tvResult.setText(name);
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Kết quả");
        builder.setView(alertLayout);
        builder.setCancelable(false);
        builder.setNegativeButton("Thử lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final int finalPosition = position;
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                students.get(finalPosition).setRolled(true);
                preferenceHelper.setClassList(new Gson().toJson(classList));
                Toast.makeText(getApplicationContext(), "Thành công!", Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
