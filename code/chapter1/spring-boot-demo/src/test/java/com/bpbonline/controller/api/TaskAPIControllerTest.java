package com.bpbonline.controller.api;

import com.bpbonline.mapper.TaskMapper;
import com.bpbonline.mapper.TaskMapperTest;
import com.bpbonline.model.Task;
import com.bpbonline.model.TaskStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.With;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskAPIControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired TaskMapper taskMapper;
    @Autowired ObjectMapper objectMapper;

    @Test
    @WithUserDetails("sanaulla")
    @Transactional
    public void test_addNewTask() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/tasks")
                        .param("title", "Title 1")
                        .param("description", "Task description")
                        .param("status", TaskStatus.Doing.toString())
        ).andExpect(status().isOk()).andReturn();
        String newTaskJson = mvcResult.getResponse().getContentAsString();
        Task newTask = objectMapper.readValue(newTaskJson, Task.class);
        assertThat(newTask.getTitle()).isEqualTo("Title 1");
        assertThat(newTask.getStatus()).isEqualTo(TaskStatus.Doing);
        assertThat(newTask.getId()).isNotNull();
        assertThat(newTask.getCreatedBy()).isEqualTo("sanaulla");

        //testing validations
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/tasks")
                        .param("description", "Task description")
                        .param("status", TaskStatus.Doing.toString())
        ).andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("sanaulla")
    @Transactional
    public void test_editTask() throws Exception {
        //first create a task
        Task t = new Task("title", "description", "sanaulla");
        taskMapper.addNewTask(t);

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/tasks/"+t.getId())
                        .param("title", "Title Updated")
                        .param("status", TaskStatus.Done.toString())
                        .param("description", "Task description")
        ).andExpect(status().isOk()).andReturn();
        String newTaskJson = mvcResult.getResponse().getContentAsString();
        Task newTask = objectMapper.readValue(newTaskJson, Task.class);
        assertThat(newTask.getTitle()).isEqualTo("Title Updated");
        assertThat(newTask.getUpdatedBy()).isEqualTo("sanaulla");
        assertThat(newTask.getUpdatedOn()).isNotNull();
        assertThat(newTask.getStatus()).isEqualTo(TaskStatus.Done);

        //testing validations
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/tasks/"+t.getId())
                        .param("status", TaskStatus.Done.toString())
                        .param("description", "Task description")
        ).andExpect(status().isBadRequest());

        //testing validations
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/tasks/"+t.getId())
                        .param("title", "Title")
                        .param("description", "Task description")
        ).andExpect(status().isBadRequest());

        //testing privilege
        t = new Task("title", "description", "mohamed");
        taskMapper.addNewTask(t);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/tasks/"+t.getId())
                        .param("title", "Title Update")
                        .param("status", TaskStatus.Done.toString())
        ).andExpect(status().isForbidden());

    }

    @Test
    @Transactional
    @WithUserDetails("sanaulla")
    public void test_getTasks() throws Exception {
        Task t = new Task("title", "description", "sanaulla");
        taskMapper.addNewTask(t);
        t = new Task("title 2", "description 2", "sanaulla");
        taskMapper.addNewTask(t);

        t = new Task("title 2", "description 2", "mohamed");
        taskMapper.addNewTask(t);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/tasks"))
                .andExpect(status().isOk())
                .andReturn();
        List<Task> tasks = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);
        assertThat(tasks).hasSize(4);
    }

    @Test
    @Transactional
    @WithUserDetails("sanaulla")
    public void test_deleteTask() throws Exception{
        Task t = new Task("title", "description", "sanaulla");
        taskMapper.addNewTask(t);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/tasks/" + t.getId()))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(taskMapper.getTaskDetail(t.getId())).isNull();

        //testing privilege
        t = new Task("title", "description", "mohamed");
        taskMapper.addNewTask(t);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/tasks/"+t.getId())
        ).andExpect(status().isForbidden());
    }
}
