package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

//값 타입은 변경되면 안됨.(immutable)
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    //기본 생성자를 만들어 줘야 함
    // JPA 스펙 상 엔티티나 임베디드 타입은 자바 기본 생성자를 public 또는 protected로 설정해야 함
    // JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가 객체를 생성할 때 리플렉션 같은 기술을 사용할 수 있도록 지원해야 하기 때문
    // 함부로 생성하지 않게 protected로 변경
    protected Address() {
    }

    //생성할 때만 값이 세팅, setter를 제공하지 않음
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
