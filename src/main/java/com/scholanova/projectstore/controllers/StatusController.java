package com.scholanova.projectstore.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    @GetMapping(path = "/status")
    public String getStatus() {
        return "Ok";
    }
}
