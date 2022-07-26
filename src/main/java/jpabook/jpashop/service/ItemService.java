package jpabook.jpashop.service;

import jpabook.jpashop.domain.Item.Book;
import jpabook.jpashop.domain.Item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    //이것 전체를 merge라고 볼 수 있음(+반환). JPA가 한 줄로 해줌
    //넘어온 item은 영속성 컨텍스트로 바뀌지 않음.
    // 반환된 값이 새롭게 영속성 컨텍스트로 관리되는 객체가 됨
    //변경감지를 사용하면 원하는 속성만 선택해서 변경할 수 있지만,
    //주의: 병합을 사용하면 모든 속성이 변경된다 => 값이 없으면 null로 업데이트 함
    //-> 변경 감지를 사용해야 함.
    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        //Transaction 안에서 entity를 조회해야 영속성 상태로 조회가 되고
        // 해당 상태에서 값을 변경해야 변경 감지가 일어남
        Item findItem = itemRepository.findOne(itemId);
        //사실 이런 식으로 단발성 update를 하면 안됨: set이 아닌 의미있는 method를 사용할 것
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stockQuantity);
        // findItem로 찾은 것은 영속 상태이므로 호출할 필요 없음. 변경 감지로 인해서 변경됨
        //@Transactional로 인해서 Transaction이 commit됨 -> flush: 변경된 것을 탐지 -> update query를 날림
//        itemRepository.save(findItem);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
