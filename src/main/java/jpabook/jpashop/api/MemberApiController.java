package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

//@Controller + @ResponseBody = @RestController
//@ResponseBody: Json, xml로 data를 보낼 때 사용
@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    //@Valid: javax.validation
    //@RequestBody: Json으로 온 body를 Member에 그대로 mapping(json data -> member)
    //현 상황에서는 아무것도 데이터를 입력하지 않아도 null이 채워지면서 증가한 id가 반환됨
    // -> entity에 @NotEmpty 등의 validation으로 컨트롤: springboot 메뉴얼 볼 것
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //entity 값을 임의로 변환시키면 컴파일 시점에서 오류 출력
    //entity는 어떤게 넘어올지 모름
    //DTO를 사용하면 어떤 값이 사용중인지 정리할 수 있음, entity를 변경해도 api 스펙이 바뀌지는 않음
    //valication 추가 가능
    //entity와 present 계층을 위한 로직을 분리할 수 있음
    @PostMapping("api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }


    @Data
    static class CreateMemberResponse {
        public CreateMemberResponse(Long id) {
            this.id = id;
        }

        private Long id;
    }

}
