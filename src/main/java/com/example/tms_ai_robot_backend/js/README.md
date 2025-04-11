# AI机器人调用接口使用说明

本文档介绍如何使用`invokeAIRobot.js`文件中的方法调用智能机器人API，并处理流式响应数据。

## 功能概述

`invokeAIRobot.js`实现了基于SSE(Server-Sent Events)的流式数据处理，可以实时接收和处理来自AI机器人的响应，包括：

- 文本消息
- 思考过程
- 文件（图片等）
- 会话结束通知
- 错误处理

## 使用方法

### 基本调用

```javascript
const controller = invokeAIRobot({
  userId: '用户ID',  // 必填，如LDAP登录名
  query: '用户问题', // 必填
  conversationId: '会话ID', // 可选，继续之前的会话
  inputs: {}, // 可选，输入参数
  files: [], // 可选，文件列表
  
  // 回调函数
  onMessage: function(data) {
    // 处理文本消息
    console.log('回答:', data.answer);
  },
  onThought: function(data) {
    // 处理思考过程
    console.log('思考:', data.thought);
    console.log('观察:', data.observation);
  },
  onFile: function(data) {
    // 处理文件
    console.log('文件URL:', data.url);
  },
  onEnd: function(data) {
    // 处理会话结束
    console.log('会话ID:', data.conversation_id);
  },
  onError: function(error) {
    // 处理错误
    console.error('错误:', error.message);
  }
});
```

### 取消请求

如果需要中断正在进行的对话，可以使用返回的AbortController：

```javascript
// 取消请求
controller.abort();
```

## 参数说明

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| userId | string | 是 | 用户标识，如LDAP登录名 |
| query | string | 是 | 用户问题或指令 |
| conversationId | string | 否 | 会话ID，用于继续已有会话 |
| inputs | object | 否 | 智能体运行需要的输入参数 |
| files | array | 否 | 文件列表，如图片等 |
| onMessage | function | 否 | 文本消息回调 |
| onThought | function | 否 | 思考过程回调 |
| onFile | function | 否 | 文件事件回调 |
| onEnd | function | 否 | 会话结束回调 |
| onError | function | 否 | 错误处理回调 |

## 文件对象格式

当需要发送文件（如图片）时，files数组中的对象格式为：

```javascript
{
  type: "image", // 目前仅支持image
  transfer_method: "remote_url",
  url: "图片URL地址",
  upload_file_id: "" // 留空
}
```

## 回调函数数据格式

### onMessage(data)
```javascript
{
  task_id: "任务ID",
  message_id: "消息ID",
  conversation_id: "会话ID",
  answer: "文本内容",
  created_at: 1725517721,
  is_reasoning: false // 是否为推理过程
}
```

### onThought(data)
```javascript
{
  id: "思考ID",
  task_id: "任务ID",
  message_id: "消息ID",
  position: 1,
  thought: "思考内容",
  observation: "观察结果",
  tool: "使用的工具",
  tool_input: "工具输入",
  created_at: 1725517721,
  message_files: [],
  conversation_id: "会话ID",
  need_show_form: false
}
```

### onFile(data)
```javascript
{
  id: "文件ID",
  type: "文件类型",
  belongs_to: "assistant",
  url: "文件URL",
  conversation_id: "会话ID"
}
```

### onEnd(data)
```javascript
{
  task_id: "任务ID",
  message_id: "消息ID",
  conversation_id: "会话ID",
  need_show_form: false,
  metadata: {},
  usage: {}
}
```

## 注意事项

1. SSE连接默认不会自动关闭，除非服务器明确发送了`message_end`事件或发生错误
2. 使用AbortController可以在用户离开页面或不再需要结果时主动取消请求
3. 回调函数都是可选的，但建议至少实现`onMessage`和`onError` 