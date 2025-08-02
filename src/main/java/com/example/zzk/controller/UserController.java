package com.example.zzk.controller;

import com.example.zzk.mapper.JsonMapper;
import com.example.zzk.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private JsonMapper jsonMapper;

    @PostMapping
    public User createUser(@RequestBody User user) {
        jsonMapper.insert(user);
        return user;
    }

    @GetMapping("/count")
    public long getUserCount() {
        return jsonMapper.selectCount(null);
    }
}
