package com.spring.board.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.spring.board.common.MyUtil;
import com.spring.board.common.Sha256;
import com.spring.board.model.*;
import com.spring.board.service.*;

/*
	사용자 웹브라우저 요청(View)  ==> DispatcherServlet ==> @Controller 클래스 <==>> Service단(핵심업무로직단, business logic단) <==>> Model단[Repository](DAO, DTO) <==>> myBatis <==>> DB(오라클)           
	(http://...  *.action)                                  |                                                                                                                              
	 ↑                                                View Resolver
	 |                                                      ↓
	 |                                                View단(.jsp 또는 Bean명)
	 -------------------------------------------------------| 
	
	사용자(클라이언트)가 웹브라우저에서 http://localhost:9090/board/test/test_insert.action 을 실행하면
	배치서술자인 web.xml 에 기술된 대로  org.springframework.web.servlet.DispatcherServlet 이 작동된다.
	DispatcherServlet 은 bean 으로 등록된 객체중 controller 빈을 찾아서  URL값이 "/test_insert.action" 으로
	매핑된 메소드를 실행시키게 된다.                                               
	Service(서비스)단 객체를 업무 로직단(비지니스 로직단)이라고 부른다.
	Service(서비스)단 객체가 하는 일은 Model단에서 작성된 데이터베이스 관련 여러 메소드들 중 관련있는것들만을 모아 모아서
	하나의 트랜잭션 처리 작업이 이루어지도록 만들어주는 객체이다.
	여기서 업무라는 것은 데이터베이스와 관련된 처리 업무를 말하는 것으로 Model 단에서 작성된 메소드를 말하는 것이다.
	이 서비스 객체는 @Controller 단에서 넘겨받은 어떤 값을 가지고 Model 단에서 작성된 여러 메소드를 호출하여 실행되어지도록 해주는 것이다.
	실행되어진 결과값을 @Controller 단으로 넘겨준다.
*/

//=== #30. 컨트롤러 선언 ===
@Component
/* XML에서 빈을 만드는 대신에 클래스명 앞에 @Component 어노테이션을 적어주면 해당 클래스는 bean으로 자동 등록된다. 
     그리고 bean의 이름(첫글자는 소문자)은 해당 클래스명이 된다. 
     즉, 여기서 bean의 이름은 boardController 이 된다. 
     여기서는 @Controller 를 사용하므로 @Component 기능이 이미 있으므로 @Component를 명기하지 않아도 BoardController 는 bean 으로 등록되어 스프링컨테이너가 자동적으로 관리해준다. 
*/
@Controller
public class BoardController {
 
	// === #35. 의존객체 주입하기(DI: Dependency Injection) ===
	// ※ 의존객체주입(DI : Dependency Injection) 
	//  ==> 스프링 프레임워크는 객체를 관리해주는 컨테이너를 제공해주고 있다.
	//      스프링 컨테이너는 bean으로 등록되어진 BoardController 클래스 객체가 사용되어질때, 
	//      BoardController 클래스의 인스턴스 객체변수(의존객체)인 BoardService service 에 
	//      자동적으로 bean 으로 등록되어 생성되어진 BoardService service 객체를  
	//      BoardController 클래스의 인스턴스 변수 객체로 사용되어지게끔 넣어주는 것을 의존객체주입(DI : Dependency Injection)이라고 부른다. 
	//      이것이 바로 IoC(Inversion of Control == 제어의 역전) 인 것이다.
	//      즉, 개발자가 인스턴스 변수 객체를 필요에 의해 생성해주던 것에서 탈피하여 스프링은 컨테이너에 객체를 담아 두고, 
	//      필요할 때에 컨테이너로부터 객체를 가져와 사용할 수 있도록 하고 있다. 
	//      스프링은 객체의 생성 및 생명주기를 관리할 수 있는 기능을 제공하고 있으므로, 더이상 개발자에 의해 객체를 생성 및 소멸하도록 하지 않고
	//      객체 생성 및 관리를 스프링 프레임워크가 가지고 있는 객체 관리기능을 사용하므로 Inversion of Control == 제어의 역전 이라고 부른다.  
	//      그래서 스프링 컨테이너를 IoC 컨테이너라고도 부른다.
	
	//  IOC(Inversion of Control) 란 ?
	//  ==> 스프링 프레임워크는 사용하고자 하는 객체를 빈형태로 이미 만들어 두고서 컨테이너(Container)에 넣어둔후
	//      필요한 객체사용시 컨테이너(Container)에서 꺼내어 사용하도록 되어있다.
	//      이와 같이 객체 생성 및 소멸에 대한 제어권을 개발자가 하는것이 아니라 스프링 Container 가 하게됨으로써 
	//      객체에 대한 제어역할이 개발자에게서 스프링 Container로 넘어가게 됨을 뜻하는 의미가 제어의 역전 
	//      즉, IOC(Inversion of Control) 이라고 부른다.
	
	
	//  === 느슨한 결합 ===
	//      스프링 컨테이너가 BoardController 클래스 객체에서 BoardService 클래스 객체를 사용할 수 있도록 
	//      만들어주는 것을 "느슨한 결합" 이라고 부른다.
	//      느스한 결합은 BoardController 객체가 메모리에서 삭제되더라도 BoardService service 객체는 메모리에서 동시에 삭제되는 것이 아니라 남아 있다.
	
	// ===> 단단한 결합(개발자가 인스턴스 변수 객체를 필요에 의해서 생성해주던 것)
	// private InterBoardService service = new BoardService(); 
	// ===> BoardController 객체가 메모리에서 삭제 되어지면  BoardService service 객체는 멤버변수(필드)이므로 메모리에서 자동적으로 삭제되어진다.	
	

	@Autowired    // Type에 따라 알아서 Bean 을 주입해준다.
    private InterBoardService service;
	
	
	// ======== ***** spring 기초 시작 ***** ======== //
	@RequestMapping(value="/test/test_insert.action")
	public String test_insert(HttpServletRequest request) {
		
		int n = service.test_insert();
		
		String message = "";
		
		if(n==1) {
			message = "데이터 입력 성공!!";
		}
		else {
			message = "데이터 입력 실패!!";
		}
		
		request.setAttribute("message", message);
		request.setAttribute("n", n);
		
		return "test/test_insert";
	//  /WEB-INF/views/test/test_insert.jsp 페이지를 만들어야 한다.	
	}
	
	
	@RequestMapping(value="/test/test_select.action")
	public String test_select(HttpServletRequest request) {
		
		List<TestVO> testvoList = service.test_select();
		
		request.setAttribute("testvoList", testvoList);
		
		return "test/test_select";
	//  /WEB-INF/views/test/test_select.jsp 페이지를 만들어야 한다.
	}
	
	
//	@RequestMapping(value="/test/test_form.action", method = {RequestMethod.GET})  // 오로지 GET방식만 허락하는 것임.
//	@RequestMapping(value="/test/test_form.action", method = {RequestMethod.POST}) // 오로지 POST방식만 허락하는 것임. 
	@RequestMapping(value="/test/test_form.action") // GET방식 및  POST방식 둘 모두 허락하는 것임.  
	public String test_form(HttpServletRequest request) {
		
		String method = request.getMethod();
		
		if("GET".equalsIgnoreCase(method)) { // GET 방식이라면 
			return "test/test_form"; // view 단 페이지를 띄워라
			//  /WEB-INF/views/test/test_form.jsp 페이지를 만들어야 한다.
		}
		else { // POST 방식이라면
			String no = request.getParameter("no");
			String name = request.getParameter("name");
			
			Map<String, String> paraMap = new HashMap<>();
			paraMap.put("no", no);
			paraMap.put("name", name);
			
			int n = service.test_insert(paraMap);
			
			if(n==1) {
				return "redirect:/test/test_select.action";  
			//  /test/test_select.action 페이지로 redirect(페이지이동)해라는 말이다.  
			}
			else {
				return "redirect:/test/test_form.action";
			//  /test/test_form.action 페이지로 redirect(페이지이동)해라는 말이다.	
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	@RequestMapping(value="/test/test_form_vo.action") // GET방식 및  POST방식 둘 모두 허락하는 것임.  
	public String test_form_vo(HttpServletRequest request, TestVO vo) {
		
		String method = request.getMethod();
		
		if("GET".equalsIgnoreCase(method)) { // GET 방식이라면 
			return "test/test_form_vo"; // view 단 페이지를 띄워라
			//  /WEB-INF/views/test/test_form_vo.jsp 페이지를 만들어야 한다.
		}
		else { // POST 방식이라면
			
			int n = service.test_insert(vo);
			
			if(n==1) {
				return "redirect:/test/test_select.action";  
			//  /test/test_select.action 페이지로 redirect(페이지이동)해라는 말이다.  
			}
			else {
				return "redirect:/test/test_form.action";
			//  /test/test_form.action 페이지로 redirect(페이지이동)해라는 말이다.	
			}
		}
	}
	/////////////////////////////////////////////////////////////////////
	
	
	@RequestMapping(value="/test/test_form_2.action", method = {RequestMethod.GET})  // 오로지 GET방식만 허락하는 것임. 
	public String test_form_2() {
		
		return "test/test_form_2"; // view 단 페이지를 띄워라
	//  /WEB-INF/views/test/test_form_2.jsp 페이지를 만들어야 한다.
	}
	
	
	@RequestMapping(value="/test/test_form_2.action", method = {RequestMethod.POST}) // 오로지 POST방식만 허락하는 것임.
	public String test_form_2(HttpServletRequest request) {
		
		String no = request.getParameter("no");
		String name = request.getParameter("name");
		
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("no", no);
		paraMap.put("name", name);
		
		int n = service.test_insert(paraMap);
		
		if(n==1) {
			return "redirect:/test/test_select.action";  
		//  /test/test_select.action 페이지로 redirect(페이지이동)해라는 말이다.  
		}
		else {
			return "redirect:/test/test_form.action";
		//  /test/test_form.action 페이지로 redirect(페이지이동)해라는 말이다.	
		}
	}
	
	
	// === AJAX 연습시작 === //
	@RequestMapping(value="/test/test_form_3.action", method = {RequestMethod.GET})  // 오로지 GET방식만 허락하는 것임. 
	public String test_form_3() {
		
		return "test/test_form_3"; // view 단 페이지를 띄워라
	//  /WEB-INF/views/test/test_form_3.jsp 페이지를 만들어야 한다.
	}
	
	
/*
    @ResponseBody 란?
	  메소드에 @ResponseBody Annotation이 되어 있으면 return 되는 값은 View 단 페이지를 통해서 출력되는 것이 아니라 
	 return 되어지는 값 그 자체를 웹브라우저에 바로 직접 쓰여지게 하는 것이다. 
	 일반적으로 JSON 값을 Return 할때 많이 사용된다. 
 */
	@ResponseBody
	@RequestMapping(value="/test/ajax_insert.action", method = {RequestMethod.POST}) // 오로지 POST방식만 허락하는 것임.
	public String ajax_insert(HttpServletRequest request) {
		
		String no = request.getParameter("no");
		String name = request.getParameter("name");
		
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("no", no);
		paraMap.put("name", name);
		
		int n = service.test_insert(paraMap);
		
		JSONObject jsonObj = new JSONObject(); // {}
		jsonObj.put("n", n);  // {"n":1}
		
		return jsonObj.toString(); // "{"n":1}"
	}
	
	
/*
    @ResponseBody 란?
	  메소드에 @ResponseBody Annotation이 되어 있으면 return 되는 값은 View 단 페이지를 통해서 출력되는 것이 아니라 
	 return 되어지는 값 그 자체를 웹브라우저에 바로 직접 쓰여지게 하는 것이다. 일반적으로 JSON 값을 Return 할때 많이 사용된다.  
	
	>>> 스프링에서 json 또는 gson을 사용한 ajax 구현시 데이터를 화면에 출력해 줄때 한글로 된 데이터가 '?'로 출력되어 한글이 깨지는 현상이 있다. 
               이것을 해결하는 방법은 @RequestMapping 어노테이션의 속성 중 produces="text/plain;charset=UTF-8" 를 사용하면 
               응답 페이지에 대한 UTF-8 인코딩이 가능하여 한글 깨짐을 방지 할 수 있다. <<< 
*/
	@ResponseBody
	@RequestMapping(value="/test/ajax_select.action", method = {RequestMethod.GET}, produces="text/plain;charset=UTF-8") // 오로지 GET방식만 허락하는 것임.  
	public String ajax_select() {
		
		List<TestVO> testvoList = service.test_select();
		
		JSONArray jsonArr = new JSONArray(); // []
		
		if(testvoList != null) {
			for(TestVO vo : testvoList) {
				JSONObject jsonObj = new JSONObject();     // {}            {}
				jsonObj.put("no", vo.getNo());             // {"no":"101"}  {"no":"102"}
				jsonObj.put("name", vo.getName());         // {"no":"101","name":"이순신"}  {"no":"102","name":"엄정화"}
				jsonObj.put("writeday", vo.getWriteday()); // {"no":"101","name":"이순신","writeday":"2022-04-19 15:20:30"}  {"no":"102","name":"엄정화","writeday":"2022-04-19 15:22:50"}
				
				jsonArr.put(jsonObj);                      // [{"no":"101","name":"이순신","writeday":"2022-04-19 15:20:30"},{"no":"102","name":"엄정화","writeday":"2022-04-19 15:22:50"}]
			}// end of for-----------------------
		}
		
		return jsonArr.toString(); // "[{"no":"101","name":"이순신","writeday":"2022-04-19 15:20:30"},{"no":"102","name":"엄정화","writeday":"2022-04-19 15:22:50"}]"
	}
	
	
	
	// === return 타입을 String 대신에 ModelAndView 를 사용해보겠습니다. === //
	@RequestMapping(value="/test/test_form_vo_modelandview.action") // GET방식 및  POST방식 둘 모두 허락하는 것임.  
	public ModelAndView test_form(HttpServletRequest request, TestVO vo, ModelAndView mav) {
		
		String method = request.getMethod();
		
		if("GET".equalsIgnoreCase(method)) { // GET 방식이라면 
			mav.setViewName("test/test_form_vo_modelandview");
		 // view 단 페이지의 파일명 지정하기
		 // /WEB-INF/views/test/test_form_vo_modelandview.jsp 페이지를 만들어야 한다.
		}
		else { // POST 방식이라면
						
			int n = service.test_insert(vo);
			
			if(n==1) {
				mav.setViewName("redirect:/test/test_select_modelandview.action");
			//  /test/test_select_modelandview.action 페이지로 redirect(페이지이동)해라는 말이다.  
			}
			else {
				mav.setViewName("redirect:/test/test_form_vo_modelandview.action");
			//  /test/test_form_vo_modelandview.action 페이지로 redirect(페이지이동)해라는 말이다.	
			}
		}
		
		return mav;
	}
	
	
	@RequestMapping(value="/test/test_select_modelandview.action")
	public ModelAndView test_select_modelandview(ModelAndView mav) {
		
		List<TestVO> testvoList = service.test_select();
		
		mav.addObject("testvoList", testvoList);
		mav.setViewName("test/test_select_modelandview");
	    //  /WEB-INF/views/test/test_select_modelandview.jsp 페이지를 만들어야 한다.
		
		return mav;
	}
	// ======== ***** spring 기초 끝 ***** ======== //
	
	
	// ======== ***** tiles 연습 시작 ***** ======== //
	@RequestMapping(value="/test/tiles_test_1.action")
	public String tiles_test_1() {
		
		return "tiles_test_1.tiles1";
	    //  /WEB-INF/views/tiles1/tiles_test_1.jsp 페이지를 만들어야 한다.
	}
	
	
	@RequestMapping(value="/test/tiles_test_2.action")
	public String tiles_test_2() {
		
		return "test/tiles_test_2.tiles1";
	    //  /WEB-INF/views/tiles1/test/tiles_test_2.jsp 페이지를 만들어야 한다.
	}
	
	
	@RequestMapping(value="/test/tiles_test_3.action")
	public String tiles_test_3() {
		
		return "test/sample/tiles_test_3.tiles1";
	    //  /WEB-INF/views/tiles1/test/sample/tiles_test_3.jsp  페이지를 만들어야 한다.
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	
	@RequestMapping(value="/test/tiles_test_4.action")
	public ModelAndView tiles_test_4(ModelAndView mav) {
		
		mav.setViewName("tiles_test_4.tiles2");
		//  /WEB-INF/views/tiles2/tiles_test_4.jsp 페이지를 만들어야 한다.
		
		return mav;
	}
	
	
	@RequestMapping(value="/test/tiles_test_5.action")
	public ModelAndView tiles_test_5(ModelAndView mav) {
		
		mav.setViewName("test/tiles_test_5.tiles2");
		//  /WEB-INF/views/tiles2/test/tiles_test_5.jsp 페이지를 만들어야 한다.
		
		return mav;
	}
	
	
	@RequestMapping(value="/test/tiles_test_6.action")
	public ModelAndView tiles_test_6(ModelAndView mav) {
		
		mav.setViewName("test/sample/tiles_test_6.tiles2");
		//  /WEB-INF/views/tiles2/test/sample/tiles_test_6.jsp 페이지를 만들어야 한다.
		
		return mav;
	}
	
	// ======== ***** tiles 연습 끝 ***** ======== //
	
	
	// ================== ***** 게시판 시작 ***** ========================= //	
	
	// === #36. 메인 페이지 요청 === //
	@RequestMapping(value="/index.action")
	public ModelAndView index(ModelAndView mav, HttpServletRequest request) {
		
		getCurrentURL(request); // 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출
		
		List<String> imgfilenameList = service.getImgfilenameList();
		
		mav.addObject("imgfilenameList", imgfilenameList);
		mav.setViewName("main/index.tiles1");
		//  /WEB-INF/views/tiles1/main/index.jsp 파일을 생성한다.
		
		return mav;		
	}
	
	
	// === #40. 로그인 폼 페이지 요청 === //
	@RequestMapping(value="/login.action", method= {RequestMethod.GET})
	public ModelAndView login(ModelAndView mav) {
		
		mav.setViewName("login/loginform.tiles1");
		//	/WEB-INF/views/tiles1/loginform.jsp 파일을 생성한다.
		
		return mav;
	}
	
	
	// === #41. 로그인 처리하기 === //	
	@RequestMapping(value="/loginEnd.action", method= {RequestMethod.POST})
	public ModelAndView loginEnd(ModelAndView mav, HttpServletRequest request) {
		
		String userid = request.getParameter("userid");
		String pwd = request.getParameter("pwd");
		
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("userid", userid);
		paraMap.put("pwd", Sha256.encrypt(pwd));
		
		MemberVO loginuser = service.getLoginMember(paraMap);
		
		if(loginuser == null) { // 로그인 실패시
			String message = "아이디 또는 암호가 틀립니다.";
			String loc = "javascript:history.back()";
			
			mav.addObject("message", message);
			mav.addObject("loc", loc);
			
			mav.setViewName("msg");
			//	/WEB-INF/views/msg.jsp 파일을 생성한다.
		}
		else { // 아이디와 암호가 존재하는 경우
			
			if(loginuser.getIdle() == 1) { // 로그인 한지 1년이 경과한 경우
				String message = "로그인을 한지 1년이 지나서 휴면상태로 되었습니다.\\n관리자가에게 문의 바랍니다.";
				String loc = request.getContextPath()+"/index.action";
				// 원래는 위와 같이 index.action 이 아니라 휴면인 계정을 풀어주는 페이지로 잡아주어야 한다.
				
				mav.addObject("message", message);
				mav.addObject("loc", loc);
				mav.setViewName("msg");
			}
			
			else { // 로그인 한지 1년 이내인 경우
				
				HttpSession session = request.getSession();
				// 메모리에 생성되어져 있는 session 을 불러오는 것이다.
				
				session.setAttribute("loginuser", loginuser);
				// session(세션)에 로그인 되어진 사용자 정보인 loginuser 을 키이름을 "loginuser" 으로 저장시켜두는 것이다.
				
				if(loginuser.isRequirePwdChange() == true) { // 암호를 마지막으로 변경한 것이 3개월이 경과한 경우
					
					String message = "비밀번호를 변경하신지 3개월이 지났습니다.\\n암호를 변경하시는 것을 추천합니다.";
					String loc = request.getContextPath()+"/index.action";
					// 원래는 위와 같이 index.action 이 아니라 사용자의 암호를 변경해주는 페이지로 잡아주어야 한다.
					
					mav.addObject("message", message);
					mav.addObject("loc", loc);
					mav.setViewName("msg");
					
				}
				
				else { // 암호를 마지막으로 변경한 것이 3개월이 이내인 경우
					
					// 로그인을 해야만 접근할 수 있는 페이지에 로그인을 하지 않은 상태에서 접근을 시도한 경우 
					// "먼저 로그인을 하세요!!" 라는 메시지를 받고서 사용자가 로그인을 성공했다라면
					// 화면에 보여주는 페이지는 시작페이지로 가는 것이 아니라
					// 조금전 사용자가 시도하였던 로그인을 해야만 접근할 수 있는 페이지로 가기 위한 것이다.
					
					String goBackURL = (String) session.getAttribute("goBackURL");
					
					if(goBackURL !=  null) {
						mav.setViewName("redirect:"+goBackURL);
						session.removeAttribute("goBackURL"); // 세션에서 반드시 제거해주어야 한다.
					}
					else {
						mav.setViewName("redirect:/index.action"); // 시작페이지로 이동
					}
				}
				
			}
			
		}
		
		return mav;
	}
	
	
	// === #50. 로그아웃 처리하기 === //	
	@RequestMapping(value="/logout.action")
	public ModelAndView logout(ModelAndView mav, HttpServletRequest request) {
		
	/*	
		// 로그아웃시 시작페이지로 돌아가는 것임 //
		HttpSession session = request.getSession();
		session.invalidate();
		
		String message = "로그아웃 되었습니다.";
		String loc = request.getContextPath()+"/index.action";
		
		mav.addObject("message", message);
		mav.addObject("loc", loc);
		mav.setViewName("msg");
		// /WEB-INF/views/msg.jsp
		
		return mav;
	*/	
		
		// 로그아웃시 현재 보았던 그 페이지로 돌아가는 것임 //
		HttpSession session = request.getSession();
		
		String goBackURL = (String) session.getAttribute("goBackURL");
		
		session.invalidate();
		
		String message = "로그아웃 되었습니다.";
		
		String loc = "";
		if(goBackURL != null) {
			loc = request.getContextPath()+goBackURL;
		}
		else {
			loc = request.getContextPath()+"/index.action";
		}
		
		mav.addObject("message", message);
		mav.addObject("loc", loc);
		mav.setViewName("msg");
		// /WEB-INF/views/msg.jsp
		
		return mav;
	}

	
	// === #51. 게시판 글쓰기 폼페이지 요청 === //
	@RequestMapping(value="/add.action")
	public ModelAndView requiredLogin_add(HttpServletRequest request, HttpServletResponse response, ModelAndView mav) {
		
	//	getCurrentURL(request); // 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출 // 반드시 로그인 해야 들어갈 수 있기때문에 주석처리
		
		mav.setViewName("board/add.tiles1");
		// /WEB-INF/views/tiles1/board/add.jsp 파일을 생성한다.
		
		return mav;
	}
	
	
	// === #54. 게시판 글쓰기 완료 요청=== //
	@RequestMapping(value="/addEnd.action", method= {RequestMethod.POST})
//	public ModelAndView addEnd(ModelAndView mav, BoardVO boardvo) {		<== After Advice 를 사용하기 전
	public ModelAndView pointPlus_addEnd(Map<String,String> paraMap, ModelAndView mav, BoardVO boardvo) {	//	<== After Advice 를 사용하기
	/*
       form 태그의 name 명과  BoardVO 의 필드명이 같다라면 
       request.getParameter("form 태그의 name명"); 을 사용하지 않더라도
             자동적으로 BoardVO boardvo 에 set 되어진다.
    */
		
		int n = service.add(boardvo); // <== 파일첨부가 없는 글쓰기
		
		if(n==1) {
			mav.setViewName("redirect:/list.action");
			//	/list.action 페이지로 redirect(페이지이동)하라는 말이다.
		}
		else {
			mav.setViewName("board/error/add_error.tiles1");
			// /WEB-INF/views/tiles1/board/error/add_error.jsp 파일을 생성한다.	
		}
		
		// === #96. After Advice 를 사용하기 === //
		// 			글쓰기를 한 이후에 회원의 포인트를 100 점 증가
		// == After Advice 를 사용하기 위해 파라미터를 생성하는 것임 ==
		paraMap.put("fk_userid", boardvo.getFk_userid());
		paraMap.put("point", "100");
		//////////////////////////////////////////////////////
		
		return mav;
	}
	
	
	
	// === #58.글목록 보기 페이지 요청 === //
	@RequestMapping(value="/list.action")
	public ModelAndView list(ModelAndView mav, HttpServletRequest request) {
		
		getCurrentURL(request); // 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출 
		
		List<BoardVO> boardList = null;
		
		//////////////////////////////////////////////////////
		// === #69. 글조회수(readCount)증가 (DML문 update)는
		//          반드시 목록보기에 와서 해당 글제목을 클릭했을 경우에만 증가되고,
		//          웹브라우저에서 새로고침(F5)을 했을 경우에는 증가가 되지 않도록 해야 한다.
		//          이것을 하기 위해서는 session 을 사용하여 처리하면 된다.
		HttpSession session = request.getSession();
		session.setAttribute("readCountPermission", "yes");
		/*
			session 에  "readCountPermission" 키값으로 저장된 value값은 "yes" 이다.
			session 에  "readCountPermission" 키값에 해당하는 value값 "yes"를 얻으려면 
			반드시 웹브라우저에서 주소창에 "/list.action" 이라고 입력해야만 얻어올 수 있다. 
		 */
		//////////////////////////////////////////////////////
		
		// == 페이징 처리를 안한 검색어가 없는 전체 글목록 보여주기 == //
	//	boardList = service.boardListNoSearch();
		
	/*
		// == #102. 페이징 처리를 안한 검색어가 있는 전체 글목록 보여주기 == //
		String searchType = request.getParameter("searchType");
		String searchWord = request.getParameter("searchWord");
		
		if(searchType == null) {
			searchType = "";
		}
		
		if(searchWord == null) {
			searchWord = "";
		}
		
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("searchType", searchType);
		paraMap.put("searchWord", searchWord);
		
		boardList = service.boardListSearch(paraMap);
		
		
		///////////////////////////////////////////////////////////////////////
		
		// 아래는 검색대상 컬럼과 검색어를 유지시키기 위한 것이다.
		if(!"".equals(searchType) && !"".equals(searchWord)) {
			mav.addObject("paraMap", paraMap);
		}
	*/	
		///////////////////////////////////////////////////////////////////////
		
		
		// == #114. 페이징 처리를 한 검색어가 있는 전체 글목록 보여주기 시작 == //
		/* 	
		  	페이징 처리를 통한 글목록 보여주기는 
			예를 들어 3페이지의 내용을 보고자 한다라면 검색을 할 경우는 아래와 같이
     		list.action?searchType=subject&searchWord=안녕&currentShowPageNo=3 와 같이 해주어야 한다.
        	또는 
        	검색이 없는 전체를 볼때는 아래와 같이 
     		list.action 또는 
     		list.action?searchType=&searchWord=&currentShowPageNo=3 또는 
     		list.action?searchType=subject&searchWord=&currentShowPageNo=3 와 같이 해주어야 한다.
		 */
		
		String searchType = request.getParameter("searchType");
		String searchWord = request.getParameter("searchWord");
		String str_currentShowPageNo = request.getParameter("currentShowPageNo");
				
		if(searchType == null || (!"subject".equals(searchType) && !"name".equals(searchType))) {
			searchType = "";
		}
		
		if(searchWord == null || "".equals(searchWord) || searchWord.trim().isEmpty()) {
			searchWord = "";
		}
		
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("searchType", searchType);
		paraMap.put("searchWord", searchWord);
		
		// 먼저 총 게시물 건수(totalCount)를 구해와야 한다.
		// 총 게시물 건수(totalCount)는 검색조건이 있을때와 없을때로 나뉘어진다.
		int totalCount = 0; 		// 총 게시물 건수
		int sizePerPage = 3; 		// 한 페이지당 보여줄 게시물 건수
		int currentShowPageNo = 0; 	// 현재 보여주는 페이지번호로서, 초기치로는 1페이지로 설정함.
		int totalPage = 0;			// 총 페이지수(웹브라우저상에서 보여줄 총 페이지 개수, 페이지바)
		
		int startRno = 0; 	// 시작 행번호
		int endRno = 0; 	// 끝 행번호
		
		// 총 게시물 건수(totalCount)
		totalCount = service.getTotalCount(paraMap);
	//	System.out.println("~~~~~ 확인용 totalCount : " + totalCount);
		
		// 만약에 총 게시물 건수(totalCount)가 127개 이라면
		// 총 페이지수(totalPage)는 13개가 되어야 한다.
		
		totalPage = (int) Math.ceil((double)totalCount/sizePerPage);
		// (double)127/10 ==> 12.7 ==> Math.ceil(12.7) ==> 13.0 ==> (int)13.0 ==> 13
		// (double)120/10 ==> 12.0 ==> Math.ceil(12.0) ==> 12.0 ==> (int)12.0 ==> 12
		
		if(str_currentShowPageNo == null) {
			// 게시판에 보여지는 초기화면
			currentShowPageNo = 1;
		}
		else {
			try {
				currentShowPageNo = Integer.parseInt(str_currentShowPageNo);
				if( currentShowPageNo < 1 || currentShowPageNo > totalPage) {
					currentShowPageNo = 1;
				}
			} catch(NumberFormatException e) {
				currentShowPageNo = 1;
			}			
		}
		// **** 가져올 게시글의 범위를 구한다.(공식임!!!) **** 
		/*
			currentShowPageNo      startRno     endRno
			--------------------------------------------
			1 page        ===>    1           10
			2 page        ===>    11          20
			3 page        ===>    21          30
			4 page        ===>    31          40
			......                ...         ...
		 */
		
		startRno = ((currentShowPageNo - 1) * sizePerPage) + 1;
		endRno = startRno + sizePerPage - 1;
		
		paraMap.put("startRno", String.valueOf(startRno));
		paraMap.put("endRno", String.valueOf(endRno));
		
		// ~~~~~~~~~~~~
		
		boardList = service.boardListSearchWithPaging(paraMap);
		// 페이징 처리한 글목록 가져오기(검색이 있든지, 검색이 없든지 모두 다 포함 한 것)
		
		// 아래는 검색대상 컬럼과 검색어를 유지시키기 위한 것이다.
		if(!"".equals(searchType) && !"".equals(searchWord)) {
			mav.addObject("paraMap", paraMap);
		}
		
		
		// === #121. 페이지바 만들기 === //
		int blockSize = 2;
		// blockSize 는 1개 블럭(토막)당 보여지는 페이지번호의 개수이다.
		/*
		        		  1  2  3  4  5  6  7  8  9 10 [다음][마지막]  -- 1개블럭
			[맨처음][이전]  11 12 13 14 15 16 17 18 19 20 [다음][마지막]  -- 1개블럭
			[맨처음][이전]  21 22 23
		*/
		
		int loop = 1;
		/*
	        loop는 1부터 증가하여 1개 블럭을 이루는 페이지번호의 개수[ 지금은 10개(== blockSize) ] 까지만 증가하는 용도이다.
	     */
		
		int pageNo = ((currentShowPageNo - 1)/blockSize) * blockSize + 1;
	      // *** !! 공식이다. !! *** //
	      
	   /*
	       1  2  3  4  5  6  7  8  9  10  -- 첫번째 블럭의 페이지번호 시작값(pageNo)은 1 이다.
	       11 12 13 14 15 16 17 18 19 20  -- 두번째 블럭의 페이지번호 시작값(pageNo)은 11 이다.
	       21 22 23 24 25 26 27 28 29 30  -- 세번째 블럭의 페이지번호 시작값(pageNo)은 21 이다.
	       
	       currentShowPageNo         pageNo
	      ----------------------------------
	            1                      1 = ((1 - 1)/10) * 10 + 1
	            2                      1 = ((2 - 1)/10) * 10 + 1
	            3                      1 = ((3 - 1)/10) * 10 + 1
	            4                      1
	            5                      1
	            6                      1
	            7                      1 
	            8                      1
	            9                      1
	            10                     1 = ((10 - 1)/10) * 10 + 1
	           
	            11                    11 = ((11 - 1)/10) * 10 + 1
	            12                    11 = ((12 - 1)/10) * 10 + 1
	            13                    11 = ((13 - 1)/10) * 10 + 1
	            14                    11
	            15                    11
	            16                    11
	            17                    11
	            18                    11 
	            19                    11 
	            20                    11 = ((20 - 1)/10) * 10 + 1
	            
	            21                    21 = ((21 - 1)/10) * 10 + 1
	            22                    21 = ((22 - 1)/10) * 10 + 1
	            23                    21 = ((23 - 1)/10) * 10 + 1
	            ..                    ..
	            29                    21
	            30                    21 = ((30 - 1)/10) * 10 + 1
	   */
		
		
		String pageBar = "<ul style='list-style: none;'>";
		String url = "list.action";
		
		// === [맨처음][이전]만들기 === //
		if(pageNo != 1) {
			pageBar += "<li style='display:inline-block; width:70px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&currentShowPageNo=1'>[맨처음]</a></li>";
			pageBar += "<li style='display:inline-block; width:50px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&currentShowPageNo="+(pageNo-1)+"'>[이전]</a></li>";
		}
		
		while( !(loop > blockSize || pageNo > totalPage) ) {
			if(pageNo == currentShowPageNo) {
				pageBar += "<li style='display:inline-block; width:30px; font-size:12pt; border:solid 1px gray; color:red; padding:2px 4px;'>"+pageNo+"</a></li>";
			}
			else {
				pageBar += "<li style='display:inline-block; width:30px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&currentShowPageNo="+pageNo+"'>"+pageNo+"</a></li>";
			}
			
			loop++;
			pageNo++;
			
		}// end of while--------------------------------------------------
		
		
		// === [다음][맨마지막]만들기 === //
		if(pageNo <= totalPage) {
			pageBar += "<li style='display:inline-block; width:50px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&currentShowPageNo="+pageNo+"'>[다음]</a></li>";
			pageBar += "<li style='display:inline-block; width:70px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&currentShowPageNo="+totalPage+"'>[마지막]</a></li>";
		}
				
		pageBar += "</ul>";
		
		mav.addObject("pageBar", pageBar);
		
		
		// === #123. 페이징 처리되어진 후 특정 글제목을 클릭하여 상세내용을 본 이후
		//           사용자가 목록보기 버튼을 클릭했을때 돌아갈 페이지를 알려주기 위해
		//           현재 페이지 주소를 뷰단으로 넘겨준다.
		String gobackURL = MyUtil.getCurrentURL(request);
	//	System.out.println("~~~~~ 확인용 gobackURL : " + gobackURL);
		/*
			~~~~~ 확인용 gobackURL : /list.action
			~~~~~ 확인용 gobackURL : /list.action?searchType=&searchWord=&currentShowPageNo=2
			~~~~~ 확인용 gobackURL : /list.action?searchType=subject&searchWord=j
			~~~~~ 확인용 gobackURL : /list.action?searchType=subject&searchWord=j&currentShowPageNo=2
		 */		
		mav.addObject("gobackURL", gobackURL.replaceAll("&", " "));

		// == 페이징 처리를 한 검색어가 있는 전체 글목록 보여주기 끝 == //
		////////////////////////////////////////////////////////////////////
		
		
		
		mav.addObject("boardList", boardList);
		mav.setViewName("board/list.tiles1");
		// /WEB-INF/views/tiles1/board/list.jsp 파일을 생성한다.
		
		return mav;
	}
	
	
	// === #62. 글1개를 보여주는 페이지 요청 === //	
	@RequestMapping(value="/view.action")
	public ModelAndView view(ModelAndView mav, HttpServletRequest request) {
		
		getCurrentURL(request); // 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출
		
		// 조회하고자 하는 글번호 받아오기
		String seq = request.getParameter("seq");
		
		// 글목록에서 검색되어진 글내용일 경우 이전글제목, 다음글제목은 검색되어진 결과물내의 이전글과 다음글이 나오도록 하기 위한 것이다.
		String searchType = request.getParameter("searchType");
		String searchWord = request.getParameter("searchWord");
		
		if(searchType == null) {
			searchType = "";
		}

		if(searchWord == null) {
			searchWord = "";
		}
		
		// === #125. 페이징 처리되어진 후 특정 글제목을 클릭하여 상세내용을 본 이후
		//           사용자가 목록보기 버튼을 클릭했을때 돌아갈 페이지를 알려주기 위해
		//           현재 페이지 주소를 뷰단으로 넘겨준다.
		String gobackURL = request.getParameter("gobackURL");
	//	System.out.println("~~~ 확인용 : gobackURL" + gobackURL);
		// ~~~ 확인용 : gobackURL/list.action						전체글 1페이지		수정후 : ~~~ 확인용 : gobackURL/list.action
		// ~~~ 확인용 : gobackURL/list.action?searchType=			전체글 2페이지		수정후 : ~~~ 확인용 : gobackURL/list.action?searchType= searchWord= currentShowPageNo=2
		// ~~~ 확인용 : gobackURL/list.action?searchType=subject	검색조건j 1페이지	수정후 : ~~~ 확인용 : gobackURL/list.action?searchType=subject searchWord=j
		// ~~~ 확인용 : gobackURL/list.action?searchType=subject	검색조건j 2페이지	수정후 : ~~~ 확인용 : gobackURL/list.action?searchType=subject searchWord=j currentShowPageNo=2
		// getParameter의 구분자가 &여서 list에서 넘어온 주소가 잘린다. => 832줄
		
		if( gobackURL != null && gobackURL.contains(" ")) {
			gobackURL = gobackURL.replaceAll(" ", "&");
		}
	//	System.out.println("~~~ 확인용 : gobackURL" + gobackURL);
		// ~~~ 확인용 : gobackURL/list.action
		// ~~~ 확인용 : gobackURL/list.action?searchType=&searchWord=&currentShowPageNo=2
		// ~~~ 확인용 : gobackURL/list.action?searchType=subject&searchWord=j
		// ~~~ 확인용 : gobackURL/list.action?searchType=subject&searchWord=j&currentShowPageNo=2
		
		mav.addObject("gobackURL", gobackURL);
		
		// === #125 작업의 끝 === //
		//////////////////////////////////////////////////////////////////////////////////////////
		
		// 이전글제목, 다음글제목 클릭했을 때 readCountPermission 받아오기
		// String readCountPermission = request.getParameter("readCountPermission");
		
		try {
			Integer.parseInt(seq);
			
			Map<String, String> paraMap = new HashMap<>();
			paraMap.put("seq", seq);
			
			paraMap.put("searchType", searchType);
			paraMap.put("searchWord", searchWord);
			
			mav.addObject("paraMap", paraMap); // view.jsp 에서 이전글제목 및 다음글제목 클릭시 사용하기 위해서임.
			
			HttpSession session = request.getSession();
			MemberVO loginuser = (MemberVO) session.getAttribute("loginuser");
			
			String login_userid = null;
			if(loginuser != null) {
				login_userid = loginuser.getUserid();
				// login_userid 는 로그인 되어진 사용자의 userid 이다.
			}
			paraMap.put("login_userid", login_userid);
						
			// === #68. !!! 중요 !!! 
            //     글1개를 보여주는 페이지 요청은 select 와 함께 
			//     DML문(지금은 글조회수 증가인 update문)이 포함되어져 있다.
			//     이럴경우 웹브라우저에서 페이지 새로고침(F5)을 했을때 DML문이 실행되어
			//     매번 글조회수 증가가 발생한다.
			//     그래서 우리는 웹브라우저에서 페이지 새로고침(F5)을 했을때는
			//     단순히 select만 해주고 DML문(지금은 글조회수 증가인 update문)은 
			//     실행하지 않도록 해주어야 한다. !!! === //			
			
			// 위의 글목록보기 #69. 에서 session.setAttribute("readCountPermission", "yes"); 해두었다.
			BoardVO boardvo = null;
			if("yes".equals(session.getAttribute("readCountPermission")) ) {
				// 글목록보기를 클릭한 다음에 특정글을 조회해온 경우이다.
				
				boardvo = service.getView(paraMap);
				// 글조회수 증가와 함께 글1개를 조회를 해주는 것
				
				session.removeAttribute("readCountPermission");
				// 중요함!! session 에 저장된 readCountPermission 을 삭제한다.
			}
			else {
				// 웹브라우저에서 새로고침(F5)을 클릭한 경우이다.
				
				boardvo = service.getViewWithNoAddCount(paraMap);
				// 글조회수 증가는 없고 단순히 글1개 조회만을 해주는 것이다.
			}
			
			mav.addObject("boardvo", boardvo);
		} catch(NumberFormatException e) {
			
		}
		
		mav.setViewName("board/view.tiles1");
		
		return mav;
	}
	
	
	
	// === #71. 글 수정페이지 요청 === //	
	@RequestMapping(value="/edit.action")
	public ModelAndView requiredLogin_edit(HttpServletRequest request, HttpServletResponse response, ModelAndView mav) {
		
		// 글 수정해야 할 글번호 가져오기
		String seq = request.getParameter("seq");
		
		// 글 수정해야 할 글1개 내용 가져오기
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("seq", seq);
		
		BoardVO boardvo = service.getViewWithNoAddCount(paraMap); // 이미 만들어둔 글 하나만 가져오는 것
		// 글조회수(readCount) 증가 없이 단순히 글1개만 조회해주는 것이다.
		
		HttpSession session = request.getSession();
		MemberVO loginuser = (MemberVO)session.getAttribute("loginuser");
		
		if( !loginuser.getUserid().equals(boardvo.getFk_userid()) ) {
			String message = "다른 사용자의 글은 수정이 불가합니다.";
			String loc = "javascript:history.back()";
			
			mav.addObject("message", message);
			mav.addObject("loc", loc);
			mav.setViewName("msg");
		}
		else {
			// 자신의 글을 수정할 경우
			// 가져온 1개글을 글수정할 폼이 있는 view 단으로 보내준다.
			mav.addObject("boardvo", boardvo);
			mav.setViewName("board/edit.tiles1");
		}
		
		return mav;
	}
	
	
	
	// === #72. 글수정 페이지 완료하기 === //	
	@RequestMapping(value="/editEnd.action", method= {RequestMethod.POST})
	public ModelAndView editEnd(ModelAndView mav, BoardVO boardvo, HttpServletRequest request) {
		
		/*			
			글 수정을 하려면 원본글의 글암호와 수정시 입력해준 암호가 일치할때만
			글 수정이 가능하도록 해야 한다.
		 */
		int n = service.edit(boardvo);
		// n 이 1 이라면 정상적으로 변경됨.
		// n 이 0이라면 글수정에 필요한 글암호가 틀린경우임.
		
		if(n==0) {
			mav.addObject("message", "암호가 일치하지 않아  글 수정이 불가합니다.");
			mav.addObject("loc", "javascript:history.back()");
		}
		else {
			mav.addObject("message", "글 수정 성공!!");
			mav.addObject("loc", request.getContextPath()+"/view.action?seq="+boardvo.getSeq());
		}
		
		mav.setViewName("msg");
		
		return mav;
	}
	
	
	// === #76. 글삭제 페이지 요청 === //	
	@RequestMapping(value="/del.action")
	public ModelAndView requiredLogin_del(HttpServletRequest request, HttpServletResponse response, ModelAndView mav) {
		
		// 삭제해야 할 글번호 가져오기
		String seq = request.getParameter("seq");
		
		// 삭제해야할 글1새 내용 가져와서 로그인한 사람이 쓴 글이라면 글삭제가 가능하지만
		// 다른 사람이 쓴 글은 삭제가 불가하도록 해야 한다.
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("seq", seq);
				
		BoardVO boardvo = service.getViewWithNoAddCount(paraMap); // 이미 만들어둔 글 하나만 가져오는 것
		// 글조회수(readCount) 증가 없이 단순히 글1개만 조회해주는 것이다.
		
		HttpSession session = request.getSession();
		MemberVO loginuser = (MemberVO)session.getAttribute("loginuser");
		
		if( !loginuser.getUserid().equals(boardvo.getFk_userid()) ) {
			String message = "다른 사용자의 글은 삭제가 불가합니다.";
			String loc = "javascript:history.back()";
			
			mav.addObject("message", message);
			mav.addObject("loc", loc);
			mav.setViewName("msg");
		}
		else {
			// 자신의 글을 삭제할 경우
			// 글작성시 입력해준 글암호와 일치하는지 여부를 알아오도록 암호를 입력받아주는 del.jsp 페이지를 띄우도록 한다.
			mav.addObject("pw", boardvo.getPw());
			mav.addObject("seq", seq);
			mav.setViewName("board/del.tiles1");
		}
		
		return mav;
	}
	
	
	// === #77. 글삭제 페이지 완료하기 === //	
	@RequestMapping(value="/delEnd.action", method= {RequestMethod.POST})
	public ModelAndView delEnd(ModelAndView mav, HttpServletRequest request) {
		
		String seq = request.getParameter("seq");
		
		Map<String, String> paraMap = new HashMap<>(); // map은 확장성이 큰 장점이 있고, 유지보수가 수월하다.
		paraMap.put("seq", seq);
		
		int n = service.del(paraMap);
		
		if(n==1) {
			mav.addObject("message", "글 삭제 성공!!");
			mav.addObject("loc", request.getContextPath()+"/list.action");
		}
		else {
			mav.addObject("message", "글 삭제 실패!!");
			mav.addObject("loc", "javascript:history.back()");
		}
		
		mav.setViewName("msg");
		
		return mav;
		
	}
	
	
	@RequestMapping(value="/view_2.action")
	public ModelAndView view_2(ModelAndView mav, HttpServletRequest request) {
		
		getCurrentURL(request); // 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출
		
		// 조회하고자 하는 글번호 받아오기
		String seq = request.getParameter("seq");
		
		String searchType = request.getParameter("searchType");
		String searchWord = request.getParameter("searchWord");
		String gobackURL = request.getParameter("gobackURL");
		
		HttpSession session = request.getSession();
		session.setAttribute("readCountPermission", "yes");
		
		mav.setViewName("redirect:/view.action?seq="+seq+"&searchType="+searchType+"&searchWord="+searchWord+"&gobackURL="+gobackURL);
		
		return mav;
	}
	
	
	// === #84. 댓글쓰기(Ajax 로 처리) === //
	@ResponseBody
	@RequestMapping(value="/addComment.action", method= {RequestMethod.POST}, produces="text/plain;charset=UTF-8")
	public String addComment(CommentVO commentvo) {
		// 댓글쓰기에 첨부파일이 없는 경우
		
		int n = 0;

		try {
			n = service.addComment(commentvo);
		} catch (Throwable e) {
			e.printStackTrace();
		}		
		// 댓글쓰기(insert) 및 원게시물(tbl_board 테이블)에 댓글의 개수 증가(update 1씩 증가)하기 
		// 이어서 회원의 포인트를 50점을 증가하도록 한다. (tbl_member 테이블에 point 컬럼의 값을 50 증가하도록 update 한다.) 
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("n", n);
		jsonObj.put("name", commentvo.getName());
		
		return jsonObj.toString();	// "{"n":1,"name":"엄정화"}" 또는 "{"n":0, "name":"최병진"}"
	}
	
	
	// === #90. 원게시물에 딸리 댓글들을 조회해오기(Ajax 로 처리) === //
	@ResponseBody
	@RequestMapping(value="/readComment.action", method= {RequestMethod.GET}, produces="text/plain;charset=UTF-8")
	public String readComment(HttpServletRequest request) {
		
		String parentSeq = request.getParameter("parentSeq");
		
		List<CommentVO> commentList = service.getCommentList(parentSeq);
		
		JSONArray jsonArr = new JSONArray();	// []
		
		if( commentList != null ) {
			for(CommentVO cmtvo : commentList) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("name", cmtvo.getName());
				jsonObj.put("content", cmtvo.getContent());
				jsonObj.put("regdate", cmtvo.getRegDate());
				
				jsonArr.put(jsonObj);
			}// end of for-------------------------------------------
		}
			
		return jsonArr.toString();
	}
	
	// === #108. 검색어 입력시 자동글 완성하기 3 === //
	@ResponseBody
	@RequestMapping(value="/wordSearchShow.action", method= {RequestMethod.GET}, produces="text/plain;charset=UTF-8")
	public String wordSearchShow(HttpServletRequest request) {
		
		String searchType = request.getParameter("searchType");	// 컬럼명
		String searchWord = request.getParameter("searchWord");	// 검색어
		
		Map<String, String> paraMap = new HashMap<>();
		paraMap.put("searchType", searchType);
		paraMap.put("searchWord", searchWord);
		
		List<String> wordList = service.wordSearchShow(paraMap);
		
		JSONArray jsonArr = new JSONArray(); // []
		
		if(wordList != null) {
			for(String word : wordList) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("word", word);
				
				jsonArr.put(jsonObj);				
			}// end of for ---------------------------------------
		}
		
		return jsonArr.toString();
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	
	// === 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 생성 == //
	public void getCurrentURL(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.setAttribute("goBackURL", MyUtil.getCurrentURL(request));
	}	
	
	////////////////////////////////////////////////////////////////////////////////////
	
	// ================== ***** 게시판 끝 ***** ========================= //
}
