package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order => Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    //entity를 그대로 반환하는 방법. 지양되어야 함
    //Order -> Member -> Order: Infinite recursion
    //양방향 연관관계가 있다면 둘 중 하나는 jsonignore 해줘야 함

    //하지만 지연 로딩의 경우 별도의 문제가 발생 -> hibernate5Module를 사용하여 LAZY는 제외
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        //원하는 부분만 골라서 출력
        for (Order order : all) {
            //order.getMember(), order.getDelivery() 여기까지는 Proxy 객체
            //-> 추가적으로 불러와서 LAZY가 강제 초기화됨
            order.getMember().getName();
            order.getDelivery().getAddress();
        }
        return all;
    }

    //entity가 아닌 DTO로 바꿔서 보내야 함
    //but LAZY 로딩으로 인한 DB 쿼리가 너무 많이 호출되는 문제가 존재
    //ORDER -> SQL 1번 실행 -> 결과 주문수 2개: ORDER, MEMBER, DELIVERY, MEMBER, DELIVERY 총 5 - N+1 문제
    //N+1 문제가 실무에서 문제의 90% 이상을 차지함
    //지연 로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회한 경우 쿼리를 생략
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        //ORDER 2개
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

//        2개 -> loop가 2번
//        order를 SimpleOrderDto로 바꾼 뒤에 List로 변환
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }


    //본질적으로 V2와 같으나 쿼리만 다름
    //-> 쿼리 한번에 끝남. 패치 조인으로 order -> member, order -> delivery 는 이미 조회된 상태이므로 지연 로딩 X
    //**실무에서 자주 사용함**
    //select 할 때 DB에서 많이 가져옴
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    //select절에서 원하는 것만 고름 -> 최적화 but 생각보다 미비
    //new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
    //레포지토리 재사용성이 떨어짐, API 스펙에 맞춘 코드가 레포지토리에 들어가는 단점점    @GetMapping("/api/v4/simple-orders")
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrerDtos();
    }


    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();//LAZY가 초기화 됨. 영속성 컨텍스트가 Member id를 가지고 찾아보고 없으면 DB 쿼리를 날림
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();//LAZY가 초기화 됨
        }
    }



}
