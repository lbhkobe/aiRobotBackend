package com.example.tms_ai_robot_backend.service.impl;

import com.example.tms_ai_robot_backend.model.request.AIRobotRequest;
import com.example.tms_ai_robot_backend.service.AIRobotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 智能体服务实现类
 */
@Service
@Slf4j
public class AIRobotServiceImpl implements AIRobotService {

    private static final String API_URL = "https://inner-apisix.hisense.com/higpt/api/agent/v1/agents/run?user_key=snhwowmjyxucsqzrou7v9efmrcu29d12&client_key=9ca2830593ca44eba6e7afca995f5f00&app_id=4868c264aa4dde4464d59c0bd2e60fe8";
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void processStreamingRequest(AIRobotRequest request, SseEmitter emitter) throws Exception {
        executorService.execute(() -> {
            try {
                // 设置HTTP请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Accept", "text/event-stream");
                
                // 构建请求实体
                HttpEntity<AIRobotRequest> requestEntity = new HttpEntity<>(request, headers);
                
                // 发送请求并获取响应（使用字节流以便正确处理SSE）
                ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                        API_URL,
                        HttpMethod.POST,
                        requestEntity,
                        byte[].class
                );
                
                if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                    // 处理SSE响应
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    new java.io.ByteArrayInputStream(responseEntity.getBody()),
                                    StandardCharsets.UTF_8))) {
                        
                        String line;
                        StringBuilder eventData = new StringBuilder();
                        String eventName = "agent_message"; // 默认事件名称
                        
                        while ((line = reader.readLine()) != null) {
                            // 空行表示一个事件的结束
                            if (line.isEmpty()) {
                                if (eventData.length() > 0) {
                                    // 发送收集到的事件数据
                                    emitter.send(SseEmitter.event()
                                            .name(eventName)
                                            .data(eventData.toString(), MediaType.APPLICATION_JSON));
                                    
                                    // 重置事件数据
                                    eventData = new StringBuilder();
                                    eventName = "agent_message"; // 重置为默认事件名称
                                }
                                continue;
                            }
                            
                            // 处理事件行
                            if (line.startsWith("event:")) {
                                eventName = line.substring(6).trim();
                            } else if (line.startsWith("data:")) {
                                eventData.append(line.substring(5).trim());
                            } else if (line.startsWith(":")) {
                                // 注释行，忽略
                                continue;
                            } else {
                                // 其他未识别的行，作为数据追加
                                eventData.append(line);
                            }
                        }
                        
                        // 处理最后一个事件（如果有）
                        if (eventData.length() > 0) {
                            emitter.send(SseEmitter.event()
                                    .name(eventName)
                                    .data(eventData.toString(), MediaType.APPLICATION_JSON));
                        }
                    }
                } else {
                    // 处理错误响应
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("API请求失败: " + responseEntity.getStatusCode()));
                }
                
                // 完成发送
                emitter.complete();
                
            } catch (Exception e) {
                log.error("处理流式请求时发生错误", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("服务器处理失败: " + e.getMessage()));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });
    }
} 