/**
 * 调用智能机器人API的客户端JavaScript实现
 */

/**
 * 调用AI机器人接口并处理流式响应
 * @param {Object} options - 调用选项
 * @param {string} options.userId - 用户ID
 * @param {string} [options.conversationId] - 会话ID (可选)
 * @param {Object} [options.inputs={}] - 输入参数 
 * @param {string} options.query - 用户查询内容
 * @param {Array} [options.files=[]] - 文件列表
 * @param {Function} options.onMessage - 消息处理回调
 * @param {Function} options.onThought - 思考步骤回调
 * @param {Function} options.onFile - 文件事件回调
 * @param {Function} options.onEnd - 结束事件回调
 * @param {Function} options.onError - 错误处理回调
 * @returns {AbortController} 用于取消请求的控制器
 */
function invokeAIRobot(options) {
  // 参数校验
  if (!options.userId) {
    throw new Error('userId是必须的');
  }
  if (!options.query) {
    throw new Error('query是必须的');
  }
  
  // 创建请求数据
  const requestData = {
    user_id: options.userId,
    query: options.query,
    response_mode: 'streaming',
    conversation_id: options.conversationId || null,
    inputs: options.inputs || {},
    files: options.files || []
  };
  
  // 创建AbortController用于取消请求
  const controller = new AbortController();
  
  // 发起请求
  fetch('/api/robot/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify(requestData),
    signal: controller.signal
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP错误：${response.status}`);
    }
    
    // 获取SSE响应体
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    
    // 处理数据流
    function processStream({ done, value }) {
      if (done) {
        // 处理可能剩余在缓冲区的最后一个事件
        if (buffer.trim()) {
          processEvent(buffer.trim());
        }
        return;
      }
      
      // 解码收到的数据并添加到缓冲区
      buffer += decoder.decode(value, { stream: true });
      
      // 按照SSE协议分割事件（双换行分隔）
      const events = buffer.split('\n\n');
      
      // 处理完整的事件，保留最后一个可能不完整的事件
      buffer = events.pop() || '';
      
      // 处理每个完整的事件
      events.forEach(event => {
        if (event.trim()) {
          processEvent(event.trim());
        }
      });
      
      // 继续读取流
      return reader.read().then(processStream);
    }
    
    // 处理单个SSE事件
    function processEvent(eventText) {
      // 解析事件类型和数据
      let eventType = 'agent_message'; // 默认事件类型
      let eventData = '';
      
      // 按行拆分事件内容
      const lines = eventText.split('\n');
      for (const line of lines) {
        if (line.startsWith('event:')) {
          eventType = line.substring(6).trim();
        } else if (line.startsWith('data:')) {
          eventData = line.substring(5).trim();
        }
      }
      
      // 解析数据为JSON对象
      try {
        const jsonData = JSON.parse(eventData);
        
        // 根据事件类型调用相应的回调
        switch (eventType) {
          case 'agent_message':
            if (options.onMessage) {
              options.onMessage(jsonData);
            }
            break;
          case 'agent_thought':
            if (options.onThought) {
              options.onThought(jsonData);
            }
            break;
          case 'message_file':
            if (options.onFile) {
              options.onFile(jsonData);
            }
            break;
          case 'message_end':
            if (options.onEnd) {
              options.onEnd(jsonData);
            }
            break;
          case 'message_replace':
            // 替换之前的消息
            if (options.onMessage) {
              options.onMessage(jsonData);
            }
            break;
          case 'error':
            if (options.onError) {
              options.onError(jsonData);
            }
            break;
          case 'ping':
            // 忽略ping事件
            break;
          default:
            console.warn('未知事件类型:', eventType, jsonData);
        }
      } catch (err) {
        console.error('解析事件数据失败:', err, eventData);
        if (options.onError) {
          options.onError({ message: '解析事件数据失败', original: eventData });
        }
      }
    }
    
    // 开始处理流
    reader.read().then(processStream);
  })
  .catch(error => {
    if (options.onError) {
      options.onError({ message: error.message });
    } else {
      console.error('调用AI机器人接口出错:', error);
    }
  });
  
  // 返回控制器，允许调用者中止请求
  return controller;
}

/**
 * 使用示例
 */
/* 
// 示例调用
const controller = invokeAIRobot({
  userId: 'xiaoming',
  query: '你好，请介绍一下自己',
  onMessage: (data) => {
    console.log('收到消息:', data.answer);
    // 在界面上追加显示消息
  },
  onThought: (data) => {
    console.log('思考过程:', data.thought);
    console.log('观察结果:', data.observation);
    // 可选择是否在界面上显示思考过程
  },
  onFile: (data) => {
    console.log('文件:', data.url);
    // 在界面上显示图片或提供文件下载链接
  },
  onEnd: (data) => {
    console.log('对话结束:', data);
    // 更新界面状态，例如启用输入框等
  },
  onError: (error) => {
    console.error('错误:', error);
    // 在界面上显示错误消息
  }
});

// 取消请求（如果需要）
// controller.abort();
*/
