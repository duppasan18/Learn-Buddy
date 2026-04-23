package com.pasan.gateway.routers;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicRouterLoader {

    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final ApplicationEventPublisher publisher;

    private final String dataId = "gateway-routers.yaml";
    private final String group = "DEFAULT_GROUP";

    // 当前已加载的路由ID
    private final Set<String> routeIds = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() throws NacosException {

        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        log.info("监听到路由配置变化");
                        updateRoutes(configInfo);
                    }
                });

        // 首次启动更新路由表
        updateRoutes(configInfo);
    }

    /**
     * 更新路由
     */
    public void updateRoutes(String configInfo) {
        try {
            log.info("加载路由配置：\n{}", configInfo);

            List<RouteDefinition> routes = parseRoutes(configInfo);

            if (routes.isEmpty()) {
                log.error("未解析到任何路由！");
                return;
            }

            // 删除旧路由
            Flux<Void> deleteFlux = Flux.fromIterable(routeIds)
                    .flatMap(id -> routeDefinitionWriter.delete(Mono.just(id))
                            .onErrorResume(e -> Mono.empty()));

            // 新增路由
            Flux<Void> saveFlux = Flux.fromIterable(routes)
                    .flatMap(route -> routeDefinitionWriter.save(Mono.just(route)));

            // 执行 + 刷新
            deleteFlux
                    .thenMany(saveFlux)
                    .doOnComplete(() -> {
                        routeIds.clear();
                        routes.forEach(r -> routeIds.add(r.getId()));

                        log.info("动态路由刷新完成，共{}条", routes.size());

                        // 刷新网关缓存
                        publisher.publishEvent(new RefreshRoutesEvent(this));
                    })
                    .doOnError(e -> log.error("路由刷新失败", e))
                    .subscribe(); // 订阅

        } catch (Exception e) {
            log.error("解析路由配置失败", e);
        }
    }

    /**
     * RouteDefinition转换
     * 将yaml格式转换为RouteDefinition
     */
    private List<RouteDefinition> parseRoutes(String configInfo) {

        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(configInfo);

        List<Map<String, Object>> routeList =
                (List<Map<String, Object>>) map.get("routes");

        if (routeList == null) {
            return Collections.emptyList();
        }

        List<RouteDefinition> result = new ArrayList<>();

        for (Map<String, Object> item : routeList) {

            RouteDefinition rd = new RouteDefinition();

            // id
            rd.setId((String) item.get("id"));

            // uri
            rd.setUri(URI.create((String) item.get("uri")));

            // predicates
            List<String> predicates = (List<String>) item.get("predicates");
            if (predicates != null) {
                List<PredicateDefinition> predicateDefs = new ArrayList<>();
                for (String p : predicates) {
                    predicateDefs.add(new PredicateDefinition(p));
                }
                rd.setPredicates(predicateDefs);
            }

            // filters（可选）
            List<String> filters = (List<String>) item.get("filters");
            if (filters != null) {
                List<FilterDefinition> filterDefs = new ArrayList<>();
                for (String f : filters) {
                    filterDefs.add(new FilterDefinition(f));
                }
                rd.setFilters(filterDefs);
            }

            result.add(rd);
        }

        return result;
    }
}