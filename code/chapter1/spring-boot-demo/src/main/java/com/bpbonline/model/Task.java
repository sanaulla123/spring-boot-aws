package com.bpbonline.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Task {
    public Task(String title, String description, String createdBy){
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.status = TaskStatus.Todo;
    }

    public Task(String title, String description, String createdBy, TaskStatus taskStatus){
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.status = taskStatus;
    }

    Long id;
    String title;
    String description;
    TaskStatus status;
    String createdBy;
    String createdByName;
    LocalDateTime createdOn;

    String updatedBy;
    String updatedByName;
    LocalDateTime updatedOn;
}
