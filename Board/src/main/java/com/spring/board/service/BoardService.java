package com.spring.board.service;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.spring.board.common.AES256;
import com.spring.board.model.*;

//=== #31. Service 선언 === 
//트랜잭션 처리를 담당하는곳, 업무를 처리하는 곳, 비지니스(Business)단
@Service
public class BoardService implements InterBoardService {

	// === #34. 의존객체 주입하기(DI: Dependency Injection) ===
	@Autowired	// Type에 따라 알아서 Bean 을 주입해준다.
	private InterBoardDAO dao;
	// Type 에 따라 Spring 컨테이너가 알아서 bean 으로 등록된 com.spring.board.model.BoardDAO 의 bean 을  dao 에 주입시켜준다. 
    // 그러므로 dao 는 null 이 아니다.
	
	// === #45. 양방향 암호화 알고리즘인 AES256 를 사용하여 복호화 하기 위한 클래스 의존객체 주입하기(DI: Dependency Injection) ===
	@Autowired
	private AES256 aes;
	// Type 에 따라 Spring 컨테이너가 알아서 bean 으로 등록된 com.spring.board.common.AES256 의 bean 을  aes 에 주입시켜준다. 
	// 그러므로 aes 는 null 이 아니다.
	// com.spring.board.common.AES256 의 bean 은 /webapp/WEB-INF/spring/appServlet/servlet-context.xml 파일에서 bean 으로 등록시켜주었음.  
	
	@Override
	public int test_insert() {
		
		int n = dao.test_insert();
		
		return n;
	}

	
	@Override
	public List<TestVO> test_select() {
		
		List<TestVO> testvoList = dao.test_select();
		
		return testvoList;
	}


	@Override
	public int test_insert(Map<String, String> paraMap) {

		int n = dao.test_insert(paraMap);
		
		return n;
	}


	@Override
	public int test_insert(TestVO vo) {
		int n = dao.test_insert(vo);
		return 0;
	}

	///////////////////////////////////////////////////////////////////////////////
	
	// === #37. 시작페이지에서 메인 이미지를 보여주는 것 === //
	@Override
	public List<String> getImgfilenameList() {
		List<String> ImgfilenameList = dao.getImgfilenameList();		
		return ImgfilenameList;
	}

	
	// === #42. 로그인 처리하기 === //
	@Override
	public MemberVO getLoginMember(Map<String, String> paraMap) {

		MemberVO loginuser = dao.getLoginMember(paraMap);
		
		// === #48. aes 의존객체를 사용하여 로그인 되어진 사용자(loginuser)의 이메일 값을 복호화 하도록 한다. === 
	    //          또한 암호변경 메시지와 휴면처리 유무 메시지를 띄우도록 업무처리를 하도록 한다.
		
		if(loginuser != null && loginuser.getPwdchangegap() >= 3) {
			// 마지막으로 암호를 변경한 날짜가 현재시각으로부터 3개월이 지났으면
			loginuser.setRequirePwdChange(true); // 로그인시 암호를 변경해라는 alert 를 띄우도록 한다.
		}
		
		if(loginuser != null && loginuser.getLastlogingap() >= 12) {
			// 마지막으로 로그인 한 날짜시간이 현재시각으로 부터 1년이 지났으면 휴면으로 지정
			loginuser.setIdle(1);
			
			// === tbl_member 테이블의 idle 컬럼의 값을 1로 변경하기 === //
			int n = dao.updateIdle(paraMap.get("userid"));
			
		}
		
		if(loginuser != null) {
			
			String email = "";
			try {
				email = aes.decrypt(loginuser.getEmail());
			} catch (UnsupportedEncodingException | GeneralSecurityException e) {
				e.printStackTrace();
			}
			
			loginuser.setEmail(email);
		}		
		
		return loginuser;
	}

	
	// ==== #55. 글쓰기(파일첨부가 없는 글쓰기) ==== // 
	@Override
	public int add(BoardVO boardvo) {

		
		
		int n = dao.add(boardvo);
		
		return n;
	}

	
	// ==== #59. 페이징 처리를 안한 검색어가 없는 전체 글목록 보여주기 ==== //
	@Override
	public List<BoardVO> boardListNoSearch() {
		List<BoardVO> boardList = dao.boardListNoSearch();
		return boardList;
	}


	// ==== #63. 글조회수 증가와 함께 글1개를 조회를 해주는 것 ==== //
	// (먼저, 로그인을 한 상태에서 다른 사람의 글을 조회할 경우에는 글조회수 컬럼의 값을 1증가 해야한다.)
	@Override
	public BoardVO getView(Map<String, String> paraMap) {

		BoardVO boardvo = dao.getView(paraMap);  // 글1개 조회하기	// 검색까지 포함되기때문에 map이다.
		
		String login_userid = paraMap.get("login_userid"); 
		// paraMap.get("login_userid") 은 로그인을 한 상태이라면 로그인한 사용자의 userid 이고,
		// 로그인을 하지 않은 상태이라면 paraMap.get("login_userid") 은 null 이다. 
		
		if(login_userid != null && 
		   boardvo != null &&
		  !login_userid.equals(boardvo.getFk_userid())) {
			// 글조회수 증가는 로그인을 한 상태에서 다른 사람의 글을 읽을때만 증가하도록 한다.
			
			dao.setAddReadCount(boardvo.getSeq());	// 글조회수 1증가 하기
			boardvo = dao.getView(paraMap);
		}
		
		return boardvo;
	}


	// ==== #70. 글조회수 증가는 없고 단순히 글1개 조회만을 해주는 것이다. ==== //
	@Override
	public BoardVO getViewWithNoAddCount(Map<String, String> paraMap) {
		BoardVO boardvo = dao.getView(paraMap);  // 글1개 조회하기
		return boardvo;
	}

	
	// === #73. 1개글 수정하기 === //	
	@Override
	public int edit(BoardVO boardvo) {
		int n = dao.edit(boardvo);
		return n;
	}

	
	// === #78. 1개글 삭제하기 === //
	@Override
	public int del(Map<String, String> paraMap) {
		int n = dao.del(paraMap);
		return n;
	}


	// === #85. 댓글쓰기(transaction 처리) === //
	// tbl_comment 테이블에 insert 된 다음에 
	// tbl_board 테이블에 commentCount 컬럼이 1증가(update) 하도록 요청한다.
	// 이어서 회원의 포인트를 50점을 증가하도록 한다.
	// 즉, 2개이상의 DML 처리를 해야하므로 Transaction 처리를 해야 한다. (여기서는 3개의 DML 처리가 일어남)
	// >>>>> 트랜잭션처리를 해야할 메소드에 @Transactional 어노테이션을 설정하면 된다. 
	// rollbackFor={Throwable.class} 은 롤백을 해야할 범위를 말하는데 Throwable.class 은 error 및 exception 을 포함한 최상위 루트이다. 즉, 해당 메소드 실행시 발생하는 모든 error 및 exception 에 대해서 롤백을 하겠다는 말이다.
	@Override
	@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED, rollbackFor= {Throwable.class})
	public int addComment(CommentVO commentvo) throws Throwable {

		return 0;
	}

}
