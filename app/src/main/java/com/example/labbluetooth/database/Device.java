package com.example.labbluetooth.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class Device {
    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private String name;
    private Integer messagesAmount;
    @Generated(hash = 631853870)
    public Device(Long id, @NotNull String name, Integer messagesAmount) {
        this.id = id;
        this.name = name;
        this.messagesAmount = messagesAmount;
    }
    @Generated(hash = 1469582394)
    public Device() {
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
    public Integer getMessagesAmount() {
        return this.messagesAmount;
    }
    public void setMessagesAmount(Integer messagesAmount) {
        this.messagesAmount = messagesAmount;
    }
}
