package com.spring.board.model;

import java.util.List;
import java.util.Map;

public interface InterBoardDAO {

	int test_insert(); // spring_test 테이블에 insert 하기

	List<TestVO> test_select(); // spring_test 테이블에 select 하기

	// view단의 form 태그에서 입력받은 값을 spring_test 테이블에 isnert 하기
	int test_insert(Map<String, String> paraMap);

	int test_insert(TestVO vo);

	//////////////////////////////////////////////////////////////////////////
	
	// 시작페이지에서 메인 이미지를 보여주는 것
	List<String> getImgfilenameList();

	// 로그인 처리하기
	MemberVO getLoginMember(Map<String, String> paraMap);

	// === tbl_member 테이블의 idle 컬럼의 값을 1로 변경하기 === //
	int updateIdle(String string);
	
}
