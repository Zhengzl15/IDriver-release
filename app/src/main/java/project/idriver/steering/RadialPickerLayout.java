package project.idriver.steering;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.Timer;
import java.util.TimerTask;

import project.idriver.R;
import project.idriver.beans.AtosBean;
import project.idriver.beans.CommandBean;
import project.idriver.beans.GsonUtil;
import project.idriver.beans.MessageBean;
import project.idriver.nets.ZmqService;


public class RadialPickerLayout extends FrameLayout implements OnTouchListener {
    private static final String TAG = "RadialPickerLayout";

    private final int TOUCH_SLOP;
    private final int TAP_TIMEOUT;

    private static final int VISIBLE_DEGREES_STEP_SIZE = 6;

    private Vibrator mVibrator;
    private boolean mVibrate = true;
    private long mLastVibrate;

    private OnValueSelectedListener mListener;
    private int mCurrentValue;

    private CircleView mCircleView;
    private RadialTextsView mRadialTextsView;
    private RadialSelectorView mRadialSelectorView;

    private int currentDegree = 0;
    private int lastDegree = 0;
    private int totalValue = 0;
    int count = 0;
    long firClick = 0;
    long secClick = 0;

    private View mGrayBox;

    private int[] mSnapPrefer30sMap;
    private boolean mInputEnabled;
    private boolean mDoingMove;
    private boolean mDoingTouch;
    private int mDownDegrees;
    private float mDownX;
    private float mDownY;
    private AccessibilityManager mAccessibilityManager;

    private AnimatorSet mTransition;
    private Handler mHandler = new Handler();
    private Handler valueHandler;
    private ZmqService zmqService;
    private Handler zmqHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public interface OnValueSelectedListener {
        void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance);
    }

    public RadialPickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(this);
        ViewConfiguration vc = ViewConfiguration.get(context);
        TOUCH_SLOP = vc.getScaledTouchSlop();
        TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
        mDoingMove = false;

        mCircleView = new CircleView(context);
        addView(mCircleView);

        mRadialTextsView = new RadialTextsView(context);
        addView(mRadialTextsView);

        mRadialSelectorView = new RadialSelectorView(context);
        addView(mRadialSelectorView);

        // Prepare mapping to snap touchable degrees to selectable degrees.
        preparePrefer30sMap();

        mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        mLastVibrate = 0;

        mInputEnabled = true;
        mGrayBox = new View(context);
        mGrayBox.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mGrayBox.setBackgroundColor(getResources().getColor(R.color.transparent_black));
        mGrayBox.setVisibility(View.INVISIBLE);
        addView(mGrayBox);

        mAccessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

    }

    public void setHandler(Handler valueHandler) {
        this.valueHandler = valueHandler;
        timer = new Timer();
        timer.schedule(task, 10, 10);
    }

    public void stopTimer() {
        timer.cancel();
    }

    private Timer timer;
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            valueHandler.obtainMessage(100, -1, -1, ""+totalValue).sendToTarget();
        }
    };


    /**
     * Measure the view to end up as a square, based on the minimum of the height and width.
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int minDimension = Math.min(measuredWidth, measuredHeight);

        super.onMeasure(MeasureSpec.makeMeasureSpec(minDimension, widthMode),
                MeasureSpec.makeMeasureSpec(minDimension, heightMode));
    }


    public void initialize(Context context,  boolean vibrate) {

        zmqService = ZmqService.getInstance(zmqHandler);

        mVibrate = vibrate;

        mCircleView.initialize(context);
        mCircleView.invalidate();

        // Initialize the hours and minutes numbers.
        Resources res = context.getResources();
        String[] text = new String[12];
        for (int i = 0; i < 12; i++) {
            text[i] = "";
        }
        mRadialTextsView.initialize(res,
                text, null, false, true);
        mRadialTextsView.invalidate();

        mRadialSelectorView.initialize(context, false, true,
                0, false);

    }

    public void setDegree(int value) {
        synchronized (this) {
            totalValue = value;
        }

        Log.i("Degree", ""+totalValue);
        reselectSelector(value, true, false, true);
        CommandBean cmdBean = new CommandBean();
        cmdBean.setSteer("" + totalValue);
        AtosBean atos = new AtosBean();
        atos.setCommand(cmdBean);
        MessageBean msg = new MessageBean();
        msg.setAtos(atos);
        String jsonStr = GsonUtil.toJson(msg);
        zmqService.publishMsg(jsonStr);
    }

    public void setVibrate(boolean vibrate) {
        mVibrate = vibrate;
    }


    public int getValue() {
        return mCurrentValue;
    }

    /**
     * If the hours are showing, return the current hour. If the minutes are showing, return the
     * current minute.
     */
    private int getCurrentlyShowingValue() {
        return mCurrentValue;

    }


    private void preparePrefer30sMap() {

        mSnapPrefer30sMap = new int[361];

        int snappedOutputDegrees = 0;
        int count = 1;
        int expectedCount = 8;
        for (int degrees = 0; degrees < 361; degrees++) {
            mSnapPrefer30sMap[degrees] = snappedOutputDegrees;
            if (count == expectedCount) {
                snappedOutputDegrees += 6;
                if (snappedOutputDegrees == 360) {
                    expectedCount = 7;
                } else if (snappedOutputDegrees % 30 == 0) {
                    expectedCount = 14;
                } else {
                    expectedCount = 4;
                }
                count = 1;
            } else {
                count++;
            }
        }
    }

    private int snapOnly30s(int degrees, int forceHigherOrLower) {
        int stepSize = VISIBLE_DEGREES_STEP_SIZE;
        int floor = (degrees / stepSize) * stepSize;
        int ceiling = floor + stepSize;
        if (forceHigherOrLower == 1) {
            degrees = ceiling;
        } else if (forceHigherOrLower == -1) {
            if (degrees == floor) {
                floor -= stepSize;
            }
            degrees = floor;
        } else {
            if ((degrees - floor) < (ceiling - degrees)) {
                degrees = floor;
            } else {
                degrees = ceiling;
            }
        }
        return degrees;
    }


    private int reselectSelector(int degrees, boolean isInnerCircle,
                                 boolean forceToVisibleValue, boolean forceDrawDot) {
        if (degrees == -1) {
            return -1;
        }

        int stepSize;

        degrees = snapOnly30s(degrees, 0);

        RadialSelectorView radialSelectorView;
        radialSelectorView = mRadialSelectorView;
        stepSize = VISIBLE_DEGREES_STEP_SIZE;
        if (degrees == 0) {
            radialSelectorView.setSelection(0, isInnerCircle, forceDrawDot);
            radialSelectorView.invalidate();
            currentDegree = lastDegree = 0;

            Log.i("RS", "" + degrees);

            int value = degrees / stepSize;

            return value;
        }

        lastDegree = currentDegree;
        if (totalValue == 540) {
            if (degrees > 180)
                return 0;
        }
        if (totalValue == -540) {
            if (degrees < 180)
                return 0;
        }

        currentDegree = degrees;
        //Log.d(TAG, "Last Degree, " + lastDegree);
        //Log.i(TAG, "Curr Degree, " + currentDegree);
        int delta = currentDegree - lastDegree;

        if (delta < -180) {
            if (totalValue < -360) {
                totalValue = totalValue + currentDegree;
            }
            totalValue = totalValue + currentDegree;
        } else if (delta > 180) {
            if (totalValue > 0) {
                totalValue = totalValue + currentDegree - 360;
            }
            totalValue = totalValue + currentDegree - 360;
        } else {
            totalValue = totalValue + delta;
        }
        if (totalValue > 540) {
            totalValue = 540;
            currentDegree = lastDegree = 540;
            setDegree(540);
        }
        if (totalValue < -540) {
            totalValue = -540;
            currentDegree = lastDegree = 540;
            setDegree(-540);
        }
        Log.i("Total", "Total value: " + totalValue);
        int tmp = totalValue;
        if (tmp >= 360) {
            tmp = tmp - 360;
        }
        if (tmp < 0 && tmp >= -360) {
            tmp = tmp + 360;
        }
        if (tmp < -360) {
            tmp = tmp + 720;
        }

        radialSelectorView.setSelection(tmp, isInnerCircle, forceDrawDot);
        radialSelectorView.invalidate();

        Log.i("RS", "" + degrees);
        if (degrees == 0) {
            degrees = 360;
        }

        int value = degrees / stepSize;

        return value;
    }

    private synchronized int getDegreesFromCoords(float pointX, float pointY, boolean forceLegal,
                                     final Boolean[] isInnerCircle) {

        int degree = mRadialSelectorView.getDegreesFromCoords(
                    pointX, pointY, forceLegal, isInnerCircle);

            return degree;

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float eventX = event.getX();
        final float eventY = event.getY();
        int degrees;
        int value;
        final Boolean[] isInnerCircle = new Boolean[1];
        isInnerCircle[0] = false;

        long millis = SystemClock.uptimeMillis();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Log.i(TAG, "action down");

                count++;
                if(count == 1){
                    firClick = System.currentTimeMillis();

                } else if (count == 2){
                    secClick = System.currentTimeMillis();
                    if(secClick - firClick < 1000){
                        Log.i(TAG, "double click");
                        setDegree(0);
                    }
                    count = 0;
                    firClick = 0;
                    secClick = 0;

                }
                if (!mInputEnabled) {
                    return true;
                }

                mDownX = eventX;
                mDownY = eventY;

                mDoingMove = false;
                mDoingTouch = true;

                 {
                    boolean forceLegal = Utils.isTouchExplorationEnabled(mAccessibilityManager);
                    mDownDegrees = getDegreesFromCoords(eventX, eventY, forceLegal, isInnerCircle);
                    if (mDownDegrees != -1) {
                        tryVibrate();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDoingMove = true;
                                if (totalValue > -540 && totalValue < 540) {
                                    int value = reselectSelector(mDownDegrees, isInnerCircle[0],
                                            false, true);
                                }
                            }
                        }, TAP_TIMEOUT);
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!mInputEnabled) {
                    // We shouldn't be in this state, because input is disabled.
                    Log.e(TAG, "Input was disabled, but received ACTION_MOVE.");
                    return true;
                }

                float dY = Math.abs(eventY - mDownY);
                float dX = Math.abs(eventX - mDownX);

                if (!mDoingMove && dX <= TOUCH_SLOP && dY <= TOUCH_SLOP) {
                    break;
                }

                if (mDownDegrees == -1) {
                    break;
                }

                mDoingMove = true;
                mHandler.removeCallbacksAndMessages(null);
                degrees = getDegreesFromCoords(eventX, eventY, true, isInnerCircle);
                if (degrees != -1) {
                    if (totalValue > -540 && totalValue < 540) {
                        value = reselectSelector(degrees, isInnerCircle[0], false, true);
                        CommandBean cmdBean = new CommandBean();
                        cmdBean.setSteer("" + totalValue);
                        AtosBean atos = new AtosBean();
                        atos.setCommand(cmdBean);
                        MessageBean msg = new MessageBean();
                        msg.setAtos(atos);
                        String jsonStr = GsonUtil.toJson(msg);
                        zmqService.publishMsg(jsonStr);
                    }
                    if (totalValue == -540 && degrees > 180) {
                        value = reselectSelector(degrees, isInnerCircle[0], false, true);
                        CommandBean cmdBean = new CommandBean();
                        cmdBean.setSteer("" + totalValue);
                        AtosBean atos = new AtosBean();
                        atos.setCommand(cmdBean);
                        MessageBean msg = new MessageBean();
                        msg.setAtos(atos);
                        String jsonStr = GsonUtil.toJson(msg);
                        zmqService.publishMsg(jsonStr);
                    }
                    if (totalValue == 540 && degrees < 180) {
                        value = reselectSelector(degrees, isInnerCircle[0], false, true);
                        CommandBean cmdBean = new CommandBean();
                        cmdBean.setSteer("" + totalValue);
                        AtosBean atos = new AtosBean();
                        atos.setCommand(cmdBean);
                        MessageBean msg = new MessageBean();
                        msg.setAtos(atos);
                        String jsonStr = GsonUtil.toJson(msg);
                        zmqService.publishMsg(jsonStr);
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!mInputEnabled) {
                    return true;
                }

                mHandler.removeCallbacksAndMessages(null);
                mDoingTouch = false;

                if (mDownDegrees != -1) {
                    degrees = getDegreesFromCoords(eventX, eventY, mDoingMove, isInnerCircle);
                    if (degrees != -1) {
                        if (totalValue > -540 && totalValue < 540) {
                            value = reselectSelector(degrees, isInnerCircle[0], !mDoingMove, false);
                        }
                    }
                }
                mDoingMove = false;
                return true;
            default:
                break;
        }
        return false;
    }

    public void tryVibrate() {
        if (mVibrate && mVibrator != null) {
            long now = SystemClock.uptimeMillis();
            // We want to try to vibrate each individual tick discretely.
            if (now - mLastVibrate >= 125) {
                mVibrator.vibrate(5);
                mLastVibrate = now;
            }
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        if (Build.VERSION.SDK_INT >= 14) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
    }

    /**
     * Announce the currently-selected time when launched.
     */
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Clear the event's current text so that only the current time will be spoken.
            event.getText().clear();
            Time time = new Time();
            time.hour = getValue();
            long millis = time.normalize(true);
            int flags = DateUtils.FORMAT_SHOW_TIME;

            String timeString = DateUtils.formatDateTime(getContext(), millis, flags);
            event.getText().add(timeString);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }

        int changeMultiplier = 0;
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
            changeMultiplier = 1;
        } else if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            changeMultiplier = -1;
        }
        if (changeMultiplier != 0) {
            int value = getCurrentlyShowingValue();
            int stepSize = 0;
                stepSize = VISIBLE_DEGREES_STEP_SIZE;
                value %= 12;


            int degrees = value * stepSize;
            degrees = snapOnly30s(degrees, changeMultiplier);
            value = degrees / stepSize;
            int maxValue = 0;
            int minValue = 0;

            maxValue = 12;
            minValue = 1;

            if (value > maxValue) {
                // If we scrolled forward past the highest number, wrap around to the lowest.
                value = minValue;
            } else if (value < minValue) {
                // If we scrolled backward past the lowest number, wrap around to the highest.
                value = maxValue;
            }
            mListener.onValueSelected(0, value, false);
            return true;
        }

        return false;
    }
}
