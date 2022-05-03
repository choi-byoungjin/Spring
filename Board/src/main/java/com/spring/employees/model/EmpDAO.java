package com.spring.employees.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmpDAO implements InterEmpDAO {

	@Resource
	private SqlSessionTemplate sqlsession_2;

	
	// employees 테이블에서 근무중인 사원들의 부서번호 가져오기
	@Override
	public List<String> deptIdList() {
		List<String> deptIdList = sqlsession_2.selectList("hr.deptIdList");
		return deptIdList;
	}


	// employees 테이블에서 조건에 만족하는 사원들을 가져오기
	@Override
	public List<Map<String, String>> empList(Map<String, String> paraMap) {
		List<Map<String, String>> empList = sqlsession_2.selectList("hr.empList", paraMap);
		return empList;
	}
	
}
