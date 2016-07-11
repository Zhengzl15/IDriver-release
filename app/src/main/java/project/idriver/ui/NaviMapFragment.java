package project.idriver.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.NaviLatLng;

import project.idriver.R;
import project.idriver.map.NaviMapUtil;

/**
 * Author: ryan_wu
 * Email:  IMITATOR_WU@OUTLOOK.COM
 * Date:   16/2/8
 */
public class NaviMapFragment extends Fragment implements AMapNaviViewListener {
    /**
     *
     */
    private View mapLayout;                   // see on the Internet
    private AMapNavi mAMapNavi;
    private AMapNaviView mAMapNaviView;
    private NaviMapUtil naviMapUtil;

    public NaviMapFragment () {
        naviMapUtil = new NaviMapUtil();
    }

    public void setActivity(Activity activity) {
        naviMapUtil.setMainActivity(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mapLayout == null){
            mapLayout = inflater.inflate(R.layout.navi_map_fragment, container, false);
            mAMapNaviView = (AMapNaviView) mapLayout.findViewById(R.id.id_navi_fragment_navi_map_view);
            mAMapNaviView.onCreate(savedInstanceState);
            mAMapNaviView.setAMapNaviViewListener(this);
            naviMapUtil.setAMap(mAMapNaviView.getMap());
        }else if (mapLayout.getParent() != null){
            ((ViewGroup) mapLayout.getParent()).removeView(mapLayout);
        }
        return mapLayout;
    }

    public void startNavi() {
        naviMapUtil.startNavi();
    }

    public void setAMapNavi(AMapNavi aMapNavi) {
        mAMapNavi = aMapNavi;
        naviMapUtil.setAMapNavi(aMapNavi);
    }

    public void setLocation(NaviLatLng location) {
        naviMapUtil.setLocation(location);
    }

    public void setEndPoint(NaviLatLng endPoint) {
        naviMapUtil.setEndPoint(endPoint);
    }

    @Override
    public void onResume() {
        Log.i("simple navi", "resume .............................................................");
        super.onResume();
        mAMapNaviView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAMapNaviView.onPause();
        mAMapNavi.pauseNavi();
        Log.i("simple navi", "on pause.................................");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAMapNaviView.onDestroy();
        Log.i("simple navi", "on destroy.................................");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAMapNaviView.onSaveInstanceState(outState);
    }

    // AMapNaviViewListener
    @Override
    public void onNaviSetting() {}

    @Override
    public void onNaviCancel() {
        //左下角取消,处理返回按钮
        Log.i("navi fragment", "navi cancel..........................");
        onDestroy();
    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {}

    @Override
    public void onNaviTurnClick() {}

    @Override
    public void onNextRoadClick() {}

    @Override
    public void onScanViewButtonClick() {}

    @Override
    public void onLockMap(boolean b) {}
}
