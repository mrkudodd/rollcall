package com.example.kdar.rollcall2.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
import org.opencv.core.CvType;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrainingActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    @BindView(R.id.tool_bar)
    Toolbar tool_bar;
    @BindView(R.id.cameraview_training)
    CameraView cameraview_training;
    @BindView(R.id.ivPreview)
    ImageView ivPreview;
    @BindView(R.id.btnCapture)
    ToggleButton btnCapture;

    private static final String TAG = "Training_Activity";
    public static final int IDLE = 2;
    public static final int TRAINING = 0;
    public static final long MAXIMG = 10;
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private int faceState = IDLE;
    private int numberOfCameras = Camera.getNumberOfCameras();
    private int countImages = 0;
    private int mAbsoluteFaceSize = 0;
    private int mDetectorType = JAVA_DETECTOR;
    private float mRelativeFaceSize = 0.2f;
    private String mPath = "";
    private String name;
    private String number;

    private Labels labelsFile;
    private Handler mHandler;
    private Bitmap mBitmap;
    private Mat mRgba;
    private Mat mGray;
    private CascadeClassifier mJavaDetector;
    private PersonRecognizer fr;
    private String[] mDetectorName;
    private File mCascadeFile;
    private boolean mIsFrontCamera = false;
    private PreferenceHelper preferenceHelper;
    private int i = 0;

    static {
        OpenCVLoader.initDebug();
        System.loadLibrary("opencv_java");
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        preferenceHelper = new PreferenceHelper(getApplicationContext(), GlobalHelper.PREFERENCE_NAME_ROLLCALL);
        ButterKnife.bind(this);

        name = getIntent().getStringExtra("name");
        number = getIntent().getStringExtra("number");

        setSupportActionBar(tool_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnCapture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                captureOnClick();
            }
        });

        cameraview_training.setCvCameraViewListener(this);

        mPath = Environment.getExternalStorageDirectory() + "/rollcall/";

        loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        labelsFile = new Labels(mPath);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj == "IMG") {
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(mBitmap);
                    ivPreview.setRotation(90);
                    ivPreview.setImageBitmap(mBitmap);
                    if (countImages >= MAXIMG - 1) {
                        btnCapture.setChecked(false);
                        captureOnClick();
                    }
                }
            }
        };

        boolean success = (new File(mPath)).mkdirs();
        if (!success)
            Log.e("Error", "Error creating directory");
    }

    public TrainingActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native";
    }

    private void captureOnClick() {
        if (btnCapture.isChecked())
            faceState = TRAINING;
        else {
            i++;
            setResult(RESULT_OK);
            countImages = 0;
            faceState = IDLE;
            if (i == 1) {
                addInfo();
                onBackPressed();
            }
        }
    }

    private void addInfo() {
        String jsonClass = preferenceHelper.getClassList();
        Type listType = new TypeToken<ArrayList<Classes>>() {
        }.getType();
        List<Classes> classList = new Gson().fromJson(jsonClass, listType);
        int position = preferenceHelper.getClassPosition();
        if (classList.get(position).getStudents() == null){
            ArrayList<Student> students = new ArrayList<>();
            students.add(new Student(name,number,false));
            classList.get(position).setStudents(students);
        }else {
            classList.get(position).getStudents().add(new Student(name,number,false));
        }
        preferenceHelper.setClassList(new Gson().toJson(classList));
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
//                    cameraview_training.setCamFront();
//                else cameraview_training.setCamBack();
//                return true;
        }
        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat(height, width, CvType.CV_8UC4);
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
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        if (mIsFrontCamera) {
            Core.flip(mRgba, mRgba, 1);
        }

        MatOfRect faces = new MatOfRect();
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.15, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT| Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        } else if (mDetectorType == NATIVE_DETECTOR) {

        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] faces_array = faces.toArray();
        if ((faces_array.length == 1) && (faceState == TRAINING) && (countImages < MAXIMG) && (!number.equals(""))) {
            Mat m;
            Rect r = faces_array[0];
            m = mRgba.submat(r);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

            Utils.matToBitmap(m, mBitmap);

            Message msg = new Message();
            String text = "IMG";
            msg.obj = text;
            mHandler.sendMessage(msg);
            if (countImages < MAXIMG) {
                fr.add(m, number);
                countImages++;
            }
        }
        for (int i = 0; i < faces_array.length; i++) {
            Core.rectangle(mRgba, faces_array[i].tl(), faces_array[i].br(), FACE_RECT_COLOR, 3);
        }
        return mRgba;
    }

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
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
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
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
                    cameraview_training.enableView();
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
        loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraview_training != null)
            cameraview_training.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraview_training.disableView();
    }
}
