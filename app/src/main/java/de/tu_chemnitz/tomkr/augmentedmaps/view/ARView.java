package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.testframework.groundtruth.GTActivity;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class ARView extends View {
    private static final String TAG = ARView.class.getName();

    private List<MarkerDrawable> markerDrawables;

    public ARView(Context context) {
        super(context);
        init(null, 0);
    }

    public ARView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ARView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");

        int width = getWidth();
        int height = getHeight();
        Log.d(TAG, "Size: " + width + "___" + height);

        for(MarkerDrawable md : markerDrawables){
            Log.d(TAG, "Draw Marker " + md);
            md.draw(canvas);
        }
    }

    public void setMarkerListRef(List<MarkerDrawable> markerDrawables){
        this.markerDrawables = markerDrawables;
    }

}
