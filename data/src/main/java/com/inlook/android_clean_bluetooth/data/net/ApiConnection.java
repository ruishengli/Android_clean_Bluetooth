package com.inlook.android_clean_bluetooth.data.net;

import android.support.annotation.Nullable;

import com.inlook.android_clean_bluetooth.data.net.metadata.Reqmsg;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ApiConnection
 *
 * @author or
 * @since 2016/10/20.
 */

public class ApiConnection implements Callable<String> {

    private static final String CONTENT_TYPE_LABEL = "Content-Type";
    private static final String CONTENT_TYPE_VALUE_JSON = "application/json; charset=utf-8";

    private Reqmsg reqmsg;
    private String response;

    private ApiConnection(Reqmsg reqmsg) {
        this.reqmsg = reqmsg;
    }
    public static ApiConnection createPost(Reqmsg reqmsg) throws MalformedURLException {
        return new ApiConnection(reqmsg);
    }

    @Nullable
    public String requestSyncCall() {
        connApi();
        return response;
    }

    private void connApi()  {

        OkHttpClient client = this.createClient();

        RequestBody formBody = new MultipartBody.Builder()
                        .addFormDataPart("DATA",this.reqmsg.getRqstData())
                        .setType(MultipartBody.FORM)
                        .build();

        Request request = new Request.Builder()
                .url(this.reqmsg.getReqUrl())
                .addHeader(CONTENT_TYPE_LABEL, CONTENT_TYPE_VALUE_JSON)
                .post(formBody)
                .build();

        try {
            Response res = client.newCall(request).execute();
            if(res.isSuccessful()) {
                response =  res.body().toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private OkHttpClient createClient() {
        final OkHttpClient okHttpClient =  new OkHttpClient().newBuilder()
                .connectTimeout(15000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000,TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
        return okHttpClient;
    }

    @Override
    public String call() throws Exception {
        return requestSyncCall();
    }
}
