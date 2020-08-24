package com.bpbonline.controller.api;

import com.bpbonline.model.AuthenticationUser;
import com.bpbonline.model.Task;
import com.bpbonline.model.TaskStatus;
import com.bpbonline.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskAPIController {
    @Autowired TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskDetail(@PathVariable Long taskId){
        String loggedInUsername = getLoggedInUsername();
        try{
            Task taskDetail = taskService.getTaskDetail(taskId, loggedInUsername);
            return ResponseEntity.ok(taskDetail);
        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getTasks(){
        String loggedInUsername = getLoggedInUsername();
        List<Task> tasks = taskService.getTasks(loggedInUsername);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public Task addNewTask(Task taskInput){
        String loggedInUsername = getLoggedInUsername();
        taskInput.setCreatedBy(loggedInUsername);
        taskService.addNewTask(taskInput);
        return taskInput;
    }

    @PostMapping("/{taskId}")
    public ResponseEntity<?> editTask(@PathVariable Long taskId,
                                      @RequestParam(name = "title") String title,
                                      @RequestParam(name="status") TaskStatus status,
                                      @RequestParam(name="description", required = false) String description){
        String loggedInUsername = getLoggedInUsername();
        try {
            Task updatedTask = taskService.editTask(taskId, title, description, status, loggedInUsername);
            return ResponseEntity.ok(updatedTask);
        }catch(HttpClientErrorException ex){
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId){
        String loggedInUsername = getLoggedInUsername();
        try{
            taskService.deleteTask(taskId, loggedInUsername);
            return ResponseEntity.ok().build();
        }catch(HttpClientErrorException ex){
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
        }
    }

    private String getLoggedInUsername(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((AuthenticationUser)authentication.getPrincipal()).getUsername();
    }
}
