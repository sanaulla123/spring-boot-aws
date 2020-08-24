package com.bpbonline.mapper;

import com.bpbonline.model.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TaskMapper {
    void addNewTask(Task task);
    void editTask(Map<String, Object> taskParams);
    boolean canViewTask(@Param("username")String username,
                        @Param("id") Long id);
    Task getTaskDetail(@Param("id") Long id);
    List<Task> getTasks(@Param("username") String username);
    void deleteTask(@Param("id") Long taskId);
}
