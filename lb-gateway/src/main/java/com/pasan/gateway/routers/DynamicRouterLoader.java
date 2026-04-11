package com.pasan.gateway.routers;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.pasan.gateway.config.DynamicRouteConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicRouterLoader {
    // 配置管理器实例
    private final NacosConfigManager nacosConfigManager;
    //路由更新器实例
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final ConfigurableEnvironment environment;

    //private final String dataId = "shared-gateway-config.yaml";
    private final String dataId = "gateway-routers.yaml";
    private final String group = "DEFAULT_GROUP";

    private final Set<String> routeIds = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void initRouteConfigListener() throws NacosException {
        // 项目启动前拉取配置，并添加监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        //监听配置变更更新路由表
                        updateConfig(configInfo);
                    }
                });
        // 首次启动更新路由表
        updateConfig(configInfo);
    }

    public void updateConfig(String configInfo){
        try {
            log.info("监听到更新的路由表：{}", configInfo);
            // 解析配置文件
            MapPropertySource propertySource = new MapPropertySource("dynamic-route-config",
                    new Yaml().load(configInfo));

            // 放入环境
            environment.getPropertySources().addFirst(propertySource);
            // 绑定配置
            DynamicRouteConfig config = Binder.get(environment)
                    .bind("", Bindable.of(DynamicRouteConfig.class))
                    .orElseThrow(() -> new RuntimeException("路由解析失败"));

            List<RouteDefinition> routes = config.getRoutes();
            if(CollUtil.isEmpty(routes)){
                log.error("没有读取到任何路由配置");
                return;
            }

            // 删除旧路由表，添加新路由
            Flux<Void> deleteFlux = Flux.fromIterable(routeIds)
                            .flatMap(id -> routeDefinitionWriter.delete(Mono.just(id)));

            Flux<Void> saveFlux = Flux.fromIterable(routes)
                    .flatMap(route -> routeDefinitionWriter.save(Mono.just(route)));

            deleteFlux
                    .thenMany(saveFlux)
                    .doOnComplete(()->{
                        routeIds.clear();
                        routes.forEach(route -> routeIds.add(route.getId()));
                        log.info("刷新了{}条路由",routes.size());
                    })
                    .doOnError(e->log.error("路由刷新失败：{}",e.getMessage()));
        } catch (RuntimeException e) {
            log.error("路由解析失败：{}", e.getMessage());
        }
    }



}
