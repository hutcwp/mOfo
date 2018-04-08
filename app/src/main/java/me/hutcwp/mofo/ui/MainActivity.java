package me.hutcwp.mofo.ui;


import android.Manifest;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.elvishew.xlog.XLog;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;


import java.util.List;

import me.hutcwp.mofo.R;
import me.hutcwp.mofo.ui.base.BaseMapActivity;
import me.hutcwp.mofo.util.Constants;
import me.hutcwp.mofo.util.ToastUtil;


public class MainActivity extends BaseMapActivity implements AMap.CancelableCallback, MainContract.View, AMapLocationListener {
    private static final String TAG = "MainActivity";

    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);

    private static LatLng curPos = Constants.CUR_POSITION;

    private AMap aMap;
    private ImageButton mIBLocation;

    private Marker locationMarker;
    private Marker positionMarker;
    private Projection projection;
    private MainPresenter mMainPresenter;
    private OnLocationChangedListener mListener;
    private AMapLocationClientOption mLocationOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(savedInstanceState);
        initSetting();
        moveToCurrentPos(Constants.CUR_POSITION);
        showBike(null);
        setupLocationStyle();
        setUpMap();

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
        }, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE});
    }

    private void initView(Bundle savedInstanceState) {
        mapView = findViewById(R.id.map);
        mIBLocation = findViewById(R.id.ib_location);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            aMap = mapView.getMap();
        }
    }

    /**
     * 初始化AMap对象
     */
    private void initSetting() {
        mIBLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToCurrentPos(Constants.CUR_POSITION);
            }
        });
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        XLog.d(TAG, "[onLocationChanged]");
        if (mListener != null && amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                curPos = latLng;

                if (locationMarker == null) {
                    locationMarker = createMarket(latLng, R.drawable.map_marker);
                }

                if (positionMarker == null) {
                    positionMarker = createMarket(latLng, R.drawable.start_center_point);
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    startMoveLocationAndMap(latLng, positionMarker);
                }

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e(TAG, errText);
            }
        }
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

    }


    private Marker createMarket(LatLng latLng, int resId) {
        return aMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(resId))
                .anchor(0.5f, 0.5f));
    }

    /**
     * 设置自定义定位蓝点
     */
    private void setupLocationStyle() {
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
    }


    @Override
    public void setPresenter(Object presenter) {
        this.mMainPresenter = (MainPresenter) presenter;
    }


    @Override
    public void showBike(List<LatLng> bikes) {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(curPos);
        markerOption.title("西安市").snippet("西安市：34.341568, 108.940174");

        markerOption.draggable(true);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(), R.drawable.amap_ride)));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(true);//设置marker平贴地图效果

        Marker marker = aMap.addMarker(markerOption);
    }

    @Override
    public void location() {

    }

    @Override
    public void moveToCurrentPos(LatLng curPos) {
        changeCamera(
                CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        curPos, 20, 0, 30)), null);
        aMap.clear();
        aMap.addMarker(new MarkerOptions().position(curPos)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(R.drawable.gps_point)));
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        aMap.animateCamera(update, 1000, callback);
    }

    /**
     * 地图动画效果终止回调方法
     */
    @Override
    public void onCancel() {
        ToastUtil.show(MainActivity.this, "Animation canceled");
    }

    /**
     * 地图动画效果完成回调方法
     */
    @Override
    public void onFinish() {
        XLog.i(TAG, "[onFinish]");
        ToastUtil.show(MainActivity.this, "Animation complete");
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
