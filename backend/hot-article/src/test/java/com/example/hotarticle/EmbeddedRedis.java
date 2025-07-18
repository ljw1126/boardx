package com.example.hotarticle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@TestConfiguration
public class EmbeddedRedis {
    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void start() throws IOException {
        int port = isRedisRunning() ? findAvailablePort() : redisPort;
        this.redisServer = new RedisServer(port);
        this.redisServer.start();
    }

    private boolean isRedisRunning() throws IOException {
        return isRunning(executeGrepProcessCommand(redisPort));
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            this.redisServer.stop();
        }
    }

    /**
     * 현재 PC/서버에서 사용가능한 포트 조회
     */
    public int findAvailablePort() throws IOException {
        for(int port = 10000; port <= 65535; port++) {
            Process process = executeGrepProcessCommand(port);
            if(!isRunning(process)) {
                return port;
            }
        }

        throw new IllegalArgumentException("Not Found Available port: 10000 ~ 65535");
    }

    /**
     * 해당 port를 사용중인 프로세스 확인하는 sh 실행
     */
    private Process executeGrepProcessCommand(int port) throws IOException {
        String command = String.format("netstat -nat | grep LISTEN | grep %d", port);
        String[] shell = {"/bin/sh", "-c", command};
        return Runtime.getRuntime().exec(shell);
    }

    /**
     * 해당 Process가 현재 실행중인지 확인
     */
    private boolean isRunning(Process process) {
        String line;
        StringBuilder pidInfo = new StringBuilder();

        try(BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while((line = input.readLine()) != null) {
                pidInfo.append(line);
            }
        } catch (Exception e) {}

        String result = pidInfo.toString();
        return !result.isEmpty();
    }
}
