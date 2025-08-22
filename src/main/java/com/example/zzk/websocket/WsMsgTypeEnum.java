package com.example.zzk.websocket;

/**
 * WebSocket消息类型枚举
 */
public enum WsMsgTypeEnum {

    //演练实施相关
    DRILL_START("DRILL_START", "演练发布"),

    // 系统通知
    SYSTEM_NOTIFICATION("SYSTEM_NOTIFICATION", "系统通知"),
    
    // 任务相关
    TASK_ASSIGNED("TASK_ASSIGNED", "任务分配"),
    TASK_UPDATED("TASK_UPDATED", "任务更新"),
    TASK_COMPLETED("TASK_COMPLETED", "任务完成"),
    
    // 事件相关  
    NEW_EVENT("NEW_EVENT", "新事件创建"),
    UPDATE_EVENT("UPDATE_EVENT", "事件更新"),
    EVENT_STATUS_CHANGED("EVENT_STATUS_CHANGED", "事件状态变更"),
    
    // 预案相关
    PLAN_ACTIVATED("PLAN_ACTIVATED", "预案激活"),
    PLAN_UPDATED("PLAN_UPDATED", "预案更新"),
    
    // 用户相关
    USER_ONLINE("USER_ONLINE", "用户上线"),
    USER_OFFLINE("USER_OFFLINE", "用户下线"),
    
    // 心跳
    HEARTBEAT("HeartBeat", "心跳检测");

    private final String code;
    private final String description;

    WsMsgTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.code;
    }
}

