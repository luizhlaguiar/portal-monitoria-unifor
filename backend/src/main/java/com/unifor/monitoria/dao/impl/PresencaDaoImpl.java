package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.dao.PresencaDao;
import com.unifor.monitoria.model.Presenca;

import java.util.List;

@Dao
public class PresencaDaoImpl extends JpaGenericDao<Presenca, Integer> implements PresencaDao {

    @Override
    public List<Presenca> findByScheduleId(Integer scheduleId) {
        return queryList("select p from Presenca p where p.schedule.id = :sid", "sid", scheduleId);
    }

    @Override
    public boolean existsByScheduleIdAndUserId(Integer scheduleId, Integer userId) {
        return queryExists("select count(p) from Presenca p where p.schedule.id = :sid and p.user.id = :uid",
                "sid", scheduleId, "uid", userId);
    }
}