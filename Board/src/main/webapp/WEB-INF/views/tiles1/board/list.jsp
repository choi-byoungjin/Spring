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
</div>
</div>