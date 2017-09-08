package de.tu_chemnitz.tomkr.augmentedmaps.legacy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.ImageReader;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.R;

/**
 * Created by Tom Kretzschmar on 03.09.2017.
 *
 */
public class GTActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GTActivity";

    private CameraService cameraService;
    private ImageReader imageReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gtactivity_legacy);

        SurfaceView gtCaptureView = (SurfaceView) findViewById(R.id.gtCaptureView);
        Button captureButton =(Button) findViewById(R.id.captureBtn);
        captureButton.setOnClickListener(this);

        List<Surface> targets = new ArrayList<>();
        targets.add(gtCaptureView.getHolder().getSurface());
//        imageReader = ImageReader.newInstance(gtCaptureView.getWidth(), gtCaptureView.getHeight(), ImageFormat.JPEG, 1);
//        targets.add(imageReader.getSurface());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }

        cameraService = new CameraService(this, targets);

    }

    @Override
    public void onClick(View view) {
        if(cameraService != null){
//            cameraService.capture();
            Log.i(TAG, "CAPTURE");
        }
    }
}
