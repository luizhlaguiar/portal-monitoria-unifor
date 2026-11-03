package com.unifor.monitoria.dao;

import com.unifor.monitoria.model.Course;

import java.util.Optional;

public interface CourseDao extends GenericDao<Course, Integer> {

    Optional<Course> findByCode(String code);
}