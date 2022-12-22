package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
//import com.shop.dto.MainItemDto;
//import com.shop.dto.QMainItemDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import com.shop.entity.QItemImg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{

    //ItemRepositoryCustom 상속 받습니다.

    private JPAQueryFactory queryFactory;
    //동적으로 쿼리를 생성하기 위해서 JPAQueryFactory 클래스를 사용
    public ItemRepositoryCustomImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    //JPAQueryFactory 생성자로 EntityManager 객체를 넣어줍니다.
    }


    //Querydsl에서는 BooleanExpression을 사용해서 간단하면서도 재사용 가능한 동적쿼리를 만들 수 있다.
    //BooleanExpression의 값이 null이라면 조건문에 무시되기때문에 해당조건에 만족 하지 않으면 null
    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus){
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
    }//상품 판매 상태 조건이 전체 (null)일 경우는 null로 리턴 결과 값이 null 아니면
    //판매중 or 품절 상태라면 해당 조건의 상품만 조회

    private BooleanExpression regDtsAfter(String searchDateType){

        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all", searchDateType) || searchDateType == null){
            return null;
        } else if(StringUtils.equals("1d", searchDateType)){
            dateTime = dateTime.minusDays(1);
        } else if(StringUtils.equals("1w", searchDateType)){
            dateTime = dateTime.minusWeeks(1);
        } else if(StringUtils.equals("1m", searchDateType)){
            dateTime = dateTime.minusMonths(1);
        } else if(StringUtils.equals("6m", searchDateType)){
            dateTime = dateTime.minusMonths(6);
        }

        return QItem.item.regTime.after(dateTime);
    }
    //예를 들어 searchDateType 이 "1m"인 경우 date Time 의 시간을 한 달 전으로 세팅후
    // 최근 한 달 동안 등록된 상품만 조회하도록 조건값을 반환한다.



    //searchBy 값에 따라 상품명에 검색어를 포함하고 있는 상품 또는 상품  생성자의 아이디에 검색어를
    //포함하고 있는 상품을 조회하도록 조건값을 반환합니다.
    private BooleanExpression searchByLike(String searchBy, String searchQuery){

        if(StringUtils.equals("itemNm", searchBy)){
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        } else if(StringUtils.equals("createdBy", searchBy)){
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }

        return null;
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        List<Item> content = queryFactory //쿼리들 생성
                .selectFrom(QItem.item) //상품데이터를 조회하기 위해서 QItem의 item을 지정
                .where(regDtsAfter(itemSearchDto.getSearchDateType()), //BooleanExpression 반환하는 조건문을 넣어줌
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()), // ',' 는 and 조건으로 인식
                        searchByLike(itemSearchDto.getSearchBy(),
                                itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset()) //데이터를 가지고 올 시작 인텍스를 지정
                .limit(pageable.getPageSize()) // 한번에 가지고올 최대 개수
                .fetch();
        //조회한 리스트및 전체 개수를 포함 하는 content 로 반환 , 상품 데이터 리스트 조회및 상품 데이터 전체개수를
        // 조회하는 2번의 쿼리문 실행

        long total = queryFactory.select(Wildcard.count).from(QItem.item)
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .fetchOne()
                ;

        return new PageImpl<>(content, pageable, total);
        //조회한 데이터를 Page 클래스의 구현체인 PageImpl 객체로 반환
    }



    //검색어가 null 이 아니면 상품에 해당 검색어가 포함되는 상품을 조회하는 조건을 반환
    private BooleanExpression itemNmLike(String searchQuery){
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        List<MainItemDto> content = queryFactory
                .select(
                        //db조회 결과는 itemimg - item 조인된결과 반환 그중에 일부만 사용
                        new QMainItemDto( //QMainItemDto의 생성자에 반환할 값들을 넣어줍니다.
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price)
                        //@QueryProjection 을 사용하면 DTO로 바로조회 가능합니다.
                        //엔티티 조회후 DTO로 변환하는 과정을 줄일 수 있다.
                )
                .from(itemImg)
                .join(itemImg.item, item) // itemImg 테이블의 item 필드가 참조하는 item 테이블 조인
                .where(itemImg.repimgYn.eq("Y")) //5개이미지중 대표상품이미지만 부른다.
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(Wildcard.count)
                .from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repimgYn.eq("Y"))
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .fetchOne()
                ;

        return new PageImpl<>(content, pageable, total);
    }




}