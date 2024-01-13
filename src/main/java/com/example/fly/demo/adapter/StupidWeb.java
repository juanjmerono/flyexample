package com.example.fly.demo.adapter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;



@Controller
public class StupidWeb {
    
    private JPAStupidRepository jpaStupidRepository;

    public StupidWeb(JPAStupidRepository jpaStupidRepository) {
        this.jpaStupidRepository = jpaStupidRepository;
    }


    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("stupids", jpaStupidRepository.findAll());
        return "index";
    }

    @GetMapping("/signup")
    public String singUp(Stupid entity) {
        return "add-stupid";
    }
    
    @PostMapping("/addstupid")
    public String addStupid(@Valid Stupid entity, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-stupid";
        }
        jpaStupidRepository.save(entity);
        return "redirect:/index";
    }
    


}
