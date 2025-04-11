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
        SseEmitter emitter = new SseEmitter(0L); // 不超时
        try {
            aiRobotService.processStreamingRequest(request, emitter);
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(e.getMessage()));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
        return emitter;
    }
} 