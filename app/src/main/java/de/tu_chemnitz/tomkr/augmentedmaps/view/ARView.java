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
 * Created by Tom Kretzschmar on 21.10.2017.<br>
 * <br>
 * View class which displays {@link Marker} objects.<br>
 * Further for debug or testing purpose, {@link OptFlowFeature} objects can be displayed too.
 */

public class ARView extends View {
    /**
     * Tag for logging
     */
    private static final String TAG = ARView.class.getName();

    /**
     * List of markers representing MapNodes in ar, which are displayed.
     */
    private List<Marker> markerDrawables;

    /**
     * List of tracked features to be displayed.
     */
    private OptFlowFeature[] features;

    /**
     * Standard view constructor. Only call to super().<br>
     * See {@link View}
     */
    public ARView(Context context) {
        super(context);
    }

    /**
     * Standard view constructor. Only call to super().<br>
     * See {@link View}
     */
    public ARView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Standard view constructor. Only call to super().<br>
     * See {@link View}
     */
    public ARView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Draw Marker and OptFlowFeatures to given canvas.
     * @param canvas The canvas to draw to.
     */
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

    /**
     * Set the list of markers which should be displayed.
     * @param markerDrawables The markers to display.
     */
    public void setMarkerList(List<Marker> markerDrawables) {
        this.markerDrawables = markerDrawables;
    }

    /**
     * Set the list of features which should be displayed.
     * @param features The features to display.
     */
    public void setOptFlowFeaturesToDraw(OptFlowFeature[] features) {
        this.features = features;
    }
}
