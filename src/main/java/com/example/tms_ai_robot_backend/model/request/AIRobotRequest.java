package com.example.tms_ai_robot_backend.model.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 智能体请求模型
 */
@Data
public class AIRobotRequest {
    /**
     * 客户端的user标识，当前统一使用LDAP的登录名
     */
    private String user_id;
    
    /**
     * 智能体的会话ID，第一次发起会话时没有，由服务器生成并返回
     */
    private String conversation_id;
    
    /**
     * 智能体运行需要传入的输入，对应编排页面上的变量定义
     */
    private Map<String, Object> inputs;
    
    /**
     * 用户的query
     */
    private String query;
    
    /**
     * 返回响应模式，目前仅支持streaming流式模式
     */
    private String response_mode = "streaming";
    
    /**
     * 文件列表
     */
    private List<FileInfo> files;
    
    /**
     * 文件信息
     */
    @Data
    public static class FileInfo {
        /**
         * 文件类型，目前仅支持image
         */
        private String type;
        
        /**
         * 传输方式
         */
        private String transfer_method;
        
        /**
         * 图片链接地址
         */
        private String url;
        
        /**
         * 上传文件ID
         */
        private String upload_file_id = "";
    }
} 