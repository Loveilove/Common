package com.ht.common.okgo;


import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.ht.common.okgo.entity.BaseQuery;
import com.ht.common.okgo.entity.BaseResponeEntity;
import com.ht.common.util.GsonUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseLoader {

    final private Object cancelTag;

    public BaseLoader() {
        cancelTag = new Object();
    }

    /******************* GET ***********************
     * get 请求的参数拼接方式只有一种，
     * 类似于 http://192.168.1.1:8080?test1=111&test2=2222 这样的类型
     * 下列方法取名仅依照之前方法，无实际意义
     *
     */

    /***
     * get请求，map参数,不带附加头
     * @param url
     * @param paramsMap
     * @param callback
     */
    public void get(String url, Map<String, Object> paramsMap, Callback callback) {
        doGetJson(url, paramsMap, null, callback);
    }

    /***
     * get请求，map参数，带附加头
     * @param url
     * @param paramsMap
     * @param headers
     * @param callback
     */
    public void get(String url, Map<String, Object> paramsMap, Map<String, String> headers, Callback callback) {
        doGetJson(url, paramsMap, headers, callback);
    }

    /****
     * get请求，Object参数,不带附加头
     * @param url
     * @param object
     * @param callback
     */
    public <Q extends BaseQuery> void get(String url, Q object, Callback callback) {
        doGetJson(url, object2Map(object), null, callback);
    }

    /***
     * get请求，Object参数，带附加头
     * @param url
     * @param object
     * @param headers
     * @param callback
     */
    public <Q extends BaseQuery> void get(String url, Q object, Map<String, String> headers, Callback callback) {
        doGetJson(url, object2Map(object), headers, callback);
    }

    /********************************* POST ******************************************/


    /***
     * post请求，map参数，带附加头
     * @param url
     * @param paramsMap
     * @param headers
     * @param callback
     */
    public void post(String url, Map<String, Object> paramsMap, Map<String, String> headers, Callback callback) {
        doPost(url, paramsMap, headers, callback);
    }
    /****
     * post 请求,表单提交,不带附加头
     * @param url
     * @param formMap
     * @param callback
     */
    public void postForm(String url, Map<String, Object> formMap, Callback callback) {
        doPostForm(url, formMap, null, callback);
    }

    /***
     * post 请求，
     * @param url
     * @param formMap  url地址后接的参数
     * @param jsonObject
     * @param callback
     */
    public <Q extends BaseQuery> void postForm(String url,
                                               Map<String, Object> formMap, Q jsonObject,
                                               Callback callback) {
        Map jsonMap = object2Map(jsonObject);
        doPost(url, formMap, jsonMap, null, callback);
    }

    /***
     * post 请求，
     * @param url
     * @param formMap  url地址后接的参数
     * @param jsonObject
     * @param headers
     * @param callback
     */
    public <Q extends BaseQuery> void postForm(String url,
                                               Map<String, Object> formMap, Q jsonObject,
                                               Map<String, String> headers,
                                               Callback callback) {
        Map jsonMap = object2Map(jsonObject);
        doPost(url, formMap, jsonMap, headers, callback);
    }

    /****
     * post 请求,表单提交,带附加头
     * @param url
     * @param formMap
     * @param callback
     */
    public void postForm(String url, Map<String, Object> formMap, Map<String, String> headers, Callback callback) {
        doPostForm(url, formMap, headers, callback);
    }

    /***
     * post 请求，
     * @param url
     * @param formMap  url地址后接的参数
     * @param jsonMap
     * @param headers
     * @param callback
     */
    public void postForm(String url,
                         Map<String, Object> formMap, Map<String, Object> jsonMap,
                         Map<String, String> headers,
                         Callback callback) {
        doPost(url, formMap, jsonMap, headers, callback);
    }

    /****
     * post 请求,表单提交,带附加头
     * @param url
     * @param formObject
     * @param callback
     */
    public <Q extends BaseQuery> void postForm(String url, Q formObject, Callback callback) {
        doPostForm(url, object2Map(formObject), null, callback);
    }


    /****
     * post 请求,表单提交,带附加头
     * @param url
     * @param formObject
     * @param callback
     */
    public <Q extends BaseQuery> void postForm(String url, Q formObject, Map<String, String> headers, Callback callback) {
        doPostForm(url, object2Map(formObject), headers, callback);
    }

    /***
     * post 请求，
     * @param url
     * @param formObject  url地址后接的参数
     * @param jsonObject
     * @param callback
     */
    public <Q extends BaseQuery> void postForm(String url,
                                               Q formObject, Q jsonObject,
                                               Callback callback) {
        Map<String, Object> formMap = object2Map(formObject);
        Map<String, Object> jsonMap = object2Map(jsonObject);

        doPost(url, formMap, jsonMap, null, callback);
    }

    /***
     * post 请求，
     * @param url
     * @param formObject  url地址后接的参数
     * @param jsonObject
     * @param headers
     * @param callback
     */
    public <Q extends BaseQuery> void postForm(String url,
                                               Q formObject, Q jsonObject,
                                               Map<String, String> headers,
                                               Callback callback) {
        Map<String, Object> formMap = object2Map(formObject);
        Map<String, Object> jsonMap = object2Map(jsonObject);

        doPost(url, formMap, jsonMap, headers, callback);
    }

    /****
     * post 请求，body中json提交,不带附加头
     * @param url
     * @param object
     * @param callback
     */
    public <Q extends BaseQuery> void postJson(String url, Q object, Callback callback) {
        doPostJson(url, object2Map(object), null, callback);
    }

    /****
     * post 请求，body中json提交,带附加头
     * @param url
     * @param object
     * @param callback
     */
    public <Q extends BaseQuery> void postJson(String url, Q object, Map<String, String> headers, Callback callback) {
        doPostJson(url, object2Map(object), headers, callback);
    }

    /****
     * post 请求，body中json提交,不带附加头
     * @param url
     * @param jsonParamsMap
     * @param callback
     */
    public void postJson(String url, Map<String, Object> jsonParamsMap, Callback callback) {
        doPostJson(url, jsonParamsMap, null, callback);
    }

    /****
     * post 请求，body中json提交,带附加头
     * @param url
     * @param jsonParamsMap
     * @param callback
     */
    public void postJson(String url, Map<String, Object> jsonParamsMap, Map<String, String> headers, Callback callback) {
        doPostJson(url, jsonParamsMap, headers, callback);
    }

    /**
     * @param url
     * @param paramsMap     param
     * @param jsonParamsMap body
     * @param headers       head
     * @param callback
     */
    public void postJson(String url, Map<String, Object> paramsMap, Map<String, Object> jsonParamsMap, Map<String, String> headers, Callback callback) {
        doPostJson(url, paramsMap, jsonParamsMap, headers, callback);
    }


    /**
     * 上传文件
     *
     * @param url           地址
     * @param keyName       文件上传key
     * @param fileList      上传文件
     * @param paramsMap     表单参数
     * @param jsonParamsMap body参数
     * @param headers       header参数
     * @param callBack
     */
    public void postFiles(String url, String keyName, List<File> fileList, Map<String, Object> paramsMap, Map<String, Object> jsonParamsMap, Map<String, String> headers, Callback callBack) {
        doPostFile(url, keyName, fileList, paramsMap, jsonParamsMap, headers, callBack);
    }

    /***
     * post 上传文件
     * @param url
     * @param keyName
     * @param fileList
     * @param callBack
     */
    public void postFiles(String url, String keyName, List<File> fileList, Callback callBack) {
        doPostFile(url, keyName, fileList, null, null, null, callBack);
    }

    /***
     * post 上传文件
     * @param url
     * @param keyName
     * @param file
     * @param callBack
     */
    public void postFile(String url, String keyName, File file, Callback callBack) {
        List<File> fileList = new ArrayList<>();
        fileList.add(file);
        doPostFile(url, keyName, fileList, null, null, null, callBack);
    }

    /***************************** 下载  ****************/

    public void downLoad(String url, FileCallback fileCallback) {
        OkGo.<File>get(url)//
                .tag(cancelTag)//
                .execute(fileCallback);
    }

    public void downLoadByPost(String url, Map<String, Object> paramsMap,
                               Map<String, Object> jsonParamsMap,
                               Map<String, String> headers, FileCallback fileCallback) {
        OkGo.<File>post(url)//
                .tag(cancelTag)
                .params(addParams(paramsMap))
                .headers(addHeaders(headers))
                .upJson(addJson(jsonParamsMap))
                .execute(fileCallback);
    }


    /******************* cancel **************************/

    /***
     * 取消请求
     */
    public void cancel() {
        OkGo.getInstance().cancelTag(cancelTag);
    }


    /*********************  下方为不可外部访问方法 *********************/

    private void doGetJson(String url,
                           Map<String, Object> paramsMap,
                           Map<String, String> headers,
                           Callback callback) {
        OkGo.<BaseResponeEntity<String>>get(url)//
                .tag(cancelTag)//
                .params(addParams(paramsMap))
                .headers(addHeaders(headers))
                .execute(callback);
    }

    private void doPost(String url,
                            Map<String, Object> paramsMap,
                            Map<String, String> headers,
                            Callback callback) {

        OkGo.<BaseResponeEntity<String>>post(url)
                .tag(cancelTag)
                .headers(addHeaders(headers))
                .params(addParams(paramsMap))
                .execute(callback);
    }

    private void doPostJson(String url,
                            Map<String, Object> paramsMap,
                            Map<String, String> headers,
                            Callback callback) {

        OkGo.<BaseResponeEntity<String>>post(url)
                .tag(cancelTag)
                .headers(addHeaders(headers))
                .upJson(addJson(paramsMap))
                .execute(callback);
    }

    private void doPostJson(String url,
                            Map<String, Object> paramsMap,
                            Map<String, Object> jsonParamsMap,
                            Map<String, String> headers,
                            Callback callback) {

        OkGo.<BaseResponeEntity<String>>post(url)
                .tag(cancelTag)
                .params(addParams(paramsMap))
                .headers(addHeaders(headers))
                .upJson(addJson(jsonParamsMap))
                .execute(callback);
    }

    /***
     *  post提交表单
     * @param url   地址
     * @param paramsMap     表单参数
     * @param headersMap    header参数
     * @param callback      回调
     */
    private void doPostForm(String url,
                            Map<String, Object> paramsMap,
                            Map<String, String> headersMap,
                            Callback callback) {

        OkGo.<BaseResponeEntity<String>>post(url)//
                .tag(cancelTag)//
                .params(addParams(paramsMap))
                .headers(addHeaders(headersMap))
                .isMultipart(true)  //强制使用表单提交
                .execute(callback);
    }

    /***
     *
     * @param url
     * @param fileKey   文件key
     * @param files     文件集合
     * @param paramsMap     表单参数
     * @param jsonParamsMap     body参数
     * @param headersMap    header参数
     * @param callback      回调
     */
    private void doPostFile(String url,
                            String fileKey, List<File> files,
                            Map<String, Object> paramsMap,
                            Map<String, Object> jsonParamsMap,
                            Map<String, String> headersMap, Callback callback) {

        OkGo.<BaseResponeEntity<String>>post(url)
                .tag(cancelTag)//
                .addFileParams(fileKey, files)
                .params(addParams(paramsMap))
                .headers(addHeaders(headersMap))
                .upJson(addJson(jsonParamsMap))
                .isMultipart(true)
                .execute(callback);
    }

    /***
     *  post方法
     * @param url  地址
     * @param urlParamsMap  表单提交参数
     * @param jsonParamsMap     body 中的 json参数
     * @param headersMap    头参数
     * @param callback      回调
     */
    private void doPost(String url,
                        Map<String, Object> urlParamsMap,
                        Map<String, Object> jsonParamsMap,
                        Map<String, String> headersMap,
                        Callback callback) {
        OkGo.<BaseResponeEntity<String>>post(addUrlParams(url, urlParamsMap))
                .tag(cancelTag)
                .headers(addHeaders(headersMap))
                .upJson(addJson(jsonParamsMap))
                .execute(callback);
    }


    /**********************  转化方法 *********************/

    private String addJson(Map<String, Object> jsonParamsMap) {
        if (jsonParamsMap == null || jsonParamsMap.isEmpty()) {
            return null;
        }
        return GsonUtils.toJsonString(jsonParamsMap);
    }

    private HttpHeaders addHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> stringStringEntry : headers.entrySet()) {
                httpHeaders.put(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }
        return httpHeaders;
    }

    private HttpParams addParams(Map<String, Object> paramsMap) {
        HttpParams httpParams = new HttpParams();
        if (paramsMap == null || paramsMap.isEmpty()) {
            return httpParams;
        }
        for (Map.Entry<String, Object> stringObjectEntry : paramsMap.entrySet()) {
            if (stringObjectEntry.getValue() instanceof String) {
                httpParams.put(stringObjectEntry.getKey(), (String) stringObjectEntry.getValue());
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Integer) {
                httpParams.put(stringObjectEntry.getKey(), (int) stringObjectEntry.getValue());
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Float) {
                httpParams.put(stringObjectEntry.getKey(), (float) stringObjectEntry.getValue());
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Double) {
                httpParams.put(stringObjectEntry.getKey(), (double) stringObjectEntry.getValue());
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Long) {
                httpParams.put(stringObjectEntry.getKey(), (long) stringObjectEntry.getValue());
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Boolean) {
                httpParams.put(stringObjectEntry.getKey(), (boolean) stringObjectEntry.getValue());
                continue;
            }
            httpParams.put(stringObjectEntry.getKey(), stringObjectEntry.getValue().toString());
        }
        return httpParams;
    }

    protected Map object2Map(Object object) {
        if (object == null) {
            return new HashMap();
        }
        JsonElement str = GsonUtils.jsonElementFrom(GsonUtils.toJsonString(object));
        Map paramsMap = GsonUtils.fromJson(str, new TypeToken<Map>() {
        }.getType());

        return paramsMap;
    }


    private String addUrlParams(String baseUrl, Map<String, Object> urlParams) {
        if (urlParams == null || urlParams.isEmpty()) {
            return baseUrl;
        }
        String url = baseUrl + "?";
        for (Map.Entry<String, Object> stringObjectEntry : urlParams.entrySet()) {
            if (stringObjectEntry.getValue() instanceof String) {
                url += stringObjectEntry.getKey() + "=" + (String) stringObjectEntry.getValue() + "&";
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Integer) {
                url += stringObjectEntry.getKey() + "=" + (int) stringObjectEntry.getValue() + "&";
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Float) {
                url += stringObjectEntry.getKey() + "=" + (float) stringObjectEntry.getValue() + "&";
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Double) {
                url += stringObjectEntry.getKey() + "=" + (double) stringObjectEntry.getValue() + "&";
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Long) {
                url += stringObjectEntry.getKey() + "=" + (long) stringObjectEntry.getValue() + "&";
                continue;
            }
            if (stringObjectEntry.getValue() instanceof Boolean) {
                url += stringObjectEntry.getKey() + "=" + (boolean) stringObjectEntry.getValue() + "&";
                continue;
            } else {
                url += stringObjectEntry.getKey() + "=" + stringObjectEntry.getValue().toString() + "&";
            }
        }
        return url.substring(0, url.length() - 1);
    }

}
