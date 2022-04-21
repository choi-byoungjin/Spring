package com.spring.board.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

//=== #32. DAO 선언 ===//
@Repository
public class BoardDAO implements InterBoardDAO {

	// === #33. 의존객체 주입하기(DI: Dependency Injection) ===
	// >>> 의존 객체 자동 주입(Automatic Dependency Injection)은
	//     스프링 컨테이너가 자동적으로 의존 대상 객체를 찾아서 해당 객체에 필요한 의존객체를 주입하는 것을 말한다. 
	//     단, 의존객체는 스프링 컨테이너속에 bean 으로 등록되어 있어야 한다. 	

	//     의존 객체 자동 주입(Automatic Dependency Injection)방법 3가지 
	//     1. @Autowired ==> Spring Framework에서 지원하는 어노테이션이다. 
	//                       스프링 컨테이너에 담겨진 의존객체를 주입할때 타입을 찾아서 연결(의존객체주입)한다.
   
	//     2. @Resource  ==> Java 에서 지원하는 어노테이션이다.
	//                       스프링 컨테이너에 담겨진 의존객체를 주입할때 필드명(이름)을 찾아서 연결(의존객체주입)한다.
   
	//     3. @Inject    ==> Java 에서 지원하는 어노테이션이다.
    //                       스프링 컨테이너에 담겨진 의존객체를 주입할때 타입을 찾아서 연결(의존객체주입)한다.   
/*	
	@Autowired
	private SqlSessionTemplate abc; // abc는 SqlSessionTemplate 타입의 bean // abc에는 sqlsession이 들어온다.
*/
	@Resource
	private SqlSessionTemplate sqlsession; // 로컬DB mymvc_user 에 연결
	// Type 에 따라 Spring 컨테이너가 알아서 root-context.xml 에 생성된 org.mybatis.spring.SqlSessionTemplate 의  sqlsession bean 을  sqlsession 에 주입시켜준다. 
    // 그러므로 sqlsession 는 null 이 아니다.
	
	@Resource
	private SqlSessionTemplate sqlsession_2; // 로컬DB hr 에 연결
	// Type 에 따라 Spring 컨테이너가 알아서 root-context.xml 에 생성된 org.mybatis.spring.SqlSessionTemplate 의  sqlsession bean 을  sqlsession 에 주입시켜준다. 
    // 그러므로 sqlsession 는 null 이 아니다.
	
	// Type 에 따라 Spring 컨테이너가 알아서 root-context.xml 에 생성된 org.mybatis.spring.SqlSessionTemplate 의 bean 을  abc 에 주입시켜준다. 
    // 그러므로 abc 는 null 이 아니다.
	
	// spring_test 테이블에 insert 하기
	@Override
	public int test_insert() {
	//	int n = abc.insert("board.test_insert");
		
		int n = sqlsession.insert("board.test_insert"); // board는 namespace // board.xml에 있는 id가 test_insert인것
		int n_2 = sqlsession_2.insert("board.test_insert");
		
		return n*n_2;
	}

	// spring_test 테이블에 select 하기
	@Override
	public List<TestVO> test_select() {		
		List<TestVO> testvoList = sqlsession.selectList("board.test_select");		
		return testvoList;
	}


	// view단의 form 태그에서 입력받은 값을 spring_test 테이블에 isnert 하기
	@Override
	public int test_insert(Map<String, String> paraMap) {
		int n = sqlsession.insert("board.test_insert_map", paraMap);		
		return n;
	}

	// view단의 form 태그에서 입력받은 값을 spring_test 테이블에 isnert 하기
	@Override
	public int test_insert(TestVO vo) {		
		int n = sqlsession.insert("board.test_insert_vo", vo);		
		return n;		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	// === #38. 시작페이지에서 메인 이미지를 보여주는 것 === //
	@Override
	public List<String> getImgfilenameList() {
		List<String> ImgfilenameList = sqlsession.selectList("board.getImgfilenameList");
		return ImgfilenameList;
	}

	// === #46. 로그인 처리하기 === //
	@Override
	public MemberVO getLoginMember(Map<String, String> paraMap) {
		MemberVO loginuser = sqlsession.selectOne("board.getLoginMember", paraMap);
		return loginuser;
	}
	// tbl_member 테이블의 idle 컬럼의 값을 1로 변경하기 //
	@Override
	public int updateIdle(String userid) {
		int n = sqlsession.update("board.updateIdle", userid);
		return n;
	}

	
	// ==== #56. 글쓰기(파일첨부가 없는 글쓰기) ==== //
	@Override
	public int add(BoardVO boardvo) {
		int n = sqlsession.insert("board.add", boardvo);
		return n;
	}

	
	// ==== #60. 페이징 처리를 안한 검색어가 없는 전체 글목록 보여주기 ==== //
	@Override
	public List<BoardVO> boardListNoSearch() {
		List<BoardVO> boardList = sqlsession.selectList("board.boardListNoSearch");
		return boardList;
	}

	
	// ==== #64. 글 1개 조회하기 ==== //
	@Override
	public BoardVO getView(Map<String, String> paraMap) {
		BoardVO boardvo = sqlsession.selectOne("board.getView", paraMap);
		return boardvo;
	}

	
	// ==== #65. 글조회수 1증가하기 ==== //
	@Override
	public void setAddReadCount(String seq) {
		sqlsession.update("board.setAddReadCount", seq);
		
	}
		
}
