package project.idriver.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.PoiItem;
import java.util.List;
import project.idriver.R;
import project.idriver.map.NaviMapUtil;
import project.idriver.map.WAMapLoaction;

/**
 * Author: ryan_wu
 * Email:  IMITATOR_WU@OUTLOOK.COM
 * Date:   16/2/8
 */
public class GlobalFragment extends Fragment implements
        AMap.OnMarkerClickListener, AMap.InfoWindowAdapter, OnToggleStateChangedListener {
    private View mapLayout;                   // see on the Internet
    private AMap mAMap;
    private TextureMapView mMapView;
    private GlobalMapSearchFragment globalMapSearchFragment;
    private NaviMapFragment mNaviMapFragment;
    private CustomMapFragment mCustomMapFragment;
    private WAMapLoaction mAMapLocation;
    private AMapNavi mAMapNavi;

    private final static String TAG = "global fragment";

    public GlobalFragment(){
        globalMapSearchFragment = new GlobalMapSearchFragment();
        globalMapSearchFragment.setMapNaviFragment(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mapLayout == null){
            mapLayout = inflater.inflate(R.layout.global_fragment, container, false);
            mMapView = (TextureMapView) mapLayout.findViewById(R.id.id_navi_fragment_basic_map_view);
            mMapView.onCreate(savedInstanceState);
            mAMap = mMapView.getMap();
            mAMap.setOnMarkerClickListener(this);
            mAMap.setInfoWindowAdapter(this);

            // add search
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.add(R.id.id_navi_fragment_basic_map_view, globalMapSearchFragment, "search");
            transaction.commit();

            // location
            mAMapLocation = new WAMapLoaction(getActivity().getApplicationContext());
            mAMapLocation.setGlobalFragment(this);
            mAMapNavi = AMapNavi.getInstance(getActivity().getApplicationContext());
        }else if (mapLayout.getParent() != null){
            ((ViewGroup) mapLayout.getParent()).removeView(mapLayout);
        }
        return mapLayout;
    }

    public void moveToLocation(LatLng location) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 10);
        mAMap.animateCamera(cameraUpdate);
    }

    private void startNavi(Marker marker) {
        Log.i(TAG, "start navi");
        mAMap.clear();
        mNaviMapFragment = new NaviMapFragment();
        mNaviMapFragment.setActivity(getActivity());
        mNaviMapFragment.setAMapNavi(mAMapNavi);
        mNaviMapFragment.setLocation(mAMapLocation.getLocation());
        mNaviMapFragment.setEndPoint(new NaviLatLng(marker.getPosition().latitude, marker.getPosition().longitude));

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.id_navi_fragment_basic_map_view, mNaviMapFragment);
        transaction.commit();
        mNaviMapFragment.startNavi();
      }

    @Override
    public void onResume() {
        Log.i(TAG, "resume .............................................................");
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        Log.i(TAG, "on pause.................................");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mAMapLocation.destroyLocation();
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
        Log.i(TAG, "on destroy.................................");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    public void setPoiResult(List<PoiItem> poiResult){
        mAMap.clear();// 清理之前的图标
        PoiOverlay poiOverlay = new PoiOverlay(mAMap, poiResult);
        poiOverlay.removeFromMap();
        poiOverlay.addToMap();
        poiOverlay.zoomToSpan();
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.navi_info_window, null);

        TextView title = (TextView) view.findViewById(R.id.id_navi_info_window_title);
        title.setText(marker.getTitle());
        TextView snippet = (TextView) view.findViewById(R.id.id_navi_info_window_snippet);
        snippet.setText(marker.getSnippet());
        ImageButton button = (ImageButton) view.findViewById(R.id.id_navi_info_window_start_navi);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNavi(marker);
            }
        });
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onToggleStateChanged(boolean state) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (((SlideButton) getActivity().findViewById(R.id.map_choose_btn)).isToggleState()) {
            Log.i("map", "map yes");
            if (mCustomMapFragment != null) {
                transaction.remove(mCustomMapFragment);
                mCustomMapFragment = null;
            }
            if (mNaviMapFragment != null)
                mNaviMapFragment.onResume();
        } else {
            Log.i("map", "map no");
            if (mCustomMapFragment == null) {
                mCustomMapFragment = new CustomMapFragment();
                transaction.add(R.id.id_navi_fragment_basic_map_view, mCustomMapFragment);
            }
            if (mNaviMapFragment != null) {
                mNaviMapFragment.onPause();
            }
        }
        transaction.commit();
    }
}
