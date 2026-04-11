package com.pasan.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "")
public class DynamicRouteConfig {

    private List<RouteDefinition> routes;
}
