package com.viratech.proxy;

import com.viratech.dto.ExchangeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "exchange-service")
public interface ExchangeProxy {

    @GetMapping(value = "/exchange-service/{amount}/{from}/{to}")
    public ExchangeRequest getExchange(@PathVariable BigDecimal amount,
                                       @PathVariable String from,
                                       @PathVariable String to);

}
