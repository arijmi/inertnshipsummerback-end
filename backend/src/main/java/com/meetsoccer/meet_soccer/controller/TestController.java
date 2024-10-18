package com.meetsoccer.meet_soccer.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @RequestMapping("/hello")
    @PreAuthorize("hasRole('PLAYER')")
    public String hello() {
        return "Hello, World!";
    }
}
