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

	// 글쓰기(파일첨부가 없는 글쓰기)
	int add(BoardVO boardvo);

	// 페이징 처리를 안한 검색어가 없는 전체 글목록 보여주기
	List<BoardVO> boardListNoSearch();

	// 글 1개 조회하기
	BoardVO getView(Map<String, String> paraMap);
	// 글조회수 1증가하기
	void setAddReadCount(String seq);

	// 1개글 수정하기
	int edit(BoardVO boardvo);

	// 1개글 삭제하기
	int del(Map<String, String> paraMap);

	///////////////////////////////////////////////////////////////////////////// dao에서 트랜젝션 처리 없음(서비스에서 처리)
	int addComment(CommentVO commentvo); 				// 댓글쓰기(tbl_comment 테이블에 insert)
	int updateCommentCount(String parentSeq); 			// tbl_board 테이블에 commentCount 컬럼이 1증가(update)
	int updateMemberPoint(Map<String, String> paraMap);	// tbl_member 테이블의 point 컬럼의 값을 50점을 증가(update)	
	///////////////////////////////////////////////////////////////////////////

	// 원게시물에 딸린 댓글들을 조회해오기
	List<CommentVO> getCommentList(String parentSeq);

	// BoardAOP 클래스에서 사용하는 것으로 특정 회원에게 특정 점수만큼 포인트를 증가하기 위한 것 
	void pointPlus(Map<String, String> paraMap);

	// == 페이징 처리를 안한 검색어가 있는 전체 글목록 보여주기 == //
	List<BoardVO> boardListSearch(Map<String, String> paraMap);

	// 검색어 입력시 자동글 완성하기
	List<String> wordSearchShow(Map<String, String> paraMap);

	// 총 게시물 건수(totalCount) 구하기 - 검색이 있을때와 검색이 없을때로 나뉜다.
	int getTotalCount(Map<String, String> paraMap);

	// 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함 한 것)
	List<BoardVO> boardListSearchWithPaging(Map<String, String> paraMap);
	
	// 원게시물에 딸린 댓글들을 페이징 처리해서 조회해오기(Ajax 로 처리) == //
	List<CommentVO> getCommentListPaging(Map<String, String> paraMap);

	// 원글번호(parentSeq) 에 해당하는 댓글의 totalPage 수 알아오기
	int getCommentTotalPage(Map<String, String> paraMap);

	// tbl_board 테이블에서 groupno 컬럼의 최대값 알아오기
	int getGroupnoMax();

	// 글쓰기(파일첨부가 있는 글쓰기)
	int add_withFile(BoardVO boardvo);

	// 댓글 1개 조회해주는 것
	CommentVO getCommentOne(String seq);
}
