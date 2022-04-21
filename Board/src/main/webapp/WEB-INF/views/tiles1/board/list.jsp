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
	
	});// end of $(document).ready(function(){})---------------------------------------

	// Function Declaration
	function goView(seq) {
		
		location.href = "<%=ctxPath%>/view.action?seq="+seq;
		
	}// end of function goView(seq)---------------------------------------------
	
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
					<td>
						<span class="subject" onclick="goView('${boardvo.seq}')">${boardvo.subject}</span>
					</td>
					<td align="center">
						${boardvo.name}
					</td>
					<td align="center">
						${boardvo.regDate}
					</td>
					<td align="center">
						${boardvo.readCount}
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>

</div>
</div>