package com.shop.entity;

import com.shop.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order  extends BaseEntity  {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime orderDate; //주문일

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; //주문상태 ORDER CANCEL

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL
            ,orphanRemoval = true, fetch = FetchType.LAZY)
    //부모가 삭제되었을때 자식도 함께 삭제시키는 역할을 수햄
    private List<OrderItem> orderItems = new ArrayList<>();

// order 내부에 orderitems가 OneToMany로 양방향으로 선언되어 있기 때문에
    //양쪽에 값을 넣을 addOrderItem(OrderItem orderItem)
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this); //양방향 참조 관계이므로 orderItem 객체에도 order 객체셋팅
    }

    public static Order createOrder(Member member, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setMember(member); //상품을 주문한 회원의 정보를 셋팅

        for(OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
        }
        //상품페이지에서는 1개의 상품을 주문하지만, 장바구니 페이지에서는 한 번에 여럭 개의
        // 상품을 주문할 수 잇습니다. 따라서 여러 개의 주문 상품을 담을수 있도록 리스트형태로
        // 파라미터 값을 받으며 주문 객체에 orderItem 객체를 추가합니다. 
        

        order.setOrderStatus(OrderStatus.ORDER); //주문상태를  ORDER 로 셋팅
        order.setOrderDate(LocalDateTime.now()); //현재시간을 주문시간으로 세팅
        return order;
    }

    public int getTotalPrice() { //총주문 금액을 구하는 메소드
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
        //Item 클래스에서 주문 취소시 주문 수량을 상품의 재고에 더해주는 로직과
        //주문 상태를 취소 상태로 바꿔 주는 메소드를 구현합니다.
    }
}