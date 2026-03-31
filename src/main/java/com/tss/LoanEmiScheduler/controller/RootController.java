package com.tss.LoanEmiScheduler.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RootController {
    @GetMapping
    public String home(){
        return "Welcome to Loan EMI system. Sign up/Login to continue the service";
    }
}
