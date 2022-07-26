package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@Controller + @ResponseBody = @RestController
//@ResponseBody: Json, xml로 data를 보낼 때 사용
@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;


    //엔티티를 직접 노출하게 되면 엔티티에 대한 모든 정보가 노출됨
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    //entity 를 DTO로 변환
    //but API 스펙이 변하지 않음, 유연성 증가
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    //Result로 감싸서 추가적인 데이터(ex count)들을 반환할 수 있게 함
    //리스트[{data1}, {data2}] 자체를 반환하는 것이 아닌 객체{}를 반환하는 것
//    {
//        "count" : count,
//        "data":
//        [
//         {data1}, {data2}
//        ]
//    }
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    //@Valid: javax.validation
    //@RequestBody: Json으로 온 body를 Member에 그대로 mapping(json data -> member)
    //현 상황에서는 아무것도 데이터를 입력하지 않아도 null이 채워지면서 증가한 id가 반환됨
    // -> entity에 @NotEmpty 등의 validation으로 컨트롤: springboot 메뉴얼 볼 것
    //실무에서 지양할 것
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //entity 값을 임의로 변환시키면 컴파일 시점에서 오류 출력
    //entity는 어떤게 넘어올지 모름
    //DTO를 사용하면 어떤 값이 사용중인지 정리할 수 있음, entity를 변경해도 api 스펙이 바뀌지는 않음
    //valication 추가 가능
    //entity와 present 계층, validation 등을 위한 로직을 분리할 수 있음
    //요청값으로 entity 대신에 별도의 DTO를 받는 방식
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request
    ) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }



    //DTO
    @Data
    static class CreateMemberRequest {
        //entity를 건들지 않고 여기서 제약 가능
//        @NotEmpty
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
