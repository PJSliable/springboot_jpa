package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    @GetMapping("hello")
    public String hello(Model model) {
//model에 data를 넣어 view로 넘길 수 있음
        model.addAttribute("data", "hello!!!");
//화면 이름, .html이 자동으로 붙음. resources/templates/+{ViewName}+.html
        return "hello";
    }


}
