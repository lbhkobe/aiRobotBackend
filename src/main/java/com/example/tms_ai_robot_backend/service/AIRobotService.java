package com.example.tms_ai_robot_backend.service;

import com.example.tms_ai_robot_backend.model.request.AIRobotRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 智能体服务接口
 */
public interface AIRobotService {
    
    /**
     * 处理流式请求
     * @param request 请求参数
     * @param emitter SSE发射器
     * @throws Exception 处理异常
     */
    void processStreamingRequest(AIRobotRequest request, SseEmitter emitter) throws Exception;
} 