/* (C)2025 */
package com.example.urlshortenerapi.features.user;

import com.example.urlshortenerapi.features.user.dto.UserRequestDTO;
import com.example.urlshortenerapi.features.user.dto.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping(value = "/user")
    public UserResponseDTO create(@RequestBody @Valid UserRequestDTO request) {
        return this.userService.create(request);
    }
}
