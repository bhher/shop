package com.shop.entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@EntityListeners(value = {AuditingEntityListener.class})
//Auditing을 적용하기 위해 어노테이션 추가
@MappedSuperclass
//공통 매핑 정보가 필요할때 사용하는 어노테이션 부모 클래스를 상속
//받는 자식 클래스에 매핑 정보만 제공
@Getter
@Setter
public abstract class BaseTimeEntity {
    @CreatedDate //엔티티가 생성되어 저장할 때 시간자동 저장 
    @Column(updatable = false)
    private LocalDateTime regTime;

    @LastModifiedDate//엔티티의 값을 변경할 때 시간을 자동 저장
    private LocalDateTime updateTime;
}
//보통 테이블에 등록일 ,수정일, 등록자, 수정자 를 모두 다 넣어주지만
//어떤 테이블은 등록자, 수정자를 넣지 않는 테이블도 잇따.
// 그런 Entity는 BaseTimeEntity만 상속 받을 수 있도록 BaseTimeEntity
//클래스를 생성