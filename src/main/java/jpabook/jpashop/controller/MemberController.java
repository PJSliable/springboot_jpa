package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        //validation이 가능하므로 빈 껍데기라도 넘김
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    //Valid annotation으로 유효성 검사를 할 수 있음 ex) NotEmpty
    public String create(@Valid MemberForm form, BindingResult result) {

        //error시에 다시 form으로 보내고 message 출력
        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        //"redirect:/" : 첫 번째 페이지로 넘어감
        return "redirect:/";
    }

    //엄밀하게 하려면 Member Entity를 그대로 뿌리기 보다는
    //DTO로 변환을 하여 화면에 꼭 필요한 정보들을 출력하는 것을 권장
    //해당 사례에서는 전부 뿌리므로 그대로 진행하였음
    //API를 만들 때에는 절대 Entity를 외부로 반환하면 안됨
    //1. API의 로직을 변환하는 것으로 API의 스펙이 변함: API가 불안정해짐
    //2. password 같은 정보가 그대로 노출될 수 있음
    @GetMapping("/members")
    public String list(Model model) {
        model.addAttribute("members", memberService.findMember());
        return "members/memberList";
    }
}
