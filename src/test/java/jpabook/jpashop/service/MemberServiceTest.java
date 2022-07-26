package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

//Junit을 실행할 때 Spring과 integration
@RunWith(SpringRunner.class)
@SpringBootTest //Springboot를 띄운 상태에서 test하려면 필요. 없으면 autowired 실패
@Transactional //data rollback: test에서 DB에 데이터가 안남게 함
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em; // em.flush(); 로 확인할 수 있음

    @Test
    //persist 한다고 해서 DB에 INSERT 문이 나가진 않음
    //DB transaction이 commit 될 때 나감
    //하지만 spring에서의 transactional은 transaction commit을 하지 않고 Rollback을 함
//    @Rollback(false) //로 두면 rollback을 하지 않고 commit을 하기에 확인할 수 있음
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long savedId = memberService.join(member);

        //then
        //같은 transaction 안에서 같은 entity, id(pk) 값이 똑같으면 같은 영속성 context에 하나로만 관리가 됨
//        em.flush(); //DB에 쿼리가 강제로 나가서 반영시킴
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test(expected = IllegalStateException.class)
    //(expected = IllegalStateException.class)를 통해
    //try ~ catch 구문을 사용하지 않아도 예외 처리 됨
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim1");

        Member member2 = new Member();
        member2.setName("kim1");
        //when
        memberService.join(member1);
        memberService.join(member2); // 같은 이름이면 에러가 발생해야 함

        //then
        fail("예외가 발생해야 한다.");
    }
}