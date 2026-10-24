package com.travel.ai_agent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatViewController {

    @GetMapping({"/", "/chat-ui", "/ui"})
    public String chatPage(Model model) {
        model.addAttribute("appName", "Chalo Chalein Trip Finder");
        return "chat";
    }
}
