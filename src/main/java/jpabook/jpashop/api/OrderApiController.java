package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;


/**
 * 컬렉션 조회: 일대 다 관계(OneToMany)
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;

    @GetMapping("api/v1/orders")
    public List<Order> ordersV1() {
        //LAZY 로딩에 대해서 컨트롤 해줘야 함. 여기서는 넘어감
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName(); // 강제 초기화
            order.getDelivery().getAddress(); // 마찬가지
            List<OrderItem> orderItems = order.getOrderItems();
//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName();
//            }
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }


    @GetMapping("api/v2/orders")
    public List<OrderDto> ordersV2() {
        //LAZY 로딩에 대해서 컨트롤 해줘야 함. 여기서는 넘어감

        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(OrderDto::new)
                .collect(toList());
    }
    //그냥 join 하므로 데이터가 뻥튀기 됨
    //fetch join으로 sql이 한번만 실행 됨

    //단점1: 페이징이 불가능
    //-> 각각의 row을 기준으로 순서를 매겨 처리하기 때문에
    // 원하는 Order를 기준으로 limit, offset하지 않고 더 숫자가 적은 OrderItem을 기준으로 페이징
    //-> 메모리에 전부 올려서 페이징 처리 : 메모리 터질 수 있음( 매우 위험 )
    // ** 컬렉션(OneToMany) fetch join에서는 페이징을 하면 안됨 **

    // 단점2: 컬렉션 패치 조인은 1개만 사용할 수 있음
    // -> 1:N:M 이면 N*M로 데이터가 뻥튀기 됨
    // -> 무엇을 기준으로 데이터를 가져와야하는지 모르게 될 수 있음: 데이터 정확성이 떨어짐(개수가 안맞거나 하는 등)
    // 보통 경고를 냄
    public List<OrderDto> ordersV3() {
        return orderRepository.findAllWithItem().stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    @Getter
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; // Dto 안에도 entity 가 있으면 안됨 OrderItem
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

            orderItems = order.getOrderItems().stream()
                    .map(orderItem->new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName;//상품 명
        private int orderPrice;//주문가격
        private int count; // 주문수량
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }



}
