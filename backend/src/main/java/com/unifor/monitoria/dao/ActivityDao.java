package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.Activity;

import java.util.List;

public interface ActivityDao extends GenericDao<Activity, Integer> {

    List<Activity> findByDisciplineId(Integer disciplineId);
}