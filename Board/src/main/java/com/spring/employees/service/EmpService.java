package com.spring.employees.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.employees.model.InterEmpDAO;

@Service
public class EmpService implements InterEmpService {

	@Autowired
	private InterEmpDAO dao;

	
	// employees 테이블에서 근무중인 사원들의 부서번호 가져오기
	@Override
	public List<String> deptIdList() {
		List<String> deptIdList = dao.deptIdList();
		return deptIdList;
	}

	
	// employees 테이블에서 조건에 만족하는 사원들을 가져오기
	@Override
	public List<Map<String, String>> empList(Map<String, Object> paraMap) {
		List<Map<String, String>> empList = dao.empList(paraMap);
		return empList;
	}
	

	// employees 테이블에서 부서명별 인원수 및 퍼센티지 가져오기
	@Override
	public List<Map<String, String>> employeeCntByDeptname() {
		List<Map<String, String>> deptnamePercentageList = dao.employeeCntByDeptname();
		return deptnamePercentageList;
	}


	// employees 테이블에서 성별 인원수 및 퍼센티지 가져오기
    @Override
    public List<Map<String, String>> employeeCntByGender() {
       List<Map<String, String>> genderPercentageList = dao.employeeCntByGender(); 
       return genderPercentageList;
    }

    // 특정 부서명에 근무하는 직원들의 성별 인원수 및 퍼센티지 가져오기 
    @Override
    public List<Map<String, String>> genderCntSpecialDeptname(Map<String, String> paraMap) { 
       List<Map<String, String>> genderPercentageList = dao.genderCntSpecialDeptname(paraMap); 
       return genderPercentageList;
    }


    // 인사관리 페이지에 접속한 페이지URL, 사용자ID, 접속IP주소, 접속시간을 기록으로 DB에 insert 하도록 한다.
	@Override
	public void insert_accessTime(Map<String, String> paraMap) {
		dao.insert_accessTime(paraMap);
	}
	
}
