package com.example.kdar.rollcall2.view;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;

public class CameraView extends JavaCameraView {

    private static final String TAG = "Sample::CameraView";

    public CameraView(Context context, AttributeSet cameraId) {
        super(context, cameraId);
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public void setCamFront() {
        disconnectCamera();
        setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT % numberCameras());
        connectCamera(getWidth(), getHeight());
    }

    public void setCamBack() {
        disconnectCamera();
        setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK % numberCameras());
        connectCamera(getWidth(), getHeight());
    }

    public int numberCameras() {
        return Camera.getNumberOfCameras();
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

}
