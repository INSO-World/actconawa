package at.ac.tuwien.inso.actconawa.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CrossOrigin("*")
public class UiController {

    @GetMapping({"/", "/#/?"})
    public String forwardToUi() {
        return "forward:/index.html";
    }
}
