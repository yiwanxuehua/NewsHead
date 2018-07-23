package com.nowcoder.async;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nowcoder on 2016/7/14.
 */
public class EventModel {
    private EventType type;
    private int actorId;//动作
    private int entityId;//触发该动作的对象
    private int entityType;//触发类型
    private int entityOwnerId;//是谁触发的
    private Map<String, String> exts = new HashMap<>();

    public Map<String, String> getExts() {
        return exts;
    }
    public EventModel() {

    }
    public EventModel(EventType type) {
        this.type = type;
    }

    public String getExt(String name) {
        return exts.get(name);
    }

    public EventModel setExt(String name, String value) {
        exts.put(name, value);
        return this;
    }

    public EventType getType() {
        return type;
    }

    public EventModel setType(EventType type) {
        this.type = type;
        return this;
    }

    public int getActorId() {
        return actorId;
    }

    public EventModel setActorId(int actorId) {
        this.actorId = actorId;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public EventModel setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public EventModel setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityOwnerId() {
        return entityOwnerId;
    }

    public EventModel setEntityOwnerId(int entityOwnerId) {
        this.entityOwnerId = entityOwnerId;
        return this;
    }
}
