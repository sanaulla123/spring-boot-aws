package com.bpbonline.mapper;

import com.bpbonline.model.Task;
import com.bpbonline.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@MybatisTest
public class TaskMapperTest {
    @Autowired TaskMapper taskMapper;


    @Test
    public void test_addNewTask(){
        String username = "sanaulla";
        Task t = new Task("title 1", "description 1", username);
        taskMapper.addNewTask(t);
        assertThat(t.getId()).isEqualTo(4L);
    }

    @Test
    public void test_editTask(){
        String username = "sanaulla";
        Task t = new Task("title 1", "description 1", username);
        taskMapper.addNewTask(t);
        Long taskId = t.getId();
        t.setTitle("Title updated");
        t.setUpdatedBy(username);
        t.setStatus(TaskStatus.Done);
        taskMapper.editTask(Map.of("title", "Title updated",
                "updatedBy", username, "id", taskId,
                "status", TaskStatus.Done));

        Task taskDetailFromDb = taskMapper.getTaskDetail(taskId);
        assertThat(taskDetailFromDb.getTitle()).isEqualTo(t.getTitle());
        assertThat(taskDetailFromDb.getUpdatedOn()).isNotNull();
        assertThat(taskDetailFromDb.getStatus()).isEqualTo(t.getStatus());
    }

    @Test
    public void test_canViewTask(){
        String username = "sanaulla";
        Task t = new Task("title 1", "description 1", username);
        taskMapper.addNewTask(t);
        Long taskId = t.getId();
        boolean canViewTask = taskMapper.canViewTask(username, taskId);
        assertThat(canViewTask).isTrue();
        canViewTask = taskMapper.canViewTask("mohamed", taskId);
        assertThat(canViewTask).isFalse();
    }

    /*
    List<Task> getTasks(@Param("username") String username);*/

    @Test
    public void test_getTasks(){
        String username = "sanaulla";
        Task t = new Task("title 1", "description 1", username);
        taskMapper.addNewTask(t);
        t = new Task("title 2", "description 2", username);
        taskMapper.addNewTask(t);

        username = "mohamed";
        t = new Task("title 1", "description 1", username);
        taskMapper.addNewTask(t);
        t = new Task("title 2", "description 2", username);
        taskMapper.addNewTask(t);

        List<Task> tasks = taskMapper.getTasks("sanaulla");
        //two from above and two from data.sql
        assertThat(tasks).hasSize(4);
    }

}
