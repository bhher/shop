package com.shop.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MainItemDto {
    private Long id;
    private String itemNm;
    private String itemDetail;
    private String imgUrl;
    private Integer price;

    //Projection 이란? 테이블에서 원하는 컬럼만 뽑아서 조회하는 것


    //생성자에  @QueryProjection 어노테이션을 선언하여 Querydsl로 결과 조회시
    //MainitemDto 객체로 바로 받아오도록 활용하겠다.
    @QueryProjection
    public MainItemDto(Long id, String itemNm, String itemDetail, String imgUrl,Integer price){
        this.id = id;
        this.itemNm = itemNm;
        this.itemDetail = itemDetail;
        this.imgUrl = imgUrl;
        this.price = price;
    }

}
