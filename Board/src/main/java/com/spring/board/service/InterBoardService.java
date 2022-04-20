package com.spring.board.service;

import java.util.*;

import com.spring.board.model.*;

public interface InterBoardService {

	int test_insert();

	List<TestVO> test_select();

	int test_insert(Map<String, String> paraMap);

	int test_insert(TestVO vo);

	////////////////////////////////////////////////////////////////////
	
	// 시작페이지에서 메인 이미지를 보여주는 것
	List<String> getImgfilenameList();

	// 로그인 처리하기
	MemberVO getLoginMember(Map<String, String> paraMap);
	
}
