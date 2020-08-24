package com.bpbonline.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping
    public String home(ModelMap modelMap){
        modelMap.put("message", "Simple message");
        return "home";
    }
}
