package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.Presenca;

import java.util.List;

public interface PresencaDao extends GenericDao<Presenca, Integer> {

    List<Presenca> findByScheduleId(Integer scheduleId);

    boolean existsByScheduleIdAndUserId(Integer scheduleId, Integer userId);
}