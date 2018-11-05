package xyz.berby.im.controller;

import xyz.berby.im.util.ApplicationContextHolder;
import xyz.berby.im.util.ReflectUtil;
import xyz.berby.im.vo.RespBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

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
            , HttpServletRequest request
            , HttpServletResponse response, String string) {

        // 先决检验

        //
        Object service = null;
        try {
            service = ApplicationContextHolder.getBean(serviceName);
        } catch (Exception e){}

        if (service == null) {
            return new RespBody("service: " + serviceName + " not found!", response);
        }

        // 各种switch

        // 直接查找服务方法并运行
        String path = serviceName + ":" + operateName;
        Method method = PATH_METHOD_MAPPING.get(path);
        method = method == null? ReflectUtil.getMethod(service, operateName): method;
        if (method == null) {
            return new RespBody( serviceName + "." + operateName + "() not found!", response);
        }
        // 缓存，保证性能
        PATH_METHOD_MAPPING.put(path, method);

        Map<String, String[]> params = request.getParameterMap();


        Object data = null;
        try {
            // 处理消息体提交的数据
            Map<String, String[]> stringMap = null;
            if (string != null) {
                stringMap = this.handleString(string);
            }
            else {
                stringMap = new HashMap<>();
            }
            if (stringMap != null) {
                for (Map.Entry<String, String[]> entry : params.entrySet()) {
                    stringMap.put(entry.getKey(), entry.getValue());
                }
            }
            // 开始反射到服务类的方法
            Object[] paramValues = ReflectUtil.getParamValues(stringMap, method);
            if (paramValues == null) {
                data = method.invoke(service);
            }
            else {
                data = method.invoke(service, paramValues);
            }

        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null || message.equals("")) {
                message = "参数异常";
            }
            e.printStackTrace();
            return new RespBody(400, message, response);
        }


        return new RespBody(data, response);
    }

    /**
     * <pre>
     * 解析自定义的数据解析格式
     * 当前定义的解析格式为:
     * {
     *     某方法参数名：{
     *         参数类内部的数据的键值对表现方式
     *     }
     * }
     * 如服务queryOrgService.queryList(ReqBody<ServerConfig> reqBody);
     * string的格式应为
     * {
     *     reqBody: {
     *         param: {
     *             ...
     *         },
     *         serverConfig: {
     *             ...
     *         }
     *         ...
     *     }
     *     ...
     * }
     * 转换后的map数据应为
     * key => reqBody
     * value => {
     *     param: {
     *         ...
     *     },
     *     serverConfig: {
     *         ...
     *     }
     *     ...
     * }
     * 一个方法中有多个对象也类似编排即可
     * 不过要注意的是，如果post消息体post提交中带有相同的key值，那么post提交的数据将会
     * 覆盖post消息体的消息
     * </pre>
     * @param string 未解析的数据
     * @return map
     */
    abstract Map<String, String[]> handleString(String string);

}
