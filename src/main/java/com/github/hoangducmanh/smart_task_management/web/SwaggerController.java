package com.github.hoangducmanh.smart_task_management.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for handling Swagger documentation requests.
 * This controller provides an endpoint to redirect users to the Swagger UI documentation page.
 * Note: The Swagger UI should be disabled in production environments for security reasons.
 */

@RestController
@RequestMapping("/v1/api")
public class SwaggerController {
    @GetMapping("/doc")
    public RedirectView doc() {
        return new RedirectView("/swagger-ui/index.html");
    }
}
