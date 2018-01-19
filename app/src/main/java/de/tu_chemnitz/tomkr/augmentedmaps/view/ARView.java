package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.Const;
import de.tu_chemnitz.tomkr.augmentedmaps.core.Controller;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.OptFlowFeature;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 */

public class ARView extends View {
    private static final String TAG = ARView.class.getName();

    private List<Marker> markerDrawables;
    private OptFlowFeature[] features;

    public ARView(Context context) {
        super(context);
    }

    public ARView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ARView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        canvas.drawCircle(960, 540, 20, Const.paintStroke);

        if (markerDrawables != null) {
            synchronized (Controller.listLock) {
                for (Marker md : markerDrawables) {
                    md.setDisplaySize(width, height);
                    md.draw(canvas);
                }
            }
        }

        if (features != null) {
            for (OptFlowFeature f : features) {
                Paint paint;
                if (f.isReliable()) {
                    paint = Const.paintFill;
                } else {
                    paint = Const.paintFillRed;
                }
                canvas.drawCircle((float) f.getX(), (float) (f.getY()), 10, paint);
                canvas.drawLine((float) f.getOx(), (float) f.getOy(), (float) f.getX(), (float) f.getY(), paint);
            }
        }
    }

    public void setMarkerList(List<Marker> markerDrawables) {
        this.markerDrawables = markerDrawables;
    }

    public void setOptFlowFeaturesToDraw(OptFlowFeature[] features) {
        this.features = features;
    }
}
