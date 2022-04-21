package com.spring.employees.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.spring.board.common.MyUtil;

@Controller
public class EmpController {

	@RequestMapping(value="/emp/empList.action")
	public String requiredLogin_employeeInfoView(HttpServletRequest request, HttpServletResponse response) {
		
	//	getCurrentURL(request); // 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출
		
		return "emp/empList.tiles2";
		//	/WEB-INF/views/tiles2/emp/empList.jsp 파일을 생성한다.
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
		
	// === 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 생성 == //
	public void getCurrentURL(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.setAttribute("goBackURL", MyUtil.getCurrentURL(request));
	}	
	
	////////////////////////////////////////////////////////////////////////////////////
}
