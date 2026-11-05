package com.viratech.controller;

import com.viratech.environment.InstanceInformationService;
import com.viratech.model.Exchange;
import com.viratech.service.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "Exchange Endpoint")
@RestController
@RequestMapping("/exchange-service")
public class ExchangeController {

    private Logger logger = LoggerFactory.getLogger(ExchangeController.class.getSimpleName());

    @Autowired
    InstanceInformationService informationService;

    @Autowired
    ExchangeService service;

    /*@GetMapping(value = "{amount}/{from}/{to}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Exchange getExchange(@PathVariable BigDecimal amount,
                                @PathVariable String from,
                                @PathVariable String to){
        return new Exchange(
                1L, from, to, BigDecimal.ONE, BigDecimal.ONE, "PORT " + informationService.retrieveServerPort());
    }*/

    @Operation(summary = "Convert currency from book service")
    @GetMapping(value = "{amount}/{from}/{to}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Exchange getExchange(@PathVariable BigDecimal amount,
                                @PathVariable String from,
                                @PathVariable String to){

        logger.info("getExchange is called with -> {}, {}, {}", amount, from,to);
        return service.find(amount, from, to);
    }

}
