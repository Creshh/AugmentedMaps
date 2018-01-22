package de.tu_chemnitz.tomkr.augmentedmaps.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


import static android.hardware.camera2.CameraCharacteristics.LENS_FACING;


/**
 * Created by Tom Kretzschmar on 18.10.2017.<br>
 * <br>
 * A Camera2 implementation. Used for displaying a preview and sending images for processing using an image reader.<br>
 * Only Landscape Mode possible!
 */
@SuppressWarnings("ConstantConditions")
public class Camera2 {
    /**
     * Tag for logging
     */
    private static final String TAG = Camera2.class.getName();

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    /**
     * Constant for targeted preview width
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Constant for targeted preview height
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * FOV readings from camera instance
     */
    public static float[] fov;

    /**
     * The id of the camera device used
     */
    private String cameraId;

    /**
     * The current capture session
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * The camera device instance
     */
    private CameraDevice mCameraDevice;

    /**
     * The acutal preview image size
     */
    private Size mPreviewSize;

    /**
     * A background thread which handles the camera results
     */
    private HandlerThread mBackgroundThread;

    /**
     * A message handler for the background thread
     */
    private Handler mBackgroundHandler;

    /**
     * The Imagereader instance used for acquiring images for processing
     */
    private ImageReader mImageReader;

    /**
     * The request builder used for the preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * The preview request
     */
    private CaptureRequest mPreviewRequest;

    /**
     * A Semaphore used for synchronizing the camera open and close sequences
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * A TextureView on which the preview will be displayed.
     */
    private TextureView previewTarget;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * The discplay of the user device, used for calculating sizes and fov
     */
    private Display display;

    /**
     * The system camera manager instance
     */
    private CameraManager manager;

    /**
     * A Listener instance which handles new images gained with imagereader
     */
    private ImageReader.OnImageAvailableListener onImageAvailableListener;

    /**
     * Default Camera2 Constructor attempts to use the backfacing camera.
     *
     * @param previewTarget TextureView where the camera preview will be shown.
     * @param context       The Activity Context, from where this is called.
     * @param display       The Display of the device on which the preview will be shown.
     */
    public Camera2(TextureView previewTarget, Context context, Display display) {
        this(previewTarget, context, display, CameraMetadata.LENS_FACING_BACK);
    }

    /**
     * Private Camera2 Constructor using the given lensFacing for acquiring the camera.
     *
     * @param previewTarget TextureView where the camera preview will be shown.
     * @param context       The Activity Context, from where this is called.
     * @param display       The Display of the device on which the preview will be shown.
     */
    private Camera2(TextureView previewTarget, Context context, Display display, final int lensFacing) {
        this.previewTarget = previewTarget;
        this.display = display;
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics;
        try {
            for (String id : manager.getCameraIdList()) {
                characteristics = manager.getCameraCharacteristics(id);
                if (characteristics.get(LENS_FACING) == lensFacing) {
                    cameraId = id;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        calculateFOV();
    }

    /**
     * Register the listener for he imageReader
     * @param onImageAvailableListener The listener which gets notified on new available images
     */
    public void registerImageAvailableListener(ImageReader.OnImageAvailableListener onImageAvailableListener) {
        this.onImageAvailableListener = onImageAvailableListener;
    }


    /**
     * Start the preview and acquisition of images
     */
    public void startService() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        setupCamera();
    }

    /**
     * Stops the preview and acquisition of images
     */
    public void stopService() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.abortCaptures();
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }

        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configure and open the camera depending on state of preview target
     */
    private void setupCamera() {
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).

        if (previewTarget.isAvailable()) {
            openCamera(previewTarget.getWidth(), previewTarget.getHeight());
        } else {
            previewTarget.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    /**
     * Configure and open the camera device.
     * @param width the targeted width of the output
     * @param height the targeted height of the output
     */
    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(cameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException | SecurityException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: " + width + "-" + height);
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: " + width + "-" + height);
            Log.d(TAG, "onSurfaceTextureSizeChanged previewSize: " + mPreviewSize.getWidth() + "-" + mPreviewSize.getHeight());
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;

//            self.finish();
        }

    };


    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int width, int height) {
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                return;
            }

            mImageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 10);
            mImageReader.setOnImageAvailableListener(onImageAvailableListener, mBackgroundHandler);

            // Find out if we need to swap dimension to get the preview size relative to sensor coordinate.
            int displayRotation = display.getRotation();
            //noinspection ConstantConditions
            Integer mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e(TAG, "Display rotation is invalid: " + displayRotation);
            }

            Point displaySize = new Point();
            display.getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;


            if (swappedDimensions) {
                //noinspection SuspiciousNameCombination
                rotatedPreviewWidth = height;
                //noinspection SuspiciousNameCombination
                rotatedPreviewHeight = width;
                //noinspection SuspiciousNameCombination
                maxPreviewWidth = displaySize.y;
                //noinspection SuspiciousNameCombination
                maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }
//                mPreviewSize = CameraHelpers.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
            mPreviewSize = CameraHelpers.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, new Size(displaySize.x, displaySize.y));


        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG, "Currently an NPE is thrown when the Camera2API is used but not supported on the device this code runs.");
        }
    }


    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = previewTarget.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());

            // Here, we create a CameraCaptureSession for camera preview.
            List<Surface> targets = new ArrayList<>();
            targets.add(surface);
            targets.add(mImageReader.getSurface());
            mCameraDevice.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // The camera is already closed
                    if (null == mCameraDevice) {
                        return;
                    }

                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = cameraCaptureSession;
                    try {
                        // Auto focus should be continuous for camera preview.
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                        // Flash disabled
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);

                        // Finally, we start displaying the camera preview.
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    showToast("ConfigureFailed");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Log.d(TAG, "configureTransform: " + viewWidth + "-" + viewHeight);
        int rotation = display.getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        previewTarget.setTransform(matrix);
    }

    /**
     * Calculates the field of view in degree of the currently used camera device
     */
    public void calculateFOV() {
        CameraCharacteristics characteristics;
        try {
            characteristics = manager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
        float[] maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        SizeF size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        float w = size.getWidth();
        float h = size.getHeight();
        float horizontalAngle = (float) Math.toDegrees(2 * Math.atan(w / (maxFocus[0] * 2)));
        float verticalAngle = (float) Math.toDegrees(2 * Math.atan(h / (maxFocus[0] * 2)));

        Log.d(TAG, "FOV => " + horizontalAngle + "x" + verticalAngle);
        fov = new float[]{horizontalAngle, verticalAngle};
    }
}
