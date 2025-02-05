package com.todo.task;

import com.todo.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    public TaskRepository repository;

    @PostMapping("/")
    public ResponseEntity<Object> create(@RequestBody Task task, HttpServletRequest request) {
        // Get UserId
        var userId = (UUID) request.getAttribute("userId");
        task.setUserId(userId);

        // Validate Date
        var currentDate = LocalDateTime.now();

        if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The start date must be greater than today!");
        }

        if (task.getStartAt().isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The task's start date must be greater than its end date!");
        }


        // Create Task
        var createdTask = this.repository.save(task);

        // Send Response
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping("/")
    public ResponseEntity<Object> listByUserId(HttpServletRequest request) {
        // Get UserId
        var userId = request.getAttribute("userId");
        var tasks = this.repository.findByUserId((UUID) userId);

        // Send Response
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PutMapping("/update/{taskId}")
    public ResponseEntity<Object> updateById(@RequestBody Task taskModel, @PathVariable UUID taskId, HttpServletRequest request) {
        var task = this.repository.findById(taskId).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Utils.getNonNullPropertyNames(taskModel, task);

        var userId = (UUID) request.getAttribute("userId");

        if (!task.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You do not have permission to edit this task");
        }

        Task updatedTask = this.repository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }
}
