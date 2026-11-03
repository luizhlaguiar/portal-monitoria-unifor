package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.ActivityDao;
import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.model.Activity;

import java.util.List;

@Dao
public class ActivityDaoImpl extends JpaGenericDao<Activity, Integer> implements ActivityDao {

    @Override
    public List<Activity> findByDisciplineId(Integer disciplineId) {
        return queryList("select a from Activity a where a.discipline.id = :did", "did", disciplineId);
    }
}