package com.spring.employees.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
		
		String str_DeptId = request.getParameter("str_DeptId");
	//	System.out.println("~~~ 확인용 str_DeptId => " + str_DeptId);
		/*
			~~~ 확인용 str_DeptId => null
			~~~ 확인용 str_DeptId => 20,100,110
			~~~ 확인용 str_DeptId => 80,110
		 */
		
		String gender = request.getParameter("gender");
	//	System.out.println("~~~ 확인용 gender => " + gender);
		/*
			~~~ 확인용 gender => null
			~~~ 확인용 gender => 
			~~~ 확인용 gender => 남
		 */
		
		Map<String, Object> paraMap = new HashMap<>();
		
		if(str_DeptId != null && !"".equals(str_DeptId)) {
			String[] arr_DeptId = str_DeptId.split("\\,"); // 문자열을 ,로 나눠서 배열에 넣는다.
			paraMap.put("arr_DeptId", arr_DeptId);
			
			request.setAttribute("str_DeptId", str_DeptId);
			// 뷰단에서 체크되어진 값을 유지시키기 위한 것이다.
		}
		
		if(gender != null && !"".equals(gender)) {
			paraMap.put("gender", gender);
			
			request.setAttribute("gender", gender);
			// 뷰단에서 선택한 성별을 유지시키기 위한 것이다.
		}
		
		
		List<Map<String, String>> empList = service.empList(paraMap);		
		
		request.setAttribute("deptIdList", deptIdList);
		request.setAttribute("empList", empList);
		
		return "emp/empList.tiles2";
		//	/WEB-INF/views/tiles2/emp/empList.jsp 파일을 생성한다.
	}
	
	// ==== #176. Excel 파일로 다운받기 예제 ==== //
	// /excel/downloadExcelFile.action
	@RequestMapping(value="/excel/downloadExcelFile.action", method= {RequestMethod.POST}) 
	public String downloadExcelFile(HttpServletRequest request, Model model) { // 모델은 오로지 저장소 역할만 한다.
		
		String str_DeptId = request.getParameter("str_DeptId");
	//	System.out.println("~~~ 확인용 str_DeptId => " + str_DeptId);
		/*
			~~~ 확인용 str_DeptId => null
			~~~ 확인용 str_DeptId => 20,100,110
			~~~ 확인용 str_DeptId => 80,110
		 */
		
		String gender = request.getParameter("gender");
	//	System.out.println("~~~ 확인용 gender => " + gender);
		/*
			~~~ 확인용 gender => null
			~~~ 확인용 gender => 
			~~~ 확인용 gender => 남
		 */
		
		Map<String, Object> paraMap = new HashMap<>();
		
		if(str_DeptId != null && !"".equals(str_DeptId)) {
			String[] arr_DeptId = str_DeptId.split("\\,"); // 문자열을 ,로 나눠서 배열에 넣는다.
			paraMap.put("arr_DeptId", arr_DeptId);
		}
		
		if(gender != null && !"".equals(gender)) {
			paraMap.put("gender", gender);			
		}
		
		
		List<Map<String, String>> empList = service.empList(paraMap);		
		
		// === 조회결과물인 empList 를 가지고 엑셀 시트 생성하기 ===
		// 시트를 생성하고, 행을 생성하고, 셀을 생성하고, 셀안에 내용을 넣어주면 된다.
				
		SXSSFWorkbook workbook = new SXSSFWorkbook();		
		
		// 시트생성
		SXSSFSheet sheet = workbook.createSheet("HR사원정보");
		
		// 시트 열 너비 설정
		sheet.setColumnWidth(0, 2000);
		sheet.setColumnWidth(1, 4000);
		sheet.setColumnWidth(2, 2000);
		sheet.setColumnWidth(3, 4000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 2000);
		sheet.setColumnWidth(6, 1500);
		sheet.setColumnWidth(7, 1500);
		
		// 행의 위치를 나타내는 변수
		int rowLocation = 0;
		
		
		////////////////////////////////////////////////////////////////////////////////////////
		// CellStyle 정렬하기(Alignment)
		// CellStyle 객체를 생성하여 Alignment 세팅하는 메소드를 호출해서 인자값을 넣어준다.
		// 아래는 HorizontalAlignment(가로)와 VerticalAlignment(세로)를 모두 가운데 정렬 시켰다.
		CellStyle mergeRowStyle = workbook.createCellStyle();
		mergeRowStyle.setAlignment(HorizontalAlignment.CENTER);
		mergeRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
										// import org.apache.poi.ss.usermodel.VerticalAlignment 으로 해야함.
		
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		
		
		// CellStyle 배경색(ForegroundColor)만들기
        // setFillForegroundColor 메소드에 IndexedColors Enum인자를 사용한다.
        // setFillPattern은 해당 색을 어떤 패턴으로 입힐지를 정한다.
		mergeRowStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex()); // IndexedColors.DARK_BLUE.getIndex() 는 색상(남색)의 인덱스값을 리턴시켜준다.
		mergeRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex()); // IndexedColors.LIGHT_YELLOW.getIndex() 는 연한노랑의 인덱스값을 리턴시켜준다.
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		// CellStyle 천단위 쉼표, 금액
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
		
		// Cell 폰트(Font) 설정하기
        // 폰트 적용을 위해 POI 라이브러리의 Font 객체를 생성해준다.
        // 해당 객체의 세터를 사용해 폰트를 설정해준다. 대표적으로 글씨체, 크기, 색상, 굵기만 설정한다.
        // 이후 CellStyle의 setFont 메소드를 사용해 인자로 폰트를 넣어준다.
		Font mergeRowFont = workbook.createFont(); // import org.apache.poi.ss.usermodel.Font; 으로 한다.
		mergeRowFont.setFontName("나눔고딕");
		mergeRowFont.setFontHeight((short)500);
		mergeRowFont.setColor(IndexedColors.WHITE.getIndex());
		mergeRowFont.setBold(true);
		
		mergeRowStyle.setFont(mergeRowFont);
		
		// CellStyle 테두리 Border
        // 테두리는 각 셀마다 상하좌우 모두 설정해준다.
        // setBorderTop, Bottom, Left, Right 메소드와 인자로 POI라이브러리의 BorderStyle 인자를 넣어서 적용한다.
		headerStyle.setBorderTop(BorderStyle.THICK);
		headerStyle.setBorderBottom(BorderStyle.THICK);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		
		// Cell Merge 셀 병합시키기
        /* 셀병합은 시트의 addMergeRegion 메소드에 CellRangeAddress 객체를 인자로 하여 병합시킨다.
           CellRangeAddress 생성자의 인자로(시작 행, 끝 행, 시작 열, 끝 열) 순서대로 넣어서 병합시킬 범위를 정한다. 배열처럼 시작은 0부터이다.  
        */
        // 병합할 행 만들기
		Row mergeRow = sheet.createRow(rowLocation);	// 엑셀에서 행의 시작은 0부터 시작한다.
		
		// 병합할 행에 "우리회사 사원정보" 로 셀을 만들어 셀에 스타일을 주기
		for(int i=0; i<8; i++) {
			Cell cell = mergeRow.createCell(i);
			cell.setCellStyle(mergeRowStyle);
			cell.setCellValue("우리회사 사원정보");
		}// end of for-------------------------------
		
		// 셀 병합하기		
		sheet.addMergedRegion(new CellRangeAddress(rowLocation, rowLocation, 0, 7)); // 시작 행, 끝 행, 시작 열, 끝 열 
        ////////////////////////////////////////////////////////////////////////////////////////////////
		
		// 헤더 행 생성
        Row headerRow = sheet.createRow(++rowLocation); // 엑셀에서 행의 시작은 0 부터 시작한다.
                                                        // ++rowLocation는 전위연산자임. 
        
        // 해당 행의 첫번재 열 셀 생성
        Cell headerCell = headerRow.createCell(0); // 엑셀에서 열의 시작은 0 부터 시작한다.
        headerCell.setCellValue("부서번호");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 두번재 열 셀 생성
        headerCell = headerRow.createCell(1); // 엑셀에서 열의 시작은 0 부터 시작한다.
        headerCell.setCellValue("부서명");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 세번재 열 셀 생성
        headerCell = headerRow.createCell(2); // 엑셀에서 열의 시작은 0 부터 시작한다.
        headerCell.setCellValue("사원번호");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 네번재 열 셀 생성
        headerCell = headerRow.createCell(3); // 엑셀에서 열의 시작은 0 부터 시작한다.
        headerCell.setCellValue("사원명");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 다섯번재 열 셀 생성
        headerCell = headerRow.createCell(4); // 엑셀에서 열의 시작은 0 부터 시작한다.
        headerCell.setCellValue("입사일자");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 여섯번재 열 셀 생성
        headerCell = headerRow.createCell(5); // 엑셀에서 열의 시작은 0 부터 시작한다.
        headerCell.setCellValue("월급");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 일곱번재 열 셀 생성
        headerCell = headerRow.createCell(6); // 엑셀에서 열의 시작은 0 부터 시작한다.
        headerCell.setCellValue("사원번호");
        headerCell.setCellStyle(headerStyle);
        
        // 해당 행의 여덟번재 열 셀 생성
        headerCell = headerRow.createCell(7); // 엑셀에서 열의 시작은 0 부터 시작한다.
        headerCell.setCellValue("나이");
        headerCell.setCellStyle(headerStyle);
        
        // ==== HR사원정보 내용에 해당하는 행 및 셀 생성하기 ==== //
        Row bodyRow = null;
        Cell bodyCell = null;
        
        for(int i=0; i<empList.size(); i++) {
        	
        	Map<String, String> empMap = empList.get(i);
        	
        	// 행생성
        	bodyRow = sheet.createRow(i + (rowLocation+1));
        	 
        	// 데이터 부서번호 표시
        	bodyCell = bodyRow.createCell(0);
        	bodyCell.setCellValue(empMap.get("department_id"));
        	 
        	// 데이터 부서명 표시
        	bodyCell = bodyRow.createCell(1);
        	bodyCell.setCellValue(empMap.get("department_name"));
        	
        	// 데이터 사원번호 표시
        	bodyCell = bodyRow.createCell(2);
        	bodyCell.setCellValue(empMap.get("employee_id"));
        	
        	// 데이터 사원명 표시
        	bodyCell = bodyRow.createCell(3);
        	bodyCell.setCellValue(empMap.get("fullname"));
        	
        	// 데이터 입사일자 표시
        	bodyCell = bodyRow.createCell(4);
        	bodyCell.setCellValue(empMap.get("hire_date"));
        	
        	// 데이터 월급 표시
        	bodyCell = bodyRow.createCell(5);
        	bodyCell.setCellValue(Integer.parseInt(empMap.get("monthsal")));
        	bodyCell.setCellStyle(moneyStyle); // 천단위 쉼표, 금액
        	
        	// 데이터 사원번호 표시
        	bodyCell = bodyRow.createCell(6);
        	bodyCell.setCellValue(empMap.get("gender"));
        	
        	// 데이터 나이 표시
        	bodyCell = bodyRow.createCell(7);
        	bodyCell.setCellValue(Integer.parseInt(empMap.get("age")));
        	
        }// end of for--------------------------------------
        
        model.addAttribute("locale", Locale.KOREA);
        model.addAttribute("workbook", workbook);
        model.addAttribute("workbookName", "HR사원정보");
        
		return "excelDownloadView"; // 접두어 접미어가 아님 // 우선순위 주의
		// "excelDownloadView" 은 
		//  /webapp/WEB-INF/spring/appServlet/servlet-context.xml 파일에서
		//  뷰리졸버 0 순위로 기술된 bean 의 id 값이다.  
	}
	
	
	// === #177. 차트(그래프)를 보여주는 예제(view단) === //
	@RequestMapping(value="/emp/chart.action")
	public String empmanager_chart() {	
		return "emp/chart.tiles2";
	}
	
	// === #178. 차트그리기(Ajax) 부서명별 인원통계 및 퍼센티지 가져오기 === //
	@ResponseBody
	@RequestMapping(value="/chart/employeeCntByDeptname.action", produces="text/plain;charset=UTF-8")
	public String employeeCntByDeptname() {
		
		List<Map<String, String>> deptnamePercentageList = service.employeeCntByDeptname();
		
		Gson gson = new Gson();
		JsonArray jsonArr = new JsonArray();
		
		for(Map<String, String> map : deptnamePercentageList) {
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("department_name", map.get("department_name"));
			jsonObj.addProperty("cnt", map.get("cnt"));
			jsonObj.addProperty("percentage", map.get("percentage"));
			
			jsonArr.add(jsonObj);
		}// end of for -------------------------------------------
		
		return new Gson().toJson(jsonArr);
	}
	
	
	// === #179. 차트그리기(Ajax) 성별 인원수 및 퍼센티지 가져오기 === //
   @ResponseBody
   @RequestMapping(value="/chart/employeeCntByGender.action", produces="text/plain;charset=UTF-8")
   public String employeeCntByGender() {
      
      List<Map<String, String>> genderPercentageList = service.employeeCntByGender();
      
      JsonArray jsonArr = new JsonArray();
      
      for(Map<String, String> map : genderPercentageList) {
         JsonObject jsonObj = new JsonObject();
         jsonObj.addProperty("gender", map.get("gender"));
         jsonObj.addProperty("cnt", map.get("cnt"));
         jsonObj.addProperty("percentage", map.get("percentage"));
         
         jsonArr.add(jsonObj);
      }// end of for----------------------------------------
      
      return new Gson().toJson(jsonArr);
   }
	
	////////////////////////////////////////////////////////////////////////////////////
		
	// === 로그인 또는 로그아웃을 했을 때 현재 보이던 그 페이지로 그대로 돌아가기 위한 메소드 생성 == //
	public void getCurrentURL(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.setAttribute("goBackURL", MyUtil.getCurrentURL(request));
	}	
	
	////////////////////////////////////////////////////////////////////////////////////
}
