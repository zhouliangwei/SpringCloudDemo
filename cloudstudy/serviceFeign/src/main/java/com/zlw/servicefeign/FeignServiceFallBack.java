package com.zlw.servicefeign;

import org.springframework.stereotype.Component;

@Component
public class FeignServiceFallBack implements  FeignService {
    @Override
    public String hiFeign() {
        return "sorry,this is feign hystrix!";
    }
}
