package project.idriver.steering;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import com.nineoldandroids.animation.Keyframe;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;

import project.idriver.R;


/**
 * View to show what number is selected. This will draw a blue circle over the number, with a blue
 * line coming from the center of the main circle to the edge of the blue selection.
 */
public class RadialSelectorView extends View {
    private static final String TAG = "RadialSelectorView";

    private final Paint mPaint = new Paint();

    private boolean mIsInitialized;
    private boolean mDrawValuesReady;

    private float mCircleRadiusMultiplier;
    private float mInnerNumbersRadiusMultiplier;
    private float mOuterNumbersRadiusMultiplier;
    private float mNumbersRadiusMultiplier;
    private float mSelectionRadiusMultiplier;
    private float mAnimationRadiusMultiplier;
    private boolean mHasInnerCircle;

    private int mXCenter;
    private int mYCenter;
    private int mCircleRadius;
    private float mTransitionMidRadiusMultiplier;
    private float mTransitionEndRadiusMultiplier;
    private int mLineLength;
    private int mSelectionRadius;
    private InvalidateUpdateListener mInvalidateUpdateListener;

    private int mSelectionDegrees;
    private double mSelectionRadians;
    private boolean mForceDrawDot;
    private int totalDegree;

    public RadialSelectorView(Context context) {
        super(context);
        mIsInitialized = false;
    }

    public void initialize(Context context, boolean hasInnerCircle,
                           boolean disappearsOut, int selectionDegrees, boolean isInnerCircle) {
        if (mIsInitialized) {
            Log.e(TAG, "This RadialSelectorView may only be initialized once.");
            return;
        }

        Resources res = context.getResources();

        int blue = res.getColor(R.color.blue);
        int red = res.getColor(R.color.red);
        mPaint.setColor(red);
        mPaint.setAntiAlias(true);

        mCircleRadiusMultiplier = 0.85f;


        // Calculate values for the radius size(s) of the numbers circle(s).
        mHasInnerCircle = hasInnerCircle;
        if (hasInnerCircle) {
            mInnerNumbersRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.numbers_radius_multiplier_inner));
            mOuterNumbersRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.numbers_radius_multiplier_outer));
        } else {
            mNumbersRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.numbers_radius_multiplier_normal));
        }
        mSelectionRadiusMultiplier =
                Float.parseFloat("0.14");

        // Calculate values for the transition mid-way states.
        mAnimationRadiusMultiplier = 1;
        mTransitionMidRadiusMultiplier = 1f + (0.05f * (disappearsOut ? -1 : 1));
        mTransitionEndRadiusMultiplier = 1f + (0.3f * (disappearsOut ? 1 : -1));
        mInvalidateUpdateListener = new InvalidateUpdateListener();

        setSelection(selectionDegrees, isInnerCircle, false);
        mIsInitialized = true;
    }

    public void setSelection(int selectionDegrees, boolean isInnerCircle, boolean forceDrawDot) {
        mSelectionDegrees = selectionDegrees;
        Log.i("Selection", "" + selectionDegrees);
        mSelectionRadians = selectionDegrees * Math.PI / 180;
        mForceDrawDot = forceDrawDot;

        if (mHasInnerCircle) {
            if (isInnerCircle) {
                mNumbersRadiusMultiplier = mInnerNumbersRadiusMultiplier;
            } else {
                mNumbersRadiusMultiplier = mOuterNumbersRadiusMultiplier;
            }
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    /**
     * Set the multiplier for the radius. Will be used during animations to move in/out.
     */
    public void setAnimationRadiusMultiplier(float animationRadiusMultiplier) {
        mAnimationRadiusMultiplier = animationRadiusMultiplier;
    }

    public int getDegreesFromCoords(float pointX, float pointY, boolean forceLegal,
                                    final Boolean[] isInnerCircle) {
        if (!mDrawValuesReady) {
            return -1;
        }

        double hypotenuse = Math.sqrt(
                (pointY - mYCenter) * (pointY - mYCenter) +
                        (pointX - mXCenter) * (pointX - mXCenter));
        // Check if we're outside the range
        if (mHasInnerCircle) {
            if (forceLegal) {
                // If we're told to force the coordinates to be legal, we'll set the isInnerCircle
                // boolean based based off whichever number the coordinates are closer to.
                int innerNumberRadius = (int) (mCircleRadius * mInnerNumbersRadiusMultiplier);
                int distanceToInnerNumber = (int) Math.abs(hypotenuse - innerNumberRadius);
                int outerNumberRadius = (int) (mCircleRadius * mOuterNumbersRadiusMultiplier);
                int distanceToOuterNumber = (int) Math.abs(hypotenuse - outerNumberRadius);

                isInnerCircle[0] = (distanceToInnerNumber <= distanceToOuterNumber);
            } else {
                // Otherwise, if we're close enough to either number (with the space between the
                // two allotted equally), set the isInnerCircle boolean as the closer one.
                // appropriately, but otherwise return -1.
                int minAllowedHypotenuseForInnerNumber =
                        (int) (mCircleRadius * mInnerNumbersRadiusMultiplier) - mSelectionRadius;
                int maxAllowedHypotenuseForOuterNumber =
                        (int) (mCircleRadius * mOuterNumbersRadiusMultiplier) + mSelectionRadius;
                int halfwayHypotenusePoint = (int) (mCircleRadius *
                        ((mOuterNumbersRadiusMultiplier + mInnerNumbersRadiusMultiplier) / 2));

                if (hypotenuse >= minAllowedHypotenuseForInnerNumber &&
                        hypotenuse <= halfwayHypotenusePoint) {
                    isInnerCircle[0] = true;
                } else if (hypotenuse <= maxAllowedHypotenuseForOuterNumber &&
                        hypotenuse >= halfwayHypotenusePoint) {
                    isInnerCircle[0] = false;
                } else {
                    return -1;
                }
            }
        } else {
            // If there's just one circle, we'll need to return -1 if:
            // we're not told to force the coordinates to be legal, and
            // the coordinates' distance to the number is within the allowed distance.
            if (!forceLegal) {
                int distanceToNumber = (int) Math.abs(hypotenuse - mLineLength);
                // The max allowed distance will be defined as the distance from the center of the
                // number to the edge of the circle.
                int maxAllowedDistance = (int) (mCircleRadius * (1 - mNumbersRadiusMultiplier));
                if (distanceToNumber > maxAllowedDistance) {
                    return -1;
                }
            }
        }


        float opposite = Math.abs(pointY - mYCenter);
        double radians = Math.asin(opposite / hypotenuse);
        int degrees = (int) (radians * 180 / Math.PI);

        // Now we have to translate to the correct quadrant.
        boolean rightSide = (pointX > mXCenter);
        boolean topSide = (pointY < mYCenter);
        if (rightSide && topSide) {
            degrees = 90 - degrees;
        } else if (rightSide && !topSide) {
            degrees = 90 + degrees;
        } else if (!rightSide && !topSide) {
            degrees = 270 - degrees;
        } else if (!rightSide && topSide) {
            degrees = 270 + degrees;
        }
        return degrees;
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

            mSelectionRadius = (int) (mCircleRadius * mSelectionRadiusMultiplier);

            mDrawValuesReady = true;
        }

        // Calculate the current radius at which to place the selection circle.
        mLineLength = (int) (mCircleRadius * mNumbersRadiusMultiplier * mAnimationRadiusMultiplier);
        int pointX = mXCenter + (int) (mLineLength * Math.sin(mSelectionRadians));
        int pointY = mYCenter - (int) (mLineLength * Math.cos(mSelectionRadians));

        // Draw the selection circle.
        mPaint.setAlpha(255);
        canvas.drawCircle(pointX, pointY, mSelectionRadius, mPaint);

        if (mForceDrawDot | mSelectionDegrees % 30 != 0) {
            // We're not on a direct tick (or we've been told to draw the dot anyway).
            mPaint.setAlpha(180);
            //canvas.drawCircle(pointX, pointY, (mSelectionRadius * 2 / 7), mPaint);
        } else {
            // We're not drawing the dot, so shorten the line to only go as far as the edge of the
            // selection circle.
            int lineLength = mLineLength;
            lineLength -= mSelectionRadius;
            pointX = mXCenter + (int) (lineLength * Math.sin(mSelectionRadians));
            pointY = mYCenter - (int) (lineLength * Math.cos(mSelectionRadians));
        }

        // Draw the line from the center of the circle.
        mPaint.setAlpha(255);
        mPaint.setStrokeWidth(1);
        //canvas.drawLine(mXCenter, mYCenter, pointX, pointY, mPaint);
    }

    public ObjectAnimator getDisappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady) {
            Log.e(TAG, "RadialSelectorView was not ready for animation.");
            return null;
        }

        Keyframe kf0, kf1, kf2;
        float midwayPoint = 0.2f;
        int duration = 500;

        kf0 = Keyframe.ofFloat(0f, 1);
        kf1 = Keyframe.ofFloat(midwayPoint, mTransitionMidRadiusMultiplier);
        kf2 = Keyframe.ofFloat(1f, mTransitionEndRadiusMultiplier);
        PropertyValuesHolder radiusDisappear = PropertyValuesHolder.ofKeyframe(
                "animationRadiusMultiplier", kf0, kf1, kf2);

        kf0 = Keyframe.ofFloat(0f, 1f);
        kf1 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder fadeOut = PropertyValuesHolder.ofKeyframe("alpha", kf0, kf1);

        ObjectAnimator disappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusDisappear, fadeOut).setDuration(duration);
        disappearAnimator.addUpdateListener(mInvalidateUpdateListener);

        return disappearAnimator;
    }

    public ObjectAnimator getReappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady) {
            Log.e(TAG, "RadialSelectorView was not ready for animation.");
            return null;
        }

        Keyframe kf0, kf1, kf2, kf3;
        float midwayPoint = 0.2f;
        int duration = 500;

        // The time points are half of what they would normally be, because this animation is
        // staggered against the disappear so they happen seamlessly. The reappear starts
        // halfway into the disappear.
        float delayMultiplier = 0.25f;
        float transitionDurationMultiplier = 1f;
        float totalDurationMultiplier = transitionDurationMultiplier + delayMultiplier;
        int totalDuration = (int) (duration * totalDurationMultiplier);
        float delayPoint = (delayMultiplier * duration) / totalDuration;
        midwayPoint = 1 - (midwayPoint * (1 - delayPoint));

        kf0 = Keyframe.ofFloat(0f, mTransitionEndRadiusMultiplier);
        kf1 = Keyframe.ofFloat(delayPoint, mTransitionEndRadiusMultiplier);
        kf2 = Keyframe.ofFloat(midwayPoint, mTransitionMidRadiusMultiplier);
        kf3 = Keyframe.ofFloat(1f, 1);
        PropertyValuesHolder radiusReappear = PropertyValuesHolder.ofKeyframe(
                "animationRadiusMultiplier", kf0, kf1, kf2, kf3);

        kf0 = Keyframe.ofFloat(0f, 0f);
        kf1 = Keyframe.ofFloat(delayPoint, 0f);
        kf2 = Keyframe.ofFloat(1f, 1f);
        PropertyValuesHolder fadeIn = PropertyValuesHolder.ofKeyframe("alpha", kf0, kf1, kf2);

        ObjectAnimator reappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusReappear, fadeIn).setDuration(totalDuration);
        reappearAnimator.addUpdateListener(mInvalidateUpdateListener);
        return reappearAnimator;
    }

    /**
     * We'll need to invalidate during the animation.
     */
    private class InvalidateUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            RadialSelectorView.this.invalidate();
        }
    }
}