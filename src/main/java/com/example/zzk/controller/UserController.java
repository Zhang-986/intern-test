package com.example.zzk.controller;

import com.example.zzk.mapper.JsonMapper;
import com.example.zzk.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * User Management REST Controller
 * 
 * Provides basic CRUD operations for user entities.
 * Demonstrates MyBatis Plus integration.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private JsonMapper jsonMapper;

    /**
     * Create a new user
     * 
     * @param user User object from request body
     * @return The created user
     */
    @PostMapping
    public User createUser(@RequestBody User user) {
        jsonMapper.insert(user);
        jsonMapper.insert(user);
        return user;
    }

    /**
     * Get total count of users in database
     * 
     * @return Number of users
     */
    @GetMapping("/count")
    public long getUserCount() {
        return jsonMapper.selectCount(null);
    }
}
