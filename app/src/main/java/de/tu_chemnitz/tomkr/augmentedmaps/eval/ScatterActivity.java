package de.tu_chemnitz.tomkr.augmentedmaps.eval;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;

public class ScatterActivity extends Activity implements OrientationListener, Handler.Callback{
    private static final String TAG = ScatterActivity.class.getName();

    private Orientation orientation;
//    private OrientationService orientationService;
    private PlotView plotView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scatter);

        plotView = (PlotView) findViewById(R.id.plotview);

//        orientationService = new OrientationService(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        orientationService.registerListener(plotView);
//        orientationService.start();
    }

    @Override
    public void onPause() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        orientationService.unregisterListener(plotView);
//        orientationService.stop();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onOrientationChange(Orientation values) {

    }

    @Override
    public boolean handleMessage(Message message) {

//        plotView.addValue(orientation);
//        plotView.invalidate();

        return true;
    }

//    private class BackgroundThread extends Thread{
//
//        boolean stop = false;
//        Handler mainHandler;
//
//        public BackgroundThread(Handler.Callback callback){
//            mainHandler = new Handler(LooperThread.getMainLooper(), callback);
//        }
//
//        @Override
//        public void run() {
//            super.run();
//            while(!stop) {
//
//                mainHandler.sendMessage(Message.obtain());
//
//
//            }
//        }
//    }

}


