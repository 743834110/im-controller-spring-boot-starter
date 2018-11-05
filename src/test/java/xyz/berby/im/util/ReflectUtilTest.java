package xyz.berby.im.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ClassUtil;
import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.Test;
import org.omg.PortableInterceptor.ServerIdHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.berby.im.entity.ServerConfig;
import xyz.berby.im.vo.Pager;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class ReflectUtilTest {


    Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ServerConfig serverConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setConfigId("fdfd");
        serverConfig.setCreatePerson("fdfdfd");
        return serverConfig;
    }

    @Test
    public void testJsonForParamOne() {
        ServerConfig serverConfig = serverConfig();
        ServerConfig[] configs = new ServerConfig[] {serverConfig};
        String string = JSON.toJSONString(configs);
        logger.info(string);
        ServerConfig[] serverConfigs = (ServerConfig[]) ReflectUtil.jsonForParam(configs.getClass(), new String[]{string}, null);
        System.out.println(serverConfigs[0]);
    }

    @Test
    public void testJsonForParamTwo() {

        String[] strings = new String[] {
          JSON.toJSONString(serverConfig()),
          JSON.toJSONString(serverConfig())
        };
        ServerConfig[] serverConfigs = (ServerConfig[]) ReflectUtil.jsonForParam(ServerConfig[].class, strings, null);
    }

    @Test
    public void testJsonForParamThree() {
        ServerConfig serverConfig = (ServerConfig) ReflectUtil.jsonForParam(ServerConfig.class
                , new String[] {JSON.toJSONString(serverConfig())}, null);
        System.out.println(serverConfig);
    }

    /**
     * 获取bean字段getter,setter方法
     */
    @Test
    public void testGetParamValues() throws IntrospectionException, IllegalAccessException, InstantiationException {
        Object pager = Pager.class.newInstance();
        Map<String, PropertyDescriptor> map =
                BeanUtil.getPropertyDescriptorMap(pager.getClass(), false);
        for (Map.Entry<String, PropertyDescriptor> entry: map.entrySet()) {
            Method method = entry.getValue().getWriteMethod();
            if (method == null)
                continue;
            Class<?> paramType = method.getParameterTypes()[0];
            System.out.println(paramType + ":" + ClassUtil.isSimpleTypeOrArray(paramType));
        }
    }


    public static void page(String a, Pager<ServerConfig> pager) {

    }

    /**
     * 获取方法参数中的泛型类型
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void test() throws NoSuchMethodException, IllegalAccessException, InstantiationException {

        Method method = this.getClass().getMethod("page",String.class, Pager.class);
        Type[] types = method.getGenericParameterTypes();
        for (Type type: types) {
            if (type instanceof ParameterizedType) {
                System.out.println(((ParameterizedType) type).getRawType());
            }
        }
    }

    @Test
    public void testDynamicNewInstance() {
    }
}
