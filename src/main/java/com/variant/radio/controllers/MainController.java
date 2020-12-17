package com.variant.radio.controllers;

import com.variant.radio.domain.User;
import com.variant.radio.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
public class MainController {

    private final MessageRepository messageRepository;

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    public MainController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping
    public String main(Model model, @AuthenticationPrincipal User user) {

        Map<Object, Object> data = new HashMap<>();

        data.put("profile", user);
        data.put("messages", messageRepository.findAll());


        model.addAttribute("isDevMode", "dev".equals(profile));

        model.addAttribute("frontendData", data);

        return "index";
    }
}
