package xyz.berby.im.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xyz.berby.im.entity.ServerConfig;
import xyz.berby.im.service.QueryOrgService;
import xyz.berby.im.vo.ReqBody;

@Service("queryOrgService")
public class QueryOrgServiceImpl implements QueryOrgService {

    private Logger logger = LoggerFactory.getLogger(QueryOrgService.class);

    public void queryList(ReqBody<ServerConfig>[] reqBody) {
        System.out.println(reqBody[0].getParam().getConfigId());
    }

    public void queryServerConfig(ServerConfig[] configs) {
        System.out.println(configs);
    }

    public void queryString(String[] strings) {
        System.out.println(strings);
    }
}
