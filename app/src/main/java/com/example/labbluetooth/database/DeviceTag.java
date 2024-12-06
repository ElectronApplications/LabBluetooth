package com.example.labbluetooth.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DeviceTag {
    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private Long deviceId;
    @Generated(hash = 347581502)
    public DeviceTag(Long id, @NotNull String name, @NotNull Long deviceId) {
        this.id = id;
        this.name = name;
        this.deviceId = deviceId;
    }
    @Generated(hash = 1606429719)
    public DeviceTag() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getDeviceId() {
        return this.deviceId;
    }
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }
}
