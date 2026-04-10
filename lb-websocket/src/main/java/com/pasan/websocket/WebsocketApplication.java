package com.pasan.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@Slf4j
public class WebsocketApplication {

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplicationBuilder(WebsocketApplication.class).build(args);
        Environment env = app.run(args).getEnvironment();

        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }

        log.info("\n***************************************************************************************\n\t" +
                        "应用已启动！访问地址如下：\n\t" +
                        "本地访问: \t\t{}://localhost:{}\n\t" +
                        "外部访问: \t{}://{}:{}\n\t" +
                        "当前环境配置: \t{}" +
                        "\n***************************************************************************************",
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getActiveProfiles());
    }

}
