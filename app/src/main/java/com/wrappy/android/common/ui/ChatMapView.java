package com.wrappy.android.common.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.google.android.gms.maps.MapView;
import com.wrappy.android.R;


public class ChatMapView extends MapView {

    private RectF mRect = new RectF();
    private Path mPath = new Path();

    private float mCornerRadius = 0;

    public ChatMapView(Context context) {
        this(context, null);
    }

    public ChatMapView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ChatMapView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);

        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.ChatMapView, defStyle, 0);
        float radius = a.getDimension(R.styleable.ChatMapView_wr_mapCornerRadius, 0);
        mCornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                radius,
                getResources().getDisplayMetrics());
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRect.set(0, 0, right, bottom);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mPath.reset();
        int count = canvas.save();

        mPath.addRoundRect(mRect, mCornerRadius, mCornerRadius, Path.Direction.CW);

        canvas.clipPath(mPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(count);
    }
}
