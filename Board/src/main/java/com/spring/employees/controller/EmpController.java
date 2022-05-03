package com.spring.employees.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.spring.board.common.MyUtil;
import com.spring.employees.service.InterEmpService;

@Controller
public class EmpController {

	@Autowired
	private InterEmpService service;
	
	// === #175. 다중 체크박스를 사용시 sql문에서 in 절을 처리하는 예제 === //
	@RequestMapping(value="/emp/empList.action")
	public String requiredLogin_employeeInfoView(HttpServletRequest request, HttpServletResponse response) {
		
	//	getCurrentURL(request); // 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 호출
		
		// employees 테이블에서 근무중인 사원들의 부서번호 가져오기
		List<String> deptIdList = service.deptIdList();
		
		
		
		
		Map<String, String> paraMap = new HashMap<>();
		
		List<Map<String, String>> empList = service.empList(paraMap);		
		
		request.setAttribute("deptIdList", deptIdList);
		request.setAttribute("empList", empList);
		
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
