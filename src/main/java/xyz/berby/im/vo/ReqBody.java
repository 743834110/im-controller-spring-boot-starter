package xyz.berby.im.vo;

import xyz.berby.im.entity.ServerConfig;

public class ReqBody<T> {


    private T param;

    private ServerConfig serverConfig;


    public T getParam() {
        return param;
    }

    public void setParam(T param) {
        this.param = param;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }


}
