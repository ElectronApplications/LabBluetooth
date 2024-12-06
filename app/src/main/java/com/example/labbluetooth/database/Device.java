package com.example.labbluetooth.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.DaoException;

@Entity
public class Device {
    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private String name;
    private Integer messagesAmount;
    @NotNull
    @ToMany(referencedJoinProperty = "deviceId")
    @OrderBy("name ASC")
    private List<DeviceTag> deviceTags;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 371273952)
    private transient DeviceDao myDao;
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
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 267601231)
    public List<DeviceTag> getDeviceTags() {
        if (deviceTags == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            DeviceTagDao targetDao = daoSession.getDeviceTagDao();
            List<DeviceTag> deviceTagsNew = targetDao._queryDevice_DeviceTags(id);
            synchronized (this) {
                if (deviceTags == null) {
                    deviceTags = deviceTagsNew;
                }
            }
        }
        return deviceTags;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 227786774)
    public synchronized void resetDeviceTags() {
        deviceTags = null;
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1755220927)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDeviceDao() : null;
    }
}
