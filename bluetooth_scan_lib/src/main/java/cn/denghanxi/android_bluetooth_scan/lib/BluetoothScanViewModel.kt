package cn.denghanxi.android_bluetooth_scan.lib;

import androidx.lifecycle.ViewModel;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * Created by dhx on 2021/7/14.
 */
public class BluetoothScanViewModel extends ViewModel {

    // in
    BehaviorSubject<Boolean> loadingStatus = BehaviorSubject.createDefault(false);
    PublishSubject<Boolean> refreshRequest = PublishSubject.create();

    // out
    Observable<Boolean> refreshManager = refreshRequest.withLatestFrom(loadingStatus, (b1, b2) -> b1 && !b2);

    public BluetoothScanViewModel() {
    }

}
