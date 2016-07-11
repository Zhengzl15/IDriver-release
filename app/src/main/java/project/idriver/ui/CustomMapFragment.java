package project.idriver.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import project.idriver.R;
import project.idriver.map.CustomMapUtil;

/**
 * Created by ryan_wu on 16/1/22.
 */
public class CustomMapFragment extends Fragment{
    private CustomMapUtil customMap;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.custom_map_fragment, container, false);
        WheelView startWheel = (WheelView) view.findViewById(R.id.id_custom_map_start_wheel);
        WheelView endWheel = (WheelView) view.findViewById(R.id.id_custom_map_end_wheel);
        Button setlineButton = (Button) view.findViewById(R.id.id_custom_map_setline_button);
        LinearLayout llayout = (LinearLayout) view.findViewById(R.id.id_custom_map);
        customMap = new CustomMapUtil(getActivity());
        customMap.setWheel(startWheel, endWheel);
        setlineButton.setOnClickListener(customMap);
        llayout.addView(customMap);
        return view;
    }
}
