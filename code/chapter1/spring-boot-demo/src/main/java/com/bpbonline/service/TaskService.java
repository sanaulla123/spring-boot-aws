package com.bpbonline.service;

import com.bpbonline.mapper.TaskMapper;
import com.bpbonline.model.Task;
import com.bpbonline.model.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.thymeleaf.util.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired TaskMapper taskMapper;

    public Task addNewTask(Task task){
        List<String> errors = new ArrayList<>();
        if (StringUtils.isEmpty(task.getTitle())){
            errors.add("Title is required");
        }
        if (StringUtils.isEmpty(task.getCreatedBy())){
            errors.add("created by username is required");
        }
        if ( !errors.isEmpty()){
            throw new IllegalArgumentException(errors.stream().collect(Collectors.joining(", ")));
        }
        taskMapper.addNewTask(task);
        return task;
    }

    @Transactional
    public Task editTask(Long taskId, String title, String description, TaskStatus status, String loggedInUsername) {

        boolean canViewTask = taskMapper.canViewTask(loggedInUsername, taskId);
        if ( !canViewTask){
            throw HttpClientErrorException.create("Not authorized to edit the task", HttpStatus.FORBIDDEN,
                    "Forbidden", new HttpHeaders(), null, null);
        }
        List<String> errors = new ArrayList<>();
        if ( taskId == null){
            errors.add("Task id is null");
        }
        if (StringUtils.isEmpty(title)){
            errors.add("Title is required");
        }
        if ( status == null ){
            errors.add("Status is required");
        }
        if (StringUtils.isEmpty(loggedInUsername)){
            errors.add("updated by username is required");
        }

        if ( !errors.isEmpty()){
            throw HttpClientErrorException.create(errors.stream().collect(Collectors.joining(", ")),
                    HttpStatus.BAD_REQUEST, "Bad Request", new HttpHeaders(), null, null);
        }
        taskMapper.editTask(Map.of("id", taskId, "title", title,
                "status", status,
                "description", description,
                "updatedBy", loggedInUsername));
        Task taskDetail = taskMapper.getTaskDetail(taskId);
        return taskDetail;
    }

    public Task getTaskDetail(Long taskId, String loggedInUsername) {
        boolean canViewTask = taskMapper.canViewTask(loggedInUsername, taskId);
        if ( !canViewTask){
            throw HttpClientErrorException.create("Not authorized to view task detail", HttpStatus.FORBIDDEN,
                    "Forbidden", new HttpHeaders(), null, null);
        }
        return taskMapper.getTaskDetail(taskId);
    }

    public List<Task> getTasks(String loggedInUsername) {
        return taskMapper.getTasks(loggedInUsername);
    }

    public void deleteTask(Long taskId, String loggedInUsername) {
        boolean canViewTask = taskMapper.canViewTask(loggedInUsername, taskId);
        if ( !canViewTask){
            throw HttpClientErrorException.create("Not authorized to delete task", HttpStatus.FORBIDDEN,
                    "Forbidden", new HttpHeaders(), null, null);
        }
        taskMapper.deleteTask(taskId);
    }
}
