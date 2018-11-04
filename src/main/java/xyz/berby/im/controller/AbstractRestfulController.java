package xyz.berby.im.controller;

import xyz.berby.im.util.ApplicationContextHolder;
import xyz.berby.im.util.ReflectUtil;
import xyz.berby.im.vo.RespBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

/**
 * 抽象控制类
 * 用于控制层的封装
 */
abstract class AbstractRestfulController {

    /**
     * 路径到方法的映射
     */
    private final static Map<String, Method> PATH_METHOD_MAPPING = new TreeMap<>();

    /**
     *  通用处理方法
     * @param serviceName 服务名
     * @param operateName 操作
     * @param request 请求对象
     * @param response 响应对象
     * @return
     */
    Object getCommandDeal(String serviceName, String operateName
            , HttpServletRequest request, HttpServletResponse response) {

        // 先决检验

        //
        Object service = ApplicationContextHolder.getBean(serviceName);
        if (service == null) {
            return new RespBody("service:" + serviceName + "not found!", response);
        }

        // 各种switch

        // 直接查找服务方法并运行
        String path = serviceName + ":" + operateName;
        Method method = PATH_METHOD_MAPPING.get(path);
        method = method == null? ReflectUtil.getMethod(service, operateName): method;
        if (method == null) {
            return new RespBody( serviceName + ".:" + operateName + "not found!", response);
        }

        Map<String, String[]> params = request.getParameterMap();
        try {
            Object[] paramValues = ReflectUtil.getParamValues(params, method);
        } catch (Exception e) {

        }

        return null;
    }



}
