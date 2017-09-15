package de.tu_chemnitz.tomkr.augmentedmaps.testframework.groundtruth;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService;

import static android.R.attr.id;
import static de.tu_chemnitz.tomkr.augmentedmaps.R.id.preview;

public class GTActivity extends AppCompatActivity implements OrientationListener{

    private Size previewsize;
    private Size jpegSizes[] = null;


    private TextView orientationView;

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    private CameraManager manager;

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    Button getpicture;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private OrientationService orientationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gt);
        textureView = (TextureView) findViewById(preview);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        orientationView = (TextView) findViewById(R.id.orientation);

        getpicture = (Button) findViewById(R.id.captureBtn);
        getpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicture();
            }
        });

        orientationService = new OrientationService(this);
        orientationService.registerListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(textureView != null){
            try {
                setupCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            transformImage(textureView.getWidth(), textureView.getHeight());
        } else {
            // setTextureListener
        }
        
        orientationService.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraDevice!=null)
        {
            cameraDevice.close();
            Log.d("CAMERA", "CLOSE CAMERA############");

        }
        orientationService.stop();
    }
 

    void getPicture() {
        if (cameraDevice == null) {
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            int width = 640, height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder capturebuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            capturebuilder.addTarget(reader.getSurface());
            capturebuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            capturebuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (Exception ee) {
                        Log.e("A", ee.getMessage());
                    } finally {
                        if (image != null)
                            image.close();
                    }
                }

                void save(byte[] bytes) {
                    File file12 = getOutputMediaFile();
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file12);
                        outputStream.write(bytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (outputStream != null)
                                outputStream.close();
                        } catch (Exception e) {
                        }
                    }
                }
            };

            HandlerThread handlerThread = new HandlerThread("takepicture");
            handlerThread.start();

            final Handler handler = new Handler(handlerThread.getLooper());
            reader.setOnImageAvailableListener(imageAvailableListener, handler);

            final CameraCaptureSession.CaptureCallback previewSSession = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    startCamera();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {

                    try {
                        session.capture(capturebuilder.build(), previewSSession, handler);

                    } catch (Exception e) {

                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, handler);
        } catch (Exception e) {

        }
    }

    private String selectRearCamera(CameraManager manager) throws CameraAccessException {
        for(String id : manager.getCameraIdList()){
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
                return id;
            }
        }
        return null;
    }

    private Size selectOutputSize(CameraManager manager, String cameraId) throws CameraAccessException {
        StreamConfigurationMap map = manager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size targetSize = null;
        for(Size s : map.getOutputSizes(SurfaceTexture.class)){
            if(s.getHeight() == HEIGHT){
                if(s.getWidth() == WIDTH){
                    return s;
                } else {
                    targetSize = s;
                }
            }
            Log.d("SIZES", s.toString());
        }
        return targetSize;
    }

    public String setupCamera() throws CameraAccessException {

        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = selectRearCamera(manager);
        previewsize = selectOutputSize(manager, cameraId);
        return cameraId;
    }

    private void openCamera(String cameraId) throws CameraAccessException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions here to request the missing permissions, and then overriding public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) to handle the case where the user grants the permission. See the documentation for ActivityCompat#requestPermissions for more details.
            return;
        }

        if(cameraId != null && previewsize != null){

            manager.openCamera(cameraId, stateCallback, null);
        } else {
            Log.e("CAMERA", "No Camera or Size found");
        }

    }
    private TextureView.SurfaceTextureListener surfaceTextureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // setup Camera
            try {
                String id = setupCamera();
                transformImage(width, height);
                Log.d("SURFACEAVAILABLE", "W" + width + "|H" + height);

                openCamera(id);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private CameraDevice.StateCallback stateCallback=new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice=camera;
            startCamera();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.e("CAMERA", "CAMERA DEVICE DISCONNECTED #########");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e("CAMERA", "CAMERA DEVICE ERROR #########");
        }
    };



    void  startCamera(){
        if(cameraDevice==null||!textureView.isAvailable()|| previewsize==null)
        {
            return;
        }

        SurfaceTexture texture=textureView.getSurfaceTexture();
        if(texture==null)
        {
            return;
        }

        texture.setDefaultBufferSize(previewsize.getWidth(),previewsize.getHeight());
        Surface surface=new Surface(texture);

        try
        {
            previewBuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        }catch (Exception e)
        {
        }
        previewBuilder.addTarget(surface);
        try
        {
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    previewSession=session;
                    getChangedPreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            },null);
        }catch (Exception e)
        {

        }
    }

    void getChangedPreview()
    {
        if(cameraDevice==null)
        {
            return;
        }
        previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread=new HandlerThread("changed Preview");
        thread.start();
        Handler handler=new Handler(thread.getLooper());
        try
        {
            previewSession.setRepeatingRequest(previewBuilder.build(), null, handler);
        }catch (Exception e){}
    }



    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStorageDirectory(),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void transformImage(int width, int height){
        Log.d("SURFACEAVAILABLE", "W" + width + "|H" + height);
        if(previewsize == null || textureView == null)
            return;
        Matrix matrix = new Matrix();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0,0,width,height);
        RectF previewRectF = new RectF(0,0,previewsize.getHeight(), previewsize.getWidth());
        float centerX = textureRectF.centerX();
        float centerY = textureRectF.centerY();
        if(rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270){
            previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float)width / previewsize.getWidth(), (float)height / previewsize.getHeight());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    @Override
    public void onOrientationChange(Orientation values) {


        Log.d("ORIENTATION", "X" + values.toString());
        orientationView.setText(values.toString());
    }
}
