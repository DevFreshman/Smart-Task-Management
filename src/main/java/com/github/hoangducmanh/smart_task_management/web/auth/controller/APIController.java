package com.github.hoangducmanh.smart_task_management.web.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/v1/api")
public class APIController {
    @GetMapping("/doc")
    public RedirectView doc() {
        return new RedirectView("/swagger-ui/index.html");
    }
}
