package com.unifor.monitoria.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "disciplinas")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String department;
    private String description;

    public Course() {}

    public Course(Integer id, String code, String name, String department, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.department = department;
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
