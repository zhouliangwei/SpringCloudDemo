package com.zlw.serviceribbon;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RibbonService {

    @Autowired
    RestTemplate restTemplate;

    //接口级别配置超时时间（还有其他多种方法，需要后期调查）
    @HystrixCommand(fallbackMethod = "hiError",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000" )
            })
    public String hiRibbon(){
        return restTemplate.getForObject("http://EUREKA-SERVICE/hello",String.class);
    }

    public String hiError(){
        return "Sorry, this is hystrix!";
    }

}
