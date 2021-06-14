package com.worth.bluetooth.base.network.bean;

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/30/21 --> 8:21 PM
 * Description: This is ResultData
 */
public class ResultData {
    public ResultData() {
    }
    private String result;      //  返回结果
    private boolean success;    //  返回是否成功
    private int code;           //  200  500
    private long timestamp;     //  时间戳

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ResultData{" +
                "result='" + result + '\'' +
                ", success=" + success +
                ", code=" + code +
                ", timestamp=" + timestamp +
                '}';
    }
}
