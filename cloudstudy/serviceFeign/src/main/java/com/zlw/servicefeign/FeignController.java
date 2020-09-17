package com.zlw.servicefeign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeignController {
    @Autowired
    FeignService feignService;

    @GetMapping("/feign")
    public String hiFeign(){
        return  feignService.hiFeign();
    }
}
