package com.inlook.android_clean_bluetooth.data.net.user;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.text.TextUtils;

import com.inlook.android_clean_bluetooth.data.entry.Account;
import com.inlook.android_clean_bluetooth.data.entry.UserEntry;
import com.inlook.android_clean_bluetooth.data.net.ApiConnection;
import com.inlook.android_clean_bluetooth.data.util.NetWorkUtil;

import java.net.MalformedURLException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;

/**
 * UserApiImpl
 *
 * @author or
 * @since 2016/10/21.
 */

public class UserApiImpl implements UserApi {

    private Context context;

    public UserApiImpl(Context context) {

        if (context == null ) {
            throw new IllegalArgumentException("The constructor parameters cannot be null!!!");
        }
        this.context = context.getApplicationContext();
    }


    @Override
    public Observable<UserEntry> login(Account account) {

        return Observable.create(new Observable.OnSubscribe<UserEntry>() {

            @Override
            public void call(Subscriber<? super UserEntry> subscriber) {
                if(NetWorkUtil.isInternetConnection(context)) {
                    try {
                        String responseUserDetails = loginFromApi(account);
                        if(TextUtils.isEmpty(responseUserDetails)) {
                            //转换
                            subscriber.onNext(null);
                            subscriber.onCompleted();

                        } else {
                            subscriber.onError(new NetworkErrorException());
                        }
                    } catch (Exception e) {
                        subscriber.onError(new NetworkErrorException(e));
                    }
                } else {
                    subscriber.onError(new NetworkErrorException());
                }
            }
        });
    }

    private String loginFromApi(Account account) throws MalformedURLException {
        return ApiConnection.createPost(null).requestSyncCall();
    }
}
