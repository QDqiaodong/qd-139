package com.polar.experience.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "change_log_child")
public class ChildChangeLog extends ChangeLog {
}