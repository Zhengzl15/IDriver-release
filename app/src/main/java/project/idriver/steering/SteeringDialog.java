package project.idriver.steering;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import java.util.Locale;

import project.idriver.R;


public class SteeringDialog extends DialogFragment{
    private static final String TAG = "SteeringDialog";

    private static final String KEY_CURRENT_ITEM_SHOWING = "current_item_showing";

    // Delay before starting the pulse animation, in ms.
    private static final int PULSE_ANIMATOR_DELAY = 300;

    private TextView mDoneButton;
    private TextView mSymbol;
    private TextView mValue;
    private TextView mValueSpaceView;
    private RadialPickerLayout mSteeringOp;


    private String mDoublePlaceholderText;

    private boolean mVibrate = true;
    private int totalValue;
    private Context mContext;

    public interface OnTimeSetListener {


        void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute);
    }

    @SuppressLint("ValidFragment")
    public SteeringDialog(Context context) {
        this.mContext = context;
        // Empty constructor required for dialog fragment. DO NOT REMOVE
    }

    public static SteeringDialog newInstance(Context context) {
        SteeringDialog ret = new SteeringDialog(context);
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.steering_picker_dialog, null);

        Resources res = getResources();
        mSymbol = (TextView) view.findViewById(R.id.symbol);
        mValueSpaceView = (TextView) view.findViewById(R.id.value_space);
        mValue = (TextView) view.findViewById(R.id.value);

        mSteeringOp = (RadialPickerLayout) view.findViewById(R.id.steering);
        mSteeringOp.initialize(getActivity(), mVibrate);
        int currentItemShowing = 0;
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(KEY_CURRENT_ITEM_SHOWING)) {
            currentItemShowing = savedInstanceState.getInt(KEY_CURRENT_ITEM_SHOWING);
        }
        mSteeringOp.invalidate();
        mSteeringOp.setHandler(mHandler);
        mSteeringOp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "click");
            }
        });

        mDoneButton = (TextView) view.findViewById(R.id.done_button);
        mDoneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneButtonClick();
            }
        });

        setValue(0);

        mDoublePlaceholderText = res.getString(R.string.steering_placeholder);

        return view;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    String v = (String) msg.obj;
                    //Log.i(TAG, "value: " + v);
                    totalValue = Integer.parseInt(v);
                    //Log.i("Int", "" + totalValue);
                    if (totalValue < 0) {
                        mSymbol.setText("-");
                    } else if (totalValue > 0) {
                        mSymbol.setText("+");
                    } else {
                        mSymbol.setText(" ");
                    }
                    setValue(Math.abs(totalValue));

                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismiss();
        Log.i(TAG, "destory");
        SharedPreferences sp = mContext.getSharedPreferences("steering", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("value", totalValue);
        editor.commit();
        mSteeringOp.stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = mContext.getSharedPreferences("steering", 0);
        int value = sp.getInt("value", 0);
        totalValue = value;
        mSteeringOp.setDegree(totalValue);
        Log.i(TAG, "resume: " + totalValue);
    }

    private void onDoneButtonClick() {
        //dismiss();
        SharedPreferences sp = mContext.getSharedPreferences("steering", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("value", totalValue);
        editor.commit();
        mSteeringOp.stopTimer();
        onStop();
    }

    private void setValue(int value) {
        /*if (value == 60) {
            value = 0;
        }*/
        //CharSequence text = String.format(Locale.getDefault(), "%02d", value);
        //Utils.tryAccessibilityAnnounce(mSteeringOp, text);
        String text = "" + value;
        mValue.setText(text);
        mValueSpaceView.setText(text);
    }
}