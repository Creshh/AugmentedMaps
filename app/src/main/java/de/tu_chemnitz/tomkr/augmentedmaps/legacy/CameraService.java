package de.tu_chemnitz.tomkr.augmentedmaps.legacy;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import java.util.List;

/**
 * Created by Tom Kretzschmar on 03.09.2017.
 *
 */

public class CameraService{
    private CameraCaptureSessionHandler sessionCallback;
    private CameraDeviceHandler deviceCallback;
    private CameraDevice cameraDevice;
    private List<Surface> targets;
    private CameraCaptureSession cameraCaptureSession;


    private String TAG = "CameraService";
    private CaptureRequest.Builder captureRequestBuilder;

    public CameraService(Context context, List<Surface> targets) {
        this.targets = targets;

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String camera = null;
        try {
            String cameras[] = manager.getCameraIdList();
            camera = cameras[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        deviceCallback = new CameraDeviceHandler(this);
        try {

            manager.openCamera(camera, deviceCallback, null);
        } catch (SecurityException | CameraAccessException  e) {
            Log.e("", e.getStackTrace().toString());
        }
    }

//    public Image capture(){
//        CaptureRequest request = new CaptureRequest();
//    }

    public void closeCamera(){
        if(cameraDevice != null){
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void updatePreview(){
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private class CameraDeviceHandler extends  CameraDevice.StateCallback{
        private CameraService service;

        private CameraDeviceHandler(CameraService service) {
            this.service = service;
        }

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            service.cameraDevice = cameraDevice;
            try {
                service.captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            service.captureRequestBuilder.addTarget(targets.get(0));
            service.sessionCallback = new CameraCaptureSessionHandler(service);
            try {
                cameraDevice.createCaptureSession(targets, sessionCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {}
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {}
    }

    private class CameraCaptureSessionHandler extends CameraCaptureSession.StateCallback{
        private CameraService service;

        private CameraCaptureSessionHandler(CameraService service) {
            this.service = service;
        }

        @Override
        public void onConfigured(@NonNull CameraCaptureSession captureSession) {
            //The camera is already closed
            if (null == cameraDevice) {
                return;
            }
            // When the session is ready, we start displaying the preview.
            cameraCaptureSession = captureSession;
            updatePreview();
        }
        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {}
    }

}

