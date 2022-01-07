package com.ht.common.okgo;

import android.app.Application;
import android.content.Context;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

final public class OkHttpInitHelper {

    static private OkHttpInitHelper okHttpUtils;

    private OkGo okGo;

    private OkHttpInitHelper(Application application) {
        if (okGo != null) {
            return;
        }
        this.okGo = OkGo.getInstance().init(application);
    }

    static public OkHttpInitHelper getInstance(Application application) {
        if (okHttpUtils == null) {
            okHttpUtils = new OkHttpInitHelper(application);
        }
        return okHttpUtils;
    }

    static public OkHttpInitHelper getInstance() {
        if (okHttpUtils == null) {
            throw new IllegalStateException("请先调用 getInstance(Application application) 方法初始化");
        }
        return okHttpUtils;
    }

    public OkHttpInitHelper init() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        loggingInterceptor.setColorLevel(Level.INFO);

        builder.addInterceptor(loggingInterceptor);

        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);

        okGo.setOkHttpClient(builder.build())
                .setRetryCount(3);
        return okHttpUtils;
    }

    public OkHttpInitHelper setHeader(Map<String, String> headerMap) {
        HttpHeaders headers = new HttpHeaders();
        if (headerMap != null && !headerMap.isEmpty()) {
            for (Map.Entry<String, String> stringStringEntry : headerMap.entrySet()) {
                headers.put(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
            okGo.addCommonHeaders(headers);
        }
        return okHttpUtils;
    }

    public OkHttpInitHelper setParams(Map<String, String> paramMap) {
        HttpParams params = new HttpParams();
        if (paramMap != null && !paramMap.isEmpty()) {
            for (Map.Entry<String, String> stringStringEntry : paramMap.entrySet()) {
                params.put(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
            okGo.addCommonParams(params);
        }
        return okHttpUtils;
    }

    public OkHttpInitHelper setRetryCount(int retryCount) {
        okGo.setRetryCount(retryCount);
        return okHttpUtils;
    }

    public OkHttpInitHelper setLogInfo(String tag, HttpLoggingInterceptor.Level printLevel, Level colorLevel) {

        OkHttpClient.Builder builder = okGo.getOkHttpClient().newBuilder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(tag);
        loggingInterceptor.setPrintLevel(printLevel);
        loggingInterceptor.setColorLevel(colorLevel);
        builder.addInterceptor(loggingInterceptor);

        okGo.setOkHttpClient(builder.build());

        return okHttpUtils;
    }

    public OkHttpInitHelper setTimeOut(long readTimeOut, long writeTimeOut, long connectTimeOut) {
        OkHttpClient.Builder builder = okGo.getOkHttpClient().newBuilder();
        builder.readTimeout(readTimeOut, TimeUnit.MILLISECONDS);
        builder.writeTimeout(writeTimeOut, TimeUnit.MILLISECONDS);
        builder.connectTimeout(connectTimeOut, TimeUnit.MILLISECONDS);

        okGo.setOkHttpClient(builder.build());

        return okHttpUtils;
    }

    /**
     *
     * @param context
     * @return
     */
    public OkHttpInitHelper initCookieJar(Context context) {
        OkHttpClient.Builder builder = okGo.getOkHttpClient().newBuilder();
        //1.默认是CookieJar.NO_COOKIES
        //2.使用SharedPreferences保持cookie，如果cookie不过期，则一直有效
//        builder.cookieJar(new CookieJarImpl(new SPCookieStore(context)));
        //3.使用数据库保持cookie，如果cookie不过期，则一直有效
//        builder.cookieJar(new CookieJarImpl(new DBCookieStore(context)));
        //4.使用内存保持cookie，app退出后，cookie消失
        builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));

        okGo.setOkHttpClient(builder.build());

        return okHttpUtils;
    }


}
