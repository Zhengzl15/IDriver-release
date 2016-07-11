package project.idriver.map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.enums.PathPlanningStrategy;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLink;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.DriveWayView;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.ArrayList;
import java.util.List;

import project.idriver.beans.AtosBean;
import project.idriver.beans.GlobalmapBean;
import project.idriver.beans.GsonUtil;
import project.idriver.beans.MessageBean;
import project.idriver.nets.ZmqService;

/**
 * Created by ryan_wu on 16/1/26.
 */
public class NaviMapUtil implements AMapNaviListener {
    /**
     * control navigation
     */
    private Activity mainActivity;  // to make tips
    private AMapNavi mAMapNavi;  // AMapNavi instance
    private AMap aMap;  // AMap instance
    private ProgressDialog progDialog;  // progress dialog
    private NaviLatLng location = null;  // start point location
    private NaviLatLng endPoint;  // destination
    private LatLng Beijing = new LatLng(39.904211, 116.407395);  // for emulation
    private final static String TAG = "simple navi";
    private static final String SEND_MSG_ERR = "导航信息发送失败,请检查连接";
    private final static String LOCATION_ERR = "定位错误,请检查网络和GPS";

    private ZmqService mZmqService;
    //Handle the message from ZMQ service
    private final Handler mZmqHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "Receive message but needn't");
        }
    };

    public NaviMapUtil() {
        /**
         *  set message handler
         * */
        mZmqService = ZmqService.getInstance(mZmqHandler);
    }

    public void setMainActivity(Activity activity) {
        /**
         * set activity to make toast
         */
        mainActivity = activity;
    }

    public void setAMapNavi(AMapNavi aMapNavi) {
        /**
         * set AMapNavi instance
         * and set navigation listener
         */
        mAMapNavi = aMapNavi;
        mAMapNavi.setAMapNaviListener(this);
        mAMapNavi.setEmulatorNaviSpeed(100);
    }

    public void setAMap(AMap aMap) {
        this.aMap = aMap;
    }

    public void setLocation(NaviLatLng location) {
        /**
         * set start location
         */
        this.location = location;
    }

    public void setEndPoint(NaviLatLng endPoint) {
        /**
         * set end point
         */
        this.endPoint = endPoint;
    }

    public void startNavi(){
        /**
         * judge the start and end point
         * calculate route
         */
        showProgressDialog();
        List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
        endList.add(endPoint);
        if (mAMapNavi.isGpsReady()) {
            Log.i(TAG, "1........................................");
            mAMapNavi.calculateDriveRoute(endList, new ArrayList<NaviLatLng>(), PathPlanningStrategy.DRIVING_DEFAULT);
        } else if (location != null){ // for emulate
            Log.i(TAG, "2.......................................");
            List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
//            startList.add(new NaviLatLng(Beijing.latitude, Beijing.longitude));
            startList.add(location);
            mAMapNavi.calculateDriveRoute(startList, endList, new ArrayList<NaviLatLng>(), PathPlanningStrategy.DRIVING_DEFAULT);
        } else {
            Log.i(TAG, "3.....................................");
            dismissProgressDialog();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(Beijing, 10);
            aMap.animateCamera(cameraUpdate);
            Toast.makeText(mainActivity, LOCATION_ERR, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitNaviFailure() {
        Toast.makeText(mainActivity, "init navi Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInitNaviSuccess() {
        Log.i(TAG, "init navi success .............................................................");
    }

    @Override
    public void onStartNavi(int type) {
        // type:1 实时导航
        // type:2 模拟导航
        Log.i(TAG, String.format("INT:%d  start navi .............................................................", type));
    }

    @Override
    public void onTrafficStatusUpdate() {
        //当前方路况光柱信息有更新时回调函数。
        Log.i(TAG, "traffic StatusBean update .............................................................");
    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
        //当GPS位置有更新时的回调函数。
        Log.i(TAG, "location change .............................................................");
//        Log.i(TAG, String.format("Accuracy: %f", aMapNaviLocation.getAccuracy()));
//        Log.i(TAG, String.format("Altitude: %f", aMapNaviLocation.getAltitude()));
//        Log.i(TAG, String.format("bearing: %f", aMapNaviLocation.getBearing()));
//        Log.i(TAG, String.format("gps: %f, %f", aMapNaviLocation.getCoord().getLatitude(), aMapNaviLocation.getCoord().getLongitude()));
//        Log.i(TAG, String.format("speed: %f", aMapNaviLocation.getSpeed()));
//        Log.i(TAG, String.format("time: %1$ty-%1$tm-%1$td %1$tH:%1$tM:%1$tS", aMapNaviLocation.getTime()));
//        if(aMapNaviLocation.isMatchNaviPath()){
//            Log.i(TAG, "match navi Path");
//        }
//        else{
//            Log.i(TAG, "not match navi path");
//        }
    }

    @Override
    public void onGetNavigationText(int type, String text) {
        // 导航播报信息回调函数。
        // type - 播报类型，包含导航播报、前方路况播报和整体路况播报，类型见类属性。
        // text - 播报文字。
        Log.i(TAG, "get navagation text.....................................");
        Log.i(TAG, String.format("INT: %s.  String:%s", type, text));
    }

    @Override
    public void onEndEmulatorNavi() {
        // 模拟导航停止后回调函数。
        Log.i(TAG, " end emulator navi.......................................");
        onArriveDestination();
    }

    @Override
    public void onArriveDestination() {
        // 到达目的地后回调函数
        Log.i(TAG, " arrive destination .......................................");
    }

    @Override
    public void onCalculateRouteSuccess() {
        // 步行或者驾车路径规划成功后的回调函数。
        // 应该加入导航开始的按钮
        dismissProgressDialog();
        Log.i(TAG, " calculate rote success .......................................");
//        mAMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
        mAMapNavi.startNavi(AMapNavi.GPSNaviMode);
    }

    @Override
    public void onCalculateRouteFailure(int errorInfo) {
        /* 步行或者驾车路径规划失败后的回调函数。
        1	路径计算成功
        2	网络超时或网络失败
        3	起点错误
        4	协议解析错误
        6	终点错误
        10	起点没有找到道路
        11	终点没有找到道路
        12	途经点没有找到道路
        13	用户Key非法或过期
        14	请求服务不存在
        15	请求服务响应错误
        16	无权限访问此服务
        17	请求超出配额
        18	请求参数非法
        19	未知错误
        */
        dismissProgressDialog();
        Log.i(TAG, String.format("int:%d, calculate route failure.......................................", errorInfo));
    }

    @Override
    public void onReCalculateRouteForYaw() {
        // 步行或驾车导航时,出现偏航后需要重新计算路径的回调函数。
        Log.i(TAG, " recalculate route for yaw.......................................");
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        // 驾车导航时，如果前方遇到拥堵时需要重新计算路径的回调。
        Log.i(TAG, " recalculate route for traffic jam.......................................");
    }

    @Override
    public void onArrivedWayPoint(int wayID) {
        // 驾车路径导航到达某个途经点的回调函数。
        Log.i(TAG, String.format(" int: %d arrived way point.......................................", wayID));
    }

    @Override
    public void onGpsOpenStatus(boolean enabled) {
        // 用户手机GPS设置是否开启的回调函数
        Log.i(TAG, " gps open status.......................................");
        if(enabled){
            Log.i(TAG, "gps open");
        }
        else{
            Log.i(TAG, "gps closed");
        }
    }

    @Deprecated
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {}

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        /** 导航引导信息回调 naviinfo 是导航信息类。
         * and sent the navigation to server
         */
        Log.i(TAG, "update navi info .................................");
        Log.i(TAG, String.format("%dm turn: %d", naviInfo.getCurStepRetainDistance(), naviInfo.getIconType()));
        Log.i(TAG, String.format("road %s --> %s", naviInfo.getCurrentRoadName(), naviInfo.getNextRoadName()));
        AMapNaviStep astep = mAMapNavi.getNaviPath().getSteps().get(naviInfo.getCurStep());
        AMapNaviLink alink = astep.getLinks().get(naviInfo.getCurLink());
        Log.i(TAG, String.format("link size: %d, index: %d", astep.getLinks().size(), naviInfo.getCurLink()));
        Log.i(TAG, String.format("name:%s, class: %d,  type:%d", alink.getRoadName(), alink.getRoadClass(), alink.getRoadType()));

        GlobalmapBean globalmap = new GlobalmapBean();
        globalmap.setCurrentRoadName(naviInfo.getCurrentRoadName());
        globalmap.setNextRoadName(naviInfo.getNextRoadName());
        if (naviInfo.getCurStepRetainDistance() < 10) {
            globalmap.setCurrentOrder(String.valueOf(naviInfo.getIconType()));
        }
        else {
            globalmap.setCurrentOrder("0");
        }
        globalmap.setNextOrder(String.valueOf(naviInfo.getIconType()));
        globalmap.setLeftLength(String.valueOf(naviInfo.getCurStepRetainDistance()));
        globalmap.setLimitSpeed(String.valueOf(naviInfo.getLimitSpeed()));
        globalmap.setRoadClass(String.valueOf(alink.getRoadClass()));
        globalmap.setRoadType(String.valueOf(alink.getRoadType()));
        globalmap.setRoadSize(String.valueOf(getDriveSize()));
        AtosBean atos = new AtosBean();
        atos.setGlobalmap(globalmap);
        MessageBean message = new MessageBean();
        message.setAtos(atos);
        try {
            mZmqService.publishMsg(GsonUtil.toJson(message));
        }catch (Exception e) {
            Toast.makeText(mainActivity.getApplicationContext(), SEND_MSG_ERR, Toast.LENGTH_LONG).show();
        }
    }

    @Deprecated
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {}

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
        // 摄像头信息更新回调。 CameraType
        Log.i(TAG, "update traffic facility 2........................................");
        Log.i(TAG, String.format("limit speed: %d", aMapNaviTrafficFacilityInfo.getLimitSpeed()));
        Log.i(TAG, String.format("broadcast type: %d", aMapNaviTrafficFacilityInfo.getBoardcastType()));
        Log.i(TAG, String.format("coorx: %f, coory: %f", aMapNaviTrafficFacilityInfo.getCoorX(), aMapNaviTrafficFacilityInfo.getCoorY()));
        Log.i(TAG, String.format("distance: %d", aMapNaviTrafficFacilityInfo.getDistance()));
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        // 显示路口放大图回调
        Log.i(TAG, "show cross......................................................");
    }

    @Override
    public void hideCross() {
        // 关闭路口放大图回调
        Log.i(TAG, "hide cross.....................................................");
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
        // 显示道路信息回调。
        Log.i(TAG, "show lane info..........................................");
    }

    @Override
    public void hideLaneInfo() {
        // 关闭道路信息回调。
    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] routeIds) {
        // 多路线算路成功回调
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null) {
            if (mainActivity == null)
                Log.i(TAG, "null ................");
            progDialog = new ProgressDialog(mainActivity);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
        Log.i(TAG, "show progress dialog!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    /**
     *
     * 隐藏进度框
     */
    public void dismissProgressDialog(){
        if (progDialog != null){
            progDialog.dismiss();
        }
        Log.i(TAG, "dismiss progress dialog!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private int getDriveSize() {
        //http://lbsbbs.amap.com/forum.php?mod=viewthread&tid=8276&highlight=车道
        DriveWayView dwv = new DriveWayView(mainActivity.getApplicationContext());
        return dwv.getDriveWaySize();
    }
}
