package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemSearchDto {

    //all : 상품등록전체
    //1d : 최근 하루 동안 등록된 상품
    // 1w : 최근 일주일 동안 등록된 상품
    // 1m : 최근 한 달 동안 등록된 상품
    // 6m : 최근 6개월 동안 등록된 상품
    private String searchDateType;//상품등록일

    private ItemSellStatus searchSellStatus; //상품 판매 상태 기준으로 조회

    //itemNm : 상품명
    //createBy : 상품등록자 ID
    private String searchBy; //상품을 조회할때 어떤유형으로 조회할지

    private String searchQuery = "";
    //조회할 검색어를 저장할 변수 . searchBy 가 itemNm 상품명을 기준으로 검색, createBy 상품등록자 아이디 기준검색
}