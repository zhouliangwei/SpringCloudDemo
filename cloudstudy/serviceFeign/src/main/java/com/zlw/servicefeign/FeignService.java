package com.zlw.servicefeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "EUREKA-SERVICE",fallback = FeignServiceFallBack.class)
@Repository //加上这个在controller层就不会有红叉（红叉是因为这个Bean是在程序启动的时候注入的，编译器感知不到，所以报错）
public interface FeignService {
    @GetMapping("/hello")
    String hiFeign();
}
