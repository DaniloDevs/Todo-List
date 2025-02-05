package com.todo.user;


import at.favre.lib.crypto.bcrypt.BCrypt;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository repository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody @NotNull User user) {
        var existUser = this.repository.findByUsername(user.getUsername());

        if (existUser != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Já existe um usuario cadastrado com esse username");
        }

        user.setPassword(BCrypt.withDefaults().hashToString(10,user.getPassword().toCharArray()));

        var createdUser = this.repository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

    }
}
