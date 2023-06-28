package com.assignment.voyage.controller;

import com.assignment.voyage.dto.ApiResultDto;
import com.assignment.voyage.dto.LoginRequestDto;
import com.assignment.voyage.dto.SignupRequestDto;
import com.assignment.voyage.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/signup")
    public ApiResultDto signup(@Valid @RequestBody SignupRequestDto requestDto) {

        try {
            return userService.signup(requestDto);
        } catch (Exception e) {
            return new ApiResultDto(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ApiResultDto login(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        try {
            return userService.login(requestDto, response);
        } catch (Exception e) {
            return new ApiResultDto(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}