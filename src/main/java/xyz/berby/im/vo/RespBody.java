package xyz.berby.im.vo;

import com.alibaba.fastjson.JSON;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;

/**
 * http响应对象
 * @author litianfeng
 * Created on 2018/11/4
 */
public class RespBody {


    /**
     * http状态码
     */
    private int code;
    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private Object data;

    public RespBody(Object data, HttpServletResponse response) {
        this.data = data;
        this.code = HttpStatus.OK.value();
        if (response != null) {
            response.setStatus(this.code);
        }
    }

    public RespBody(String message, HttpServletResponse response) {
        this.message = message;
        this.code = HttpStatus.NOT_FOUND.value();
        if (response != null) {
            response.setStatus(this.code);
        }
    }

    public RespBody(int code, String message, HttpServletResponse response) {
        this.message = message;
        this.code = code;
        if (response != null) {
            response.setStatus(this.code);
        }
    }

    public RespBody(int code, HttpServletResponse response) {
        this.code = code;
        if (response != null) {
            response.setStatus(code);
        }
    }





    public Object getData() {
        return data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "RespBody{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
