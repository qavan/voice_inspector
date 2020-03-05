package com.sac.speechdemo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;


@Entity(active = true, nameInDb = "TASKS_TEST")
public class Task {
    @Id
    private Long TaskId;
    @NotNull
    private String postgreId;
    @NotNull
    private String c_client;
    @NotNull
    private String c_address;
    private String c_client_id;
    private String d_prev_date;
    private String n_prev_value;
    @NotNull
    private String d_current_date;
    @NotNull
    private String n_current_value;
    @NotNull
    private Boolean b_done;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1469429066)
    private transient TaskDao myDao;

    public Task(String id, String Client, String Address, String ClientId, String PrevDate, String PrevValue, String CurrentDate, String CurrentValue, Boolean Done) {
        this.postgreId = id;
        this.c_client = Client;
        this.c_address = Address;
        this.c_client_id = ClientId;
        this.d_prev_date = PrevDate;
        this.d_current_date = CurrentDate;
        this.n_prev_value = PrevValue;
        this.n_current_value = CurrentValue;
        this.b_done = Done;
    }

    @Generated(hash = 603453828)
    public Task(Long TaskId, @NotNull String postgreId, @NotNull String c_client,
                @NotNull String c_address, String c_client_id, String d_prev_date,
                String n_prev_value, @NotNull String d_current_date,
                @NotNull String n_current_value, @NotNull Boolean b_done) {
        this.TaskId = TaskId;
        this.postgreId = postgreId;
        this.c_client = c_client;
        this.c_address = c_address;
        this.c_client_id = c_client_id;
        this.d_prev_date = d_prev_date;
        this.n_prev_value = n_prev_value;
        this.d_current_date = d_current_date;
        this.n_current_value = n_current_value;
        this.b_done = b_done;
    }

    @Generated(hash = 733837707)
    public Task() {
    }

    public Long getTaskId() {
        return this.TaskId;
    }

    public void setTaskId(Long TaskId) {
        this.TaskId = TaskId;
    }

    public String getPostgreId() {
        return this.postgreId;
    }

    public void setPostgreId(String postgreId) {
        this.postgreId = postgreId;
    }

    public String getC_client() {
        return this.c_client;
    }

    public void setC_client(String c_client) {
        this.c_client = c_client;
    }

    public String getC_address() {
        return this.c_address;
    }

    public void setC_address(String c_address) {
        this.c_address = c_address;
    }

    public String getC_client_id() {
        return this.c_client_id;
    }

    public void setC_client_id(String c_client_id) {
        this.c_client_id = c_client_id;
    }

    public String getD_prev_date() {
        return this.d_prev_date;
    }

    public void setD_prev_date(String d_prev_date) {
        this.d_prev_date = d_prev_date;
    }

    public String getN_prev_value() {
        return this.n_prev_value;
    }

    public void setN_prev_value(String n_prev_value) {
        this.n_prev_value = n_prev_value;
    }

    public String getD_current_date() {
        return this.d_current_date;
    }

    public void setD_current_date(String d_current_date) {
        this.d_current_date = d_current_date;
    }

    public String getN_current_value() {
        return this.n_current_value;
    }

    public void setN_current_value(String n_current_value) {
        this.n_current_value = n_current_value;
    }

    public Boolean getB_done() {
        return this.b_done;
    }

    public void setB_done(Boolean b_done) {
        this.b_done = b_done;
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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1442741304)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTaskDao() : null;
    }
}
