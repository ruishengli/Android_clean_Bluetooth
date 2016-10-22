package com.inlook.android_clean_bluetooth.data.net.metadata;

/**
 * ReqContent
 *
 * @author or
 * @since 2016/10/21.
 */

public class ReqContent<T1,T2> {

    private ReqBody<T1, T2> service;

    public ReqContent(T1 t1, T2 t2) {
        this.service = new ReqBody<T1, T2>(t1, t2);
    }

    public ReqBody<T1, T2> getService() {
        return service;
    }
    public void setService(ReqBody<T1, T2> Service) {
        this.service = Service;
    }
}
