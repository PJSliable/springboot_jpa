package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // 지연 로딩인 경우에는 DB에서 new로 가져오지 않음.
    //null로 둘 순 없기에 hibernate에서 ProxyMember 객체를 생성해서 넣어 둠: bytebuddy
    //-> 실제로 들어가 있는 것 new ByteBuddyInterceptor()
    //지연 로딩인 경우에는 JSON으로 뿌리지 않도록 설정해야 함
    //-> @Bean Hibernate5Module을 다운해야 함
    //-> DTO 사용이 더 좋은 방법
    //지연 로딩을 피하기 위해서 즉시 로딩(EAGER)으로 설정하면 안됨
    // -> 연관관계가 필요 없는 경우에도(해당 API 뿐만 아니라 다른 API에서도) 데이터를 항상 조회해서 성능 문제가 발생할 수 있다. 성능 튜닝이 매우 어려워진다.
    // -> 지연 로딩을 기본으로 하고 성능 최적화가 필요한 경우에는 패치 조인(fetch join)을 사용해라
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //참조하는게 주인이 private owner인 경우에만 cascade 사용해 함
    //1:N은 기본이 LAZY
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL]

    //==연관관계 (편의) 메서드==//
    //컨트롤하는 쪽이 들고있는 게 좋음
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 로직==//
    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //==조회 로직==//

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;

//        return orderItems.stream()
//                .mapToInt(OrderItem::getTotalPrice)
//                .sum();
    }
}
