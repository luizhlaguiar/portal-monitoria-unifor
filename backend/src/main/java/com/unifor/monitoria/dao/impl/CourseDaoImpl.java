package com.unifor.monitoria.dao.impl;

import com.unifor.monitoria.dao.CourseDao;
import com.unifor.monitoria.dao.Dao;
import com.unifor.monitoria.model.Course;

import java.util.Optional;

@Dao
public class CourseDaoImpl extends JpaGenericDao<Course, Integer> implements CourseDao {

    @Override
    public Optional<Course> findByCode(String code) {
        return queryFirst("select c from Course c where c.code = :code", "code", code);
    }
}