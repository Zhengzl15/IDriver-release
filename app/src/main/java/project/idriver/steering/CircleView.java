package project.idriver.steering;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import project.idriver.R;


/**
 * Draws a simple white circle on which the numbers will be drawn.
 */
public class CircleView extends View {
    private static final String TAG = "CircleView";

    private final Paint mPaint = new Paint();
    private int mWhite;
    private int mBlack;
    private float mCircleRadiusMultiplier;
    //private float mAmPmCircleRadiusMultiplier;
    private boolean mIsInitialized;

    private boolean mDrawValuesReady;
    private int mXCenter;
    private int mYCenter;
    private int mCircleRadius;

    public CircleView(Context context) {
        super(context);

        Resources res = context.getResources();
        mWhite = res.getColor(R.color.white);
        mBlack = res.getColor(R.color.numbers_text_color);
        mPaint.setAntiAlias(true);

        mIsInitialized = false;
    }

    public void initialize(Context context) {
        if (mIsInitialized) {
            Log.e(TAG, "CircleView may only be initialized once.");
            return;
        }

        Resources res = context.getResources();
        {
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.circle_radius_multiplier));
            //mAmPmCircleRadiusMultiplier =
             //       Float.parseFloat(res.getString(R.string.ampm_circle_radius_multiplier));
        }

        mIsInitialized = true;
    }


    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        if (viewWidth == 0 || !mIsInitialized) {
            return;
        }

        if (!mDrawValuesReady) {
            mXCenter = getWidth() / 2;
            mYCenter = getHeight() / 2;
            mCircleRadius = (int) (Math.min(mXCenter, mYCenter) * mCircleRadiusMultiplier);

            mDrawValuesReady = true;
        }

        mPaint.setColor(mBlack);
        canvas.drawCircle(mXCenter, mYCenter, mCircleRadius, mPaint);

        mPaint.setColor(mWhite);
        canvas.drawCircle(mXCenter, mYCenter, mCircleRadius-30, mPaint);
    }
}