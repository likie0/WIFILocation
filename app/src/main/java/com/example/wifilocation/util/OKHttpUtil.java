package com.example.wifilocation.util;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpUtil {
    private static Request request = null;
    private static Call call = null;
    private static int TimeOut = 120;

    //单例获取http3对象
    private static OkHttpClient client = null;
    /**
     * OkHttpClient的构造方法，通过线程锁的方式构造
     * @return OkHttpClient对象
     */
    private static synchronized OkHttpClient getInstance() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .readTimeout(TimeOut, TimeUnit.SECONDS)
                    .connectTimeout(TimeOut, TimeUnit.SECONDS)
                    .writeTimeout(TimeOut, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

    /**
     * callback接口
     * 异步请求时使用
     */
    static class MyCallBack implements Callback {
        private OkHttpCallback okHttpCallBack;

        public MyCallBack(OkHttpCallback okHttpCallBack) {
            this.okHttpCallBack = okHttpCallBack;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            okHttpCallBack.onFailure(e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            okHttpCallBack.onSuccess(response);
        }
    }
    /**
     * 获得同步get请求对象Response
     * @param url
     * @return Response
     */
    private static Response doSyncGet(String url) {
        //创建OkHttpClient对象
        client = getInstance();
        request = new Request.Builder()
                .url(url)//请求链接
                .build();//创建Request对象
        try {
            //获取Response对象
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获得异步get请求对象
     * @param url      请求地址
     * @param callback 实现callback接口
     */
    private static void doAsyncGet(String url,OkHttpCallback callback) {
        MyCallBack myCallback = new MyCallBack(callback);
        client = getInstance();
        request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(myCallback);
    }

    /**
     * 同步post请求
     * 例如：请求的最终地址为：http://127.0.0.1:8081/user/getUser/123
     * @param url 基本请求地址   例子： http://127.0.0.1:8081
     * @param json 提交的json字符串
     * @param args 请求的参数    args[]=new String[]{"user","getUser","123"}
     * @return
     */
    public static String postSyncRequest(String url,String json,String... args){
        List<String> result=new ArrayList<>();
        String address=url;
        for(int i=0;i<args.length;i++){
            address=address+"/"+args[i];
        }
        final String finalAddress = address;
        new Thread(new Runnable() {
            @Override
            public void run() {
                client=getInstance();
                Log.d("同步post请求地址：",finalAddress);
                FormBody.Builder formBody = new FormBody.Builder();
                Log.d("传给服务器的json",json);
                formBody.add("json",json);
                request=new Request.Builder()
                        .url(finalAddress)
                        .post(formBody.build())
                        .addHeader("device-platform", "android")
                        .build();
                try{
                    Response response=client.newCall(request).execute();
                    String res=response.body().string();
                    result.add(res);
                    Log.d("HttpUtil", "同步post请求成功！");
                    Log.d("请求对象：", res);
                }catch (Exception e){
                    Log.d("HttpUtil", "同步post请求失败！");
                    e.printStackTrace();
                }
            }
        }).start();
        /**因为函数返回是立刻执行的，而result要在请求完成之后才能获得
         * 所以需要等待result获得返回值之后再执行return*/
        while(result.size()==0){
            try {
                TimeUnit.MILLISECONDS.sleep(10);//等待xx毫秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result.get(0);
    }

}
