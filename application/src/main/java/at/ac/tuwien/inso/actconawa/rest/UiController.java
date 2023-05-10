package at.ac.tuwien.inso.actconawa.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CrossOrigin("*")
public class UiController {

    @GetMapping("/")
    public String home() {
        return "redirect:/ui/";
    }

    @GetMapping("/ui")
    public String redirectUi() {
        return "redirect:/ui/";
    }

    @GetMapping("/ui/")
    public String index() {
        return "/ui/index.html";
    }
}
