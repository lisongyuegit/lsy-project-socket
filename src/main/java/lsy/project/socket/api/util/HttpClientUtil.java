package lsy.project.socket.api.util;

import java.io.IOException;

import com.alibaba.fastjson.JSON;

import com.lsy.base.result.ResultVo;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class HttpClientUtil {

    //健值对s
    public static final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    public static ResultVo httpPost(String url, FormBody formBody, String token, MediaType content_type) {
        ResultVo resultVo = new ResultVo();
        OkHttpClient client = new OkHttpClient();
        String resultJson = "";
        // 创建请求方式
        Request request = null;
        if (null != token && token.length() > 0) {
            request = new Request.Builder().addHeader("Authorization", "Bearer " + token).url(url).post(formBody).build();
        } else {
            request = new Request.Builder().url(url).post(formBody).build();
        }
        // 执行请求操作
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                resultJson = response.body().string();
                resultVo = JSON.parseObject(resultJson, ResultVo.class);
            } else {
                resultVo.setError_no(response.code());
                resultVo.setError_info(response.message());
            }
        } catch (Exception e) {
            resultVo.setError_no(-1);
            resultVo.setError_info(e.getMessage());
        }
        return resultVo;
    }

    // get请求
    public static ResultVo httpGet(String url, String token) {
        ResultVo resultVo = new ResultVo();
        OkHttpClient client = new OkHttpClient();
        String resultJson = "";

        // 创建请求方式
        Request request = new Request.Builder().addHeader("Authorization", "Bearer " + token).url(url).get().build();

        // 执行请求操作
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                resultJson = response.body().string();
                resultVo = JSON.parseObject(resultJson, ResultVo.class);
            } else {
                resultVo.setError_no(response.code());
                resultVo.setError_info(response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultVo;
    }

    // delete请求
    public static ResultVo httpDelete(String url, String token, String batch) {
        ResultVo resultVo = new ResultVo();
        OkHttpClient client = new OkHttpClient();
        String resultJson = "";

        // 创建请求方式
        Request request = null;
        if (null != batch && batch.length() > 0) {
            // 批量操作
            request = new Request.Builder().addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json").url(url).delete().build();
        } else {
            request = new Request.Builder().addHeader("Authorization", "Bearer " + token).url(url).delete().build();
        }
        // 执行请求操作
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                resultJson = response.body().string();
                resultVo = JSON.parseObject(resultJson, ResultVo.class);
            } else {
                resultVo.setError_no(response.code());
                resultVo.setError_info(response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultVo;
    }

    // put请求
    public static ResultVo httpUpdate(String url, String paramJson, String token, MediaType content_type) {
        ResultVo resultVo = new ResultVo();
        OkHttpClient client = new OkHttpClient();
        String resultJson = "";

        // 组装参数
        RequestBody body = RequestBody.create(content_type, paramJson);
        Request request = null;
        try {
            // 创建请求方式
            request = new Request.Builder().addHeader("Authorization", "Bearer " + token).url(url).put(body).build();
        } catch (Exception e) {
            System.out.println(e);
        }
        // 执行请求操作
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                resultJson = response.body().string();
                resultVo = JSON.parseObject(resultJson, ResultVo.class);
            } else {
                resultVo.setError_no(response.code());
                resultVo.setError_info(response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultVo;
    }

}
