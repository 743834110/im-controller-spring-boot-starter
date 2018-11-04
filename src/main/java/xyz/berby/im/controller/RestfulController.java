package xyz.berby.im.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/rest")
public class RestfulController extends AbstractRestfulController{

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationContext context;


    @RequestMapping(value = "/te", method = {RequestMethod.POST, RequestMethod.GET})
    public Object handleHttpRequest(HttpServletRequest httpServletRequest) {
        httpServletRequest.getParameterMap();
        return "fdfdfdfdfffd";
    }

    @RequestMapping("/{service}/{operate}")
    public Object getCommonDeal(@PathVariable String service
            , @PathVariable String operate
            , HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        return super.getCommandDeal(service, operate, httpServletRequest, httpServletResponse);
    }





}
