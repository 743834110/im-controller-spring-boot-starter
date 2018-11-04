package xyz.berby.im.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.springframework.util.ClassUtils;
import org.springframework.web.multipart.MultipartFile;
import xyz.berby.im.entity.ServerConfig;
import xyz.berby.im.vo.Pager;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.alibaba.fastjson.JSON.parseArray;

public class ReflectUtil {


    /**
     * 根据方法名称获取方法对象
     * @param service
     * @param methodName
     * @return
     */
    public static Method getMethod(Object service, String methodName) {

        Method[] methods = service.getClass().getMethods();
        for (Method method: methods) {
            if (method.getName().equals(methodName))
                return method;
        }
        return  null;
    }

    /**
     * 获取json数据到入参字段的映射
     * @param paramType 方法入参类型
     * @param value 对应入参待定方法的值
     * @return object
     */
    public static Object jsonForParam(Class<?> paramType, Object[] value) {
        String[] datas = Convert.convert(String[].class, value);
        // 数组对象时:[{}, {}, {}]
        if (paramType.isArray() && datas.length == 1 && datas[0].startsWith("[")) {
            Class<?> componentType = paramType.getComponentType();
            String data = datas[0];
            List<?> list =  JSON.parseArray(data, componentType);
            return Convert.convert(paramType, list);
        }
        // 处理new String("{...}", "{...}")
        else if (paramType.isArray() && datas[0].startsWith("{")) {
            Class<?> componentType = paramType.getComponentType();
            Object[] objects = new Object[datas.length];
            for (int i = 0; i < datas.length; i++) {
                String data = datas[i];
                objects[i] = JSON.parseObject(data, componentType);
            }
            return Convert.convert(paramType, objects);
        }
        // 处理{...}
        else if (!paramType.isArray() && datas.length == 1) {
            return JSON.parseObject(datas[0], paramType);
        }

        return null;
    }

    /**
     * 动态生成指定类型对象
     * 暴力扫描params，实例化paraType
     * 如果某方法入参为非基本类型，而且它现在的类型就是object,那么就去推测它可能时泛型擦除而生的
     * 过来的可能时serverConfig 或者 Pager<ServerConfig>
     * @return
     */
    public static Object dynamicNewInstance(Class<?> paramType, Class<?> genericType, Map<String, String[]> params) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        Object target = paramType.newInstance();
        Object genericTarget = null;
        Object concreteTarget = null;
        Map<String, PropertyDescriptor> concreteDescriptorMap = null;
        Map<String, PropertyDescriptor> genericDescriptorMap = null;
        Map<String, PropertyDescriptor> descriptorMap =
                BeanUtil.getPropertyDescriptorMap(paramType, false);
        // 泛型
        if (genericType != null) {
            genericTarget = genericType.newInstance();
            genericDescriptorMap = BeanUtil.getPropertyDescriptorMap(genericType, false);
        }

        for (Map.Entry<String, String[]> entry: params.entrySet()) {
            String paramName = entry.getKey();
            String[] paramValue = entry.getValue();
            // 没有点的，注入基本类型
            int index = paramName.indexOf('.');
            if (index == -1 && ClassUtil.isSimpleTypeOrArray(paramType)) {
                invokeMethodWithPropertyDescriptor(descriptorMap, paramName, target, paramValue);
            }
            // 有点, 为泛型,如Pager<ServerConfig>
            else if (genericTarget != null){
                String latterFieldName = paramName.substring(index + 1);
                invokeMethodWithPropertyDescriptor(genericDescriptorMap, latterFieldName, genericTarget, paramValue);
            }
            // 有点，不为泛型,如ServerConfig
            // FIX-ME
            else {
                String upperFieldName = paramName.substring(0, index);
                String latterFieldName = paramName.substring(index + 1);
                PropertyDescriptor descriptor = descriptorMap.get(upperFieldName);
                if (descriptor == null) {
                    continue;
                }
                Method method = descriptor.getWriteMethod();
                method.setAccessible(true);
                // 检查参数
                Class<?>[] clazzes = method.getParameterTypes();
                if (clazzes.length != 1) {
                    throw new RuntimeException("参数不为1");
                }
                Class<?> clazz = clazzes[0];
                Map<String, PropertyDescriptor> latterDescriptorMap =
                        BeanUtil.getPropertyDescriptorMap(clazz, false);


            }
        }
        return target;
    }

    /**
     * 方法调用
     * @param descriptorMap
     * @param paramName
     * @param target
     * @param paramValue
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void invokeMethodWithPropertyDescriptor(Map<String, PropertyDescriptor> descriptorMap
            , String paramName, Object target, String[] paramValue) throws InvocationTargetException, IllegalAccessException {
        PropertyDescriptor descriptor = descriptorMap.get(paramName);
        if (descriptor == null) {
            return;
        }
        Method method = descriptor.getWriteMethod();
        method.setAccessible(true);
        // 检查参数
        Class<?>[] clazzes = method.getParameterTypes();
        if (clazzes.length != 1) {
            throw new RuntimeException("参数不为1");
        }
        Class<?> clazz = clazzes[0];
        if (clazz.isArray()) {
            method.invoke(target, Convert.convert(clazz, paramValue));
        }
        else {
            method.invoke(target, Convert.convert(clazz, paramValue[0]));
        }
    }

    /**
     * 根据map获取入传参数得知
     * @return
     */
    public static Object[] getParamValues(Map<String, String[]> params, Method method) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        Class<?>[] paramTypes = method.getParameterTypes();
        Type[] types = method.getGenericParameterTypes();
        // 某字段泛型类型
        Class<?> genericType = null;

        if (paramTypes.length == 0) {
            return null;
        }

        Object[] paramValues = new Object[paramTypes.length];
        Paranamer paranamer = new BytecodeReadingParanamer();
        String[] parameterNames = paranamer.lookupParameterNames(method, false);

        for (int i = 0; types != null && i < types.length ; i++) {

            Class<?> paramType = null;
            // 检查该注入方法是否含有泛型类
            Type type = types[i];
            if (type instanceof ParameterizedType) {
                genericType = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
                paramType = (Class<?>) ((ParameterizedType) type).getRawType();
            }
            else {
                genericType = null;
                paramType = (Class<?>) type;
            }
            // 从传输过来的map中获取对象方法字段中的值
            Object[] value = params.get(parameterNames[i]);

            // 原始类型处理
            if (ClassUtil.isSimpleTypeOrArray(paramType)
                    || paramTypes[i].isAssignableFrom(MultipartFile.class)) {

                if (value != null && value.length > 0) {
                    paramValues[i] = paramType.isArray()?
                            Convert.convert(paramType, value): Convert.convert(paramType, value[0]);
                }
            }
            // 其他类的处理，value存在时，此时客户端传送过来到的数据可能时json数据
           else if (value != null && value.length > 0) {

               paramValues[i] = jsonForParam(paramType, value);
            }
            // 其他类型处理
            // 实体类型映射:value为空时
            else {

                paramValues[i] = dynamicNewInstance(paramType, genericType, params);

            }
        }

        return null;
    }

}
