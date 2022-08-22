package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;


/**
 * 컬렉션 조회: 일대 다 관계(OneToMany)
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
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


    @GetMapping("/api/v2/orders")
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

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        return orderRepository.findAllWithItem().stream()
                .map(OrderDto::new)
                .collect(toList());
    }

//    한계돌파 - 페이징 + 컬렉션 엔티티 조회 방법
//    - 먼저 ToOne 관계를 모두 패치조인 한다
//    : ToOne 관계는 row 수를 증가시키지 않으므로 페이징 쿼리에 영향을 주지 않는다.
//    - 컬렉션은 지연 로딩으로 조회한다.(패치 조인 X)
//    - 지연 로딩 성능 최적화를 위해 적용할 것
//        1. hibernate.default_batch_fetch_size: 글로벌하게 적용, 적어놓은 개수 만큼 미리 가져옴 - 주로 활용
//        2. @BatchSize: 특정 엔티티에 디테일하게 적용, 1:N 관계에서는 1에 적기
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
    return orderRepository.findAllWithMemberDelivery(offset, limit).stream()
            .map(OrderDto::new)
            .collect(toList());
    }


    //DTO 직접 조회, JPA에서 DTO를 직접 조회
    //Query: 루트 1번, 컬렉션 N번 실행
    //ToOne(N:1, 1:1)관계들을 먼저 조회하고 ToMany(1:N)관걔는 각각 별도로 처리한다.
    //-> ToOne 관계는 조인하면 row 수가 증가하지 않는다.
    //-> ToMany 관계는 조인하면 row 수가 증가한다.
    //row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화하기 쉬우므로 한번에 조회,
    //ToMany 관계는 최적화하기 어려우므로 findOrderItems() 같은 별도의 메서드로 조회
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }


    //query 2번으로 해결
    //컬렉션 조회 최적화: 일대다 관계인 컬렉션은 IN 절을활용해서 메모리에 미리 조회해서 최적화
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    //플랫 데이터 최적화: JOIN 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환
    //query 1번으로 해결
    //단점
    // 1. 우리가 원했던 방식인 order를 기준으로는 페이징 불가능
    // 2. 데이터 중복 -> 각 줄로 나온 것을 개발자가 직접 분해해서 조립하는 방법: 애플리케이션에서 추가 작업이 크다.
    // query는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라 V5보다 느릴 수 있다.
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        //직접 중복을 제거하는 방식
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }


    //권장 순서
    //1. 엔티티 조회 방식으로 우선 접근
    // 1) 패치 조인으로 쿼리 수를 최적화
    // 2) 컬랙션 최적화
    //  - 페이징 필요: hibernate.default_batch_fetch_size, @BatchSize로 최적화
    //  - 페이징 필요X: 패치 조인 사용
    // * 사실 여기서 대부분 기대하는 성능이 나온다.
    // * 이와같은 방식으로도 성능 최적화가 안되는 경우는 서비스가 정말 트래픽이 많은 경우다. 이때는 캐시(redis) 등의 방식으로 문제를 해결하는게 나을 수 있다.
    // * but 엔티티는 직접 캐싱을 하면 안된다. 영속성 컨텍스트에 올라가는 상황과 캐시가 관리하는 상황이 꼬일 수 있다. 무조건 DTO로 변환해서 DTO를 캐싱해야 한다.
    // * 엔티티를 캐시하는 방법은 hibernate 2차 캐시가 있긴 한데 실무에서 적용하기 매우 까다로움. (redis나 로컬의 메모리 캐시)
    // * 정말 이용자가 많은 경우에 한해서 DTO 조회 방식으로의 최적화를 고민할 수 있다.
    //2. 엔티티 조회 방식으로 해결이 안되면 DTO 조회 방식 사용
    //3. DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate

    //엔티티 조회 방식을 권장하는 이유: 엔티티 조회 방식은
    //패치 조인이나 hibernate.default_batch_fetch_size, @BatchSize 같이
    //코드를 거의 수정하지 않고, 옵션만 약간 변경해서 다양한 성능 최적화를 시도할 수 있다.
    //반면에 DTO를 직접 조회하는 방식은 성능을 최적화하거나 성능 최적화 방식을 변경할 때 많은 코드를 변경해야 한다.

    //개발자는 성능 최적화와 코드 복잡도 사이에서 줄타기를 해야 한다.
    //항상 그런 것은 아니지만 보통 성능 최적화는 단순한 코드를 복잡한 코드로 몰고 간다.
    //엔티티 조회 방식은 JPA가 많은 부분을 최적화 해주기 때문에, 단순한 코드를 유지하면서 성능을 최적화할 수 있다.

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
