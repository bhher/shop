package com.shop.repository;

import com.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByEmail(String email);
    //회원가입시 중복된 회원이 있는지 검사하기 위해 이메일로 회원을 검사 할 수 있도록
    //쿼리 메소드를 작성
}