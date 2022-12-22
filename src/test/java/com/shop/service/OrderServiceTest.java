package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.constant.OrderStatus;
import com.shop.dto.OrderDto;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;

    public Item saveItem(){ //테스트를 위해서 주문할 상품 메소드 생성
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        return itemRepository.save(item);
    }

    public Member saveMember(){ //테스트를 위해서 회원정보를 저장하는 메소드 생성
        Member member = new Member();
        member.setEmail("test@test.com");
        return memberRepository.save(member);

    }

    @Test
    @DisplayName("주문 테스트")
    public void order(){
        Item item = saveItem();
        Member member = saveMember();

        OrderDto orderDto = new OrderDto();
        orderDto.setCount(10); //주문할 수량
        orderDto.setItemId(item.getId()); //주문할 상품을 orderDto에 저장

        Long orderId = orderService.order(orderDto, member.getEmail());
        //주문 로직 호출결과 생성된 주문 번호를  orderId 변수 에 저장
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        //주문버놓를 이용하여 저장된 주문정보를 조회
        List<OrderItem> orderItems = order.getOrderItems();

        int totalPrice = orderDto.getCount()*item.getPrice();
        //주문한 상품의 총가격을 구합니다.
        assertEquals(totalPrice, order.getTotalPrice());
        //주문한 상품 총가격과 디비에 저장된 상품의 가격을 비교하여 같으면 테스트가 성공적으로 종료
    }
    @Test
    @DisplayName("주문 취소 테스트")
    public void cancelOrder(){
        Item item = saveItem();
        Member member = saveMember();
//테스트를 위해서 상품과 회원 데이터를 생성. 생성한 재고는 100개
        OrderDto orderDto = new OrderDto();
        orderDto.setCount(10);
        orderDto.setItemId(item.getId());
        Long orderId = orderService.order(orderDto, member.getEmail());
        //주문데이터를 생성하고 , 주문데이터는 10개

        Order order = orderRepository.findById(orderId) //생성한 엔티티를 조회
                .orElseThrow(EntityNotFoundException::new);
        orderService.cancelOrder(orderId); // 해당주문 취소

        assertEquals(OrderStatus.CANCEL, order.getOrderStatus());
        //주문의 상태가 취소 상태라면 테스트가 통과합니다.
        assertEquals(100, item.getStockNumber());
        //취소후 상품의 재고가 처음 재고 개수인 100개와 동일하면 테스트 통과
    }







}