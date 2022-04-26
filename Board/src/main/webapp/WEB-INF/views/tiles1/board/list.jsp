<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
	String ctxPath  = request.getContextPath();
%>

<style type="text/css">

	th {background-color: #DDD}
	
	.subjectStyle {font-weight:bold;
					color: navy;
					cursor: pointer;}

</style>

<script type="text/javascript">

	$(document).ready(function(){
		
		$("span.subject").bind("mouseover", function(event){
			const $target = $(event.target);
			$target.addClass("subjectStyle");
		});
		
		$("span.subject").bind("mouseout", function(event){
			const $target = $(event.target);
			$target.removeClass("subjectStyle");
		});
		
		$("input#searchWord").keyup(function(event){
			if(event.keyCode == 13) {
				// 엔터를 했을 경우
				goSearch();
			}
		});
		
		// 검색시 검색조건 및 검색어 값 유지시키기
		if( ${not empty requestScope.paraMap} ) {
			$("select#searchType").val("${requestScope.paraMap.searchType}");
			$("input#searchWord").val("${requestScope.paraMap.searchWord}");
		}
		
		<%-- === #107. 검색어 입력시 자동글 완성하기 2 === --%>
		$("div#displayList").hide();
		
		$("input#searchWord").keyup(function(){
			
			const wordLength = $(this).val().trim().length;
			// 검색어의 길이를 알아온다.
			
			if(wordLength == 0) {
				$("div#displayList").hide();
				// 검색어가 공백이거나 검색어 입력후 백스페이스키를 눌러서 검색어를 모두 지우면 검색된 내용이 안 나오도록 해야 한다.
			}
			else {
				$.ajax({
					url:"<%=ctxPath%>/wordSearchShow.action",
					type:"GET",
					data:{"searchType":$("select#searchType").val()
						, "searchWord":$("input#searchWord").val()},
					dataType:"JSON",
					success:function(json){
					//	json ==> [{"word":"엄정화의 글쓰기 두번째 연습"},{"word":"엄정화의 첫번째 글쓰기 연습"},{"word":"최병진의 글쓰기 첫번째 연습"}] // '연습'으로 검색시
					
						<%-- === #112. 검색어 입력시 자동글 완성하기 7 === --%>						
						if(json.length > 0) {
							// 검색된 데이터가 있는 경우임
							
							let html = "";
							
							$.each(json, function(index, item){								
								const word = item.word;
								// word ==> 프로그램은 JAVA 가 쉬운가요?
								
								const idx = word.toLowerCase().indexOf($("input#searchWord").val().toLowerCase());
								// 			word ==> 프로그램은 java 가 쉬운가요?
								// 검색어(JaVa)가 나오는 idx 는 6이 된다.
								
								const len = $("input#searchWord").val().length;
								// 검색어(JaVa)의 길이 len 은 4 가 된다.
							/*	
								console.log("~~~~~~~~ 시작 ~~~~~~~~")
								console.log(word.substr(0, idx)); 	// 검색어(JaVa) 앞까지의 글자 => "프로그램은 "
								console.log(word.substr(idx, len)); // 검색어(JaVa) 글자 => "JAVA"
								console.log(word.substr(idx+len)); 	// 검색어(JaVa) 뒤부터 끝까지 글자 => " 가 쉬운가요?" 
								console.log("~~~~~~~~ 끝 ~~~~~~~~")
							*/
							/*
								console.log(word.substring(0, idx)); 		// 검색어(JaVa) 앞까지의 글자 => "프로그램은 "
								console.log(word.substring(idx, idx+len)); 	// 검색어(JaVa) 글자 => "JAVA"
								console.log(word.substring(idx+len)); 		// 검색어(JaVa) 뒤부터 끝까지 글자 => " 가 쉬운가요?"
							*/

							//	word.substring(), word.substr()은 다르다
							
								const result = word.substring(0, idx) + "<span style='color:blue'>"+word.substring(idx, idx+len)+"</span>" + word.substr(idx+len);

								html += "<span style='cursor:pointer;' class='result'>"+result+"</span><br>";								
							});
							
							const input_width = $("input#searchWord").css("width"); // 검색어 input 태그 width 알아오기
							
							$("div#displayList").css({"width":input_width});// 검색결과 div 의 width 크기를 검색어 입력 input 태그 width 와 일치시키기								
							
							$("div#displayList").html(html);
							$("div#displayList").show();
							
						}
					},
					error: function(request, status, error){
						alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
					}
				});
			}
			
		});// end of $("input#searchWord").keyup(function(){})-----------------------------------------
		
		<%-- === #113. 검색어 입력시 자동글 완성하기 8 === --%>
		$(document).on("click", "span.result", function(){
			const word = $(this).text(); <%-- input태그는 .val(), span태그는 .text() --%>
			$("input#searchWord").val(word);	// 텍스트박스에 검색된 결과의 문자열을 입력해준다.
			$("div#displayList").hide();
			goSearch();
		});
		
		
		
	});// end of $(document).ready(function(){})---------------------------------------

	// Function Declaration
	function goView(seq) {
		
		location.href = "<%=ctxPath%>/view.action?seq="+seq;
		
	}// end of function goView(seq)---------------------------------------------
	
	function goSearch() {
		const frm = document.searchFrm;
		frm.method = "GET";
		frm.action = "<%=ctxPath%>/list.action";
		frm.submit();
	}// end of function goSearch() {}----------------------------------------
</script>

<div style="display: flex;">
<div style="margin: auto; padding-left: 3%;">

	<h2 style="margin-bottom: 30px;">글목록</h2>
	
	<table style="width: 1024px" class="table table-bordered">	
		<thead>
			<tr>
				<th style="width: 70px;  text-align: center;">글번호</th>
		        <th style="width: 360px; text-align: center;">제목</th>
		        <th style="width: 70px;  text-align: center;">성명</th>
		        <th style="width: 150px; text-align: center;">날짜</th>
		        <th style="width: 70px;  text-align: center;">조회수</th>
			</tr>
		</thead>
		
		<tbody>
			<c:forEach var="boardvo" items="${requestScope.boardList}" varStatus="status">
				<tr>
					<td align="center">
						${boardvo.seq}
					</td>
					<td align="left">
						<c:if test="${boardvo.commentCount > 0}">
							<span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject} <span style="vertical-align: super;">[<span style="color: red; font-size: 9pt; font-style: italic; font-weight: bold;">${boardvo.commentCount}</span>]</span></span>
						</c:if>
						
						<c:if test="${boardvo.commentCount == 0}">
							<span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject}</span>
						</c:if>
					</td>
					<td align="center">${boardvo.name}</td>
					<td align="center">${boardvo.regDate}</td>
					<td align="center">${boardvo.readCount}</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	
	<%-- === #101. 글검색 폼 추가하기 : 글제목, 글쓴이로 검색을 하도록 한다. === --%>
	<form name="searchFrm" style="margin-top: 20px;">
		<select name="searchType" id="searchType" style="height: 26px;">
			<option value="subject">글제목</option>
			<option value="name">글쓴이</option>
		</select>
		<input type="text" name="searchWord" id="searchWord" size="40" autocomplete="off" /> 
		<input type="text" style="display: none;"/> <%-- form 태그내에 input 태그가 오로지 1개 뿐일경우에는 엔터를 했을 경우 검색이 되어지므로 이것을 방지하고자 만든것이다. type hidden 하지 않고 style none 해야한다.--%>  
		<button type="button" class="btn btn-secondary btn-sm" onclick="goSearch()">검색</button>
	</form>
   
	<%-- === #106. 검색어 입력시 자동글 완성하기 1 === --%>
	<div id="displayList" style="border:solid 1px gray; border-top:0px; height:100px; margin-left:75px; margin-top:-1px; overflow:auto;">
		
	</div>
   
</div>
</div>