package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    //화면, present 계층을 위한 검증 로직이 entity에 들어가 있음
    //-> 어떤 api에서는 해당 검증이 필요하지만 다른 곳에서는 필요 없을 수 있음
    //entity를 수정해서 entity의 스펙 자체가 바뀌는 것이 문제
    //entity는 상당히 여러 곳에서 사용함. 바뀔 확률이 높음.
    //따라서 entity 변환시에 스펙이 바뀌면 안됨
    //entity의 스펙을 위한 별도의 DTO를 만들어야
    //api를 만들 때는 항상 entity를 parameter로 받지 말 것, 외부에 노출하지 말 것
    @NotEmpty
    private String name;

    @Embedded
    private Address address;

    //읽기 전용
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
