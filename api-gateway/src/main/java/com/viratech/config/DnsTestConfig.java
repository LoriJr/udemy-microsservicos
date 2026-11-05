package com.viratech.config;

import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;


@Configuration
public class DnsTestConfig {

    @Bean
    public HttpClient gatewayHttpClient(){
        return HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE);
    }
}
