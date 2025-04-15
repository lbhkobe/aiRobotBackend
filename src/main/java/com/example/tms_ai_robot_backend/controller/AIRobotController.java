package com.example.tms_ai_robot_backend.controller;

import com.example.tms_ai_robot_backend.model.request.AIRobotRequest;
import com.example.tms_ai_robot_backend.service.AIRobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/api/robot")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AIRobotController {

    @Autowired
    private AIRobotService aiRobotService;

    /**
     * 智能体对话接口，使用SSE实现流式响应
     * @param request 请求参数
     * @return SSE发射器
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AIRobotRequest request) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        
        try {
            // 设置超时或断开连接的回调
            emitter.onTimeout(() -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("连接超时"));
                } catch (IOException e) {
                    // 忽略发送超时消息时的异常
                }
                emitter.complete();
            });
            
            emitter.onCompletion(() -> {
                // 可以在这里处理连接完成后的清理工作
            });
            
            // 启动异步处理
            aiRobotService.processStreamingRequest(request, emitter);
            
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(e.getMessage()));
            } catch (IOException ex) {
                // 忽略发送错误消息时的异常
            }
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
} 