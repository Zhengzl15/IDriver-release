package project.idriver.map;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;

import project.idriver.ui.GlobalFragment;

/**
 * Author: ryan_wu
 * Email:  IMITATOR_WU@OUTLOOK.COM
 * Date:   16/2/12
 */
public class WAMapLoaction implements AMapLocationListener {
    /**
     * to detect the gps coordinate of the device
     * through wifi, gps and phone base station
     */
    private AMapLocationClient mAMapLoactionClient = null;  // client to locate device
    private AMapLocationClientOption mLocationOption = null;  // locate option
    private NaviLatLng location;  // the loaction
    private GlobalFragment mGlobalFragment = null;
    private boolean isFirst = true;  // just for the start of the software

    public WAMapLoaction(Context context) {
        /**
         * constructor
         */
        mAMapLoactionClient = new AMapLocationClient(context);
        setmLocationOption();
        mAMapLoactionClient.setLocationListener(this);
        mAMapLoactionClient.startLocation();
    }
    public void setGlobalFragment(GlobalFragment globalFragment) {
        /**
         * for make the view to the current position
         */
        mGlobalFragment = globalFragment;
    }

    private void setmLocationOption() {
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mAMapLoactionClient.setLocationOption(mLocationOption);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        /**
         * initialize the map view when the java detect the device location
         */
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                Double latitude = amapLocation.getLatitude();
                Double longitude = amapLocation.getLongitude();
                location = new NaviLatLng(latitude, longitude);
                if (isFirst) {
                    mGlobalFragment.moveToLocation(new LatLng(latitude, longitude));
                    isFirst = false;
                }
                Log.i("location", String.format("%d %f, %f", amapLocation.getLocationType(), latitude, longitude));
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    public NaviLatLng getLocation(){
        return location;
    }

    public void destroyLocation() {
        /**
         * for upper level onDestroy
         */
        mAMapLoactionClient.onDestroy();
    }
}
