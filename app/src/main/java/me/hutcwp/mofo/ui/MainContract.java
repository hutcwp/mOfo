package me.hutcwp.mofo.ui;

import com.amap.api.maps.model.LatLng;

import java.util.List;

import me.hutcwp.mofo.ui.base.IBasePresenter;
import me.hutcwp.mofo.ui.base.IBaseView;

/**
 * Created by hutcwp on 2018/4/8.
 */


public interface MainContract {


    interface View extends IBaseView {

        void showBike(List<LatLng> bikes);
        void location();
        void moveToCurrentPos(LatLng latLng);
    }

    interface Presenter extends IBasePresenter {

        void location();

        void getBikeMsg();

        void naviToPos();

        void scanCode();

        void bikeHistory();

    }

}
