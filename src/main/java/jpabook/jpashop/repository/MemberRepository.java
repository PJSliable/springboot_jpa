package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository  {
    //원래 EntityManager는 PersistenceContext가 있어야 인젝션이 가능
//    @PersistenceContext //springboot의 spring data jpa를 사용하면 autowired로 바꿀 수 있음
    // => final을 추가하고 annotation를 RequiredArgsConstructor로 교체 가능
    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }
    //JPQL: from의 대상이 table이 아니라 entity
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        //parameter name binding .setParameter("name1", name) -> :name1
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
