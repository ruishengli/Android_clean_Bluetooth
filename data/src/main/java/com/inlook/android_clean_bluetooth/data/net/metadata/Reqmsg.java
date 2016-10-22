package com.inlook.android_clean_bluetooth.data.net.metadata;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.inlook.android_clean_bluetooth.data.util.DecriptUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Reqmsg
 *
 * @author or
 * @since 2016/10/21.
 */

public class Reqmsg {

    private String version = "1.2.4";
    private String clientVersion = "138";
    private int platform = 0;
    private int tag = 0;

    private String reqUrl;

    private String rqstType;
    private String rqstData;

    public String getRqstData() {
        return rqstData;
    }

    public void setRqstType(String rqstType) {
        this.rqstType = rqstType;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public void convertServiceReqString(Object obj) {

        if(isValidRqst() && obj != null) {

            ReqHeader header = initReqHeader();
            ReqContent<ReqHeader, Object> buss = new ReqContent<>(header, obj);

            rqstData = JSON.toJSONString(buss);
        }
    }

    public boolean isValidRqst() {
        if (TextUtils.isEmpty(reqUrl) ||
                TextUtils.isEmpty(rqstType)) {
            return false;
        } else {
            return true;
        }
    }

    private ReqHeader initReqHeader() {

        ReqHeader header = new ReqHeader();
        header.setBusiness(this.rqstType);
        header.setSquence(this.tag);
        header.setVersion(this.version);
        header.setPlatform(this.platform);
        header.setClientVersion(this.clientVersion);

        String key = "A11808E7E70C08BBA8EBFD64E4EB456E";
       // String key = AESUtil.getAuthKey(LeqiApplication.applicationContext);
        if(TextUtils.isEmpty(key)) {
            return null;
        }

        long timestamp = System.currentTimeMillis();
        header.setTimestamp(timestamp);

        String ne = ne(timestamp);

        String signature = DecriptUtil.SHA1(arrayConvertStr((stringSort(new String[]{key, String.valueOf(timestamp), ne}))));
//      String signature =  AESUtil.SHA1(arrayConvertStr(stringSort(new String[]{key, String.valueOf(timestamp), ne})));
        header.setNonce(ne);
        header.setSignature(signature);

        return header;
    }


    private String[] stringSort(String[] s) {
        List<String> list = new ArrayList<>(s.length);
        for (int i = 0; i < s.length; i++) {
            list.add(s[i]);
        }
        Collections.sort(list);
        return list.toArray(s);
    }

    private  String arrayConvertStr(String[] s){
        if(s == null || s.length<=0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        int length = s.length;
        for(int i=0;i<length;i++) {
            sb.append(s[i]);
        }

        return sb.toString();
    }

    private String ne(long timestamp) {
        return  UUID.randomUUID().toString()+timestamp;
    }
}
