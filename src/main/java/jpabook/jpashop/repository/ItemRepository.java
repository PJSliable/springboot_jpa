package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            //변경감지(dirty checking): Transactional 안에서 변경분에 대해서 commit되면
            //JPA가 찾아서 update 쿼리를 자동으로 생성하여 DB에 반영
            //병합: 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능
            em.merge(item); //update와 비슷
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
