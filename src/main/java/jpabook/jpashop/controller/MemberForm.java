package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberForm {

    //화면을 위한 로직은 엔티티에는 없어야 함. 지저분해져서 유지보수하기 어려워짐
    //따라서 별도의 Form 객체나 DTO를 만들어서 관리 ex) NotEmpty annotation
    @NotEmpty(message = "회원 이름은 필수입니다.")
    private String name;

    private String city;
    private String street;
    private String zipcode;
}
