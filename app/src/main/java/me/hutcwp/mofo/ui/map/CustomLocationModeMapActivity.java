package me.hutcwp.mofo.ui.map;


import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.elvishew.xlog.XLog;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

import me.hutcwp.mofo.R;
import me.hutcwp.mofo.ui.base.BaseMapActivity;
import me.hutcwp.mofo.util.ToastUtil;

public class CustomLocationModeMapActivity extends BaseMapActivity implements
        AMapLocationListener, AMap.OnMapTouchListener, AMap.OnMarkerClickListener {

    private static final String TAG = "CustomLocationModeMapAc";


    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClientOption mLocationOption;
    private Marker locationMarker;
    private Marker positionMarker;
    private Projection projection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_location_mode);
        mapView = findViewById(R.id.map);
        aMap = mapView.getMap();
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        setUpMap();
        aMap.setOnMarkerClickListener(this);
        PermissionManager.instance().with(this);
        PermissionManager.instance().request(new OnPermissionCallback() {
            @Override
            public void onRequestAllow(String permissionName) {

            }

            @Override
            public void onRequestRefuse(String permissionName) {

            }

            @Override
            public void onRequestNoAsk(String permissionName) {

            }
        }, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION} );
    }


    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setOnMapTouchListener(this);
    }


    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());

                XLog.d(TAG,"[onLocationChanged]");
                if (locationMarker == null) {
                    locationMarker = createMarket(latLng, R.drawable.map_marker);
                }

                if (positionMarker == null) {
                    positionMarker = createMarket(latLng, R.drawable.start_center_point);
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    startMoveLocationAndMap(latLng, positionMarker);
                } else {

                }

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }


    private Marker createMarket(LatLng latLng, int resId) {
        return aMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(resId))
                .anchor(0.5f, 0.5f));
    }


    /**
     * 同时修改自定义定位小蓝点和地图的位置
     *
     * @param latLng
     */
    private void startMoveLocationAndMap(LatLng latLng, Marker marker) {
        //将小蓝点提取到屏幕上
        if (projection == null) {
            projection = aMap.getProjection();
        }
        if (marker != null && projection != null) {
            marker.setPositionByPixels(540, 460);
        }
        //移动地图，移动结束后，将小蓝点放到放到地图上
        myCancelCallback.setTargetLatlng(latLng);
    }


    MyCancelCallback myCancelCallback = new MyCancelCallback();

    @Override
    public void onTouch(MotionEvent motionEvent) {
        Log.i("amap", "onTouch 关闭地图和小蓝点一起移动的模式");

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.d("hutcwp", "onTouch: up");
                add(positionMarker.getPosition());
                break;
        }
    }


    private void add(LatLng latLng) {
        addMarkersToMap(new LatLng(latLng.latitude + 0.02, latLng.longitude + 0.01));
        addMarkersToMap(new LatLng(latLng.latitude + 0.01, latLng.longitude + 0.02));
        addMarkersToMap(new LatLng(latLng.latitude - 0.01, latLng.longitude - 0.002));
        addMarkersToMap(new LatLng(latLng.latitude - 0.02, latLng.longitude + 0.01));
        addMarkersToMap(new LatLng(latLng.latitude + 0.02, latLng.longitude + 0.001));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        ToastUtil.show(CustomLocationModeMapActivity.this, "点击了Market");
        marker.setTitle("点击了");
        marker.setSnippet("距离： + 时间");
//        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.wheel))
//                .position(marker.getPosition())
//                .title("hahah")
//                .snippet("this is test")
//                .draggable(true);
//        aMap.addMarker(markerOption);

        return false;
    }

    /**
     * 监控地图动画移动情况，如果结束或者被打断，都需要执行响应的操作
     */
    class MyCancelCallback implements AMap.CancelableCallback {

        LatLng targetLatlng;

        public void setTargetLatlng(LatLng latlng) {
            this.targetLatlng = latlng;
        }

        @Override
        public void onFinish() {
            if (locationMarker != null && targetLatlng != null) {
                locationMarker.setPosition(targetLatlng);
            }
        }

        @Override
        public void onCancel() {
            if (locationMarker != null && targetLatlng != null) {
                locationMarker.setPosition(targetLatlng);
            }
        }
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap(LatLng latLng) {
        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.wheel))
                .position(latLng)
                .draggable(true);
        Marker marker = aMap.addMarker(markerOption);
    }


    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //是指定位间隔
            mLocationOption.setInterval(2000);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


}