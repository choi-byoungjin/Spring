<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<style type="text/css">

	span.move {cursor: pointer; color: navy;}
	.moveColor {color: #660029; font-weight: bold; background-color: #ffffe6;}

</style>

<script type="text/javascript">

	$(document).ready(function(){
		
		$("span.move").hover(function(){
								$(this).addClass("moveColor"); // this는 화살표함수 불가능 function 필요
							}, 
							function(){
								$(this).removeClass("moveColor");	
							});
		
	});// end of $(document).ready(function(){})----------------------------------------------------

	// Function Declaration
	
	// == 댓글쓰기 ==
	function goAddWrite() {
		
		const commentContent = $("input#commentContent").val().trim();
		if(commentContent == "") {
			alert("댓글 내용을 입력하세요!!");
			return; // 종료
		}
		
		goAddWrite_noAttach();
		
	}// end of function goAddWrite() {}------------------------------------------------------------
	
	// 파일첨부가 없는 댓글쓰기
	function goAddWrite_noAttach() {
		
		<%--
			// 보내야할 데이터를 선정하는 두번째 방법
			// jQuery에서 사용하는 것으로써,
			// form태그의 선택자.serialize(); 을 해주면 form 태그내의 모든 값들을 name값을 키값으로 만들어서 보내준다. 
			   const queryString = $("form[name=addWriteFrm]").serialize(); 
		--%>
		
		$.ajax({
			url:"<%=request.getContextPath()%>/addComment.action",
			data:{"fk_userid":$("input#fk_userid")
				, "name":$("input#name")
				, "content":$("input#commentContent")
				, "parentSeq":$("input#parentSeq")},
	   /* 	또는 
	   		data:queryString, */
			type:"POST",
			dataType:"JSON",
			success:function(json){
				
			},
			error: function(request, status, error){
				alert("code: "+request.status+"\n"+"message: "+request.responseText+"\n"+"error: "+error);
			}
		});
		
	}// end of function goAddWrite_noAttach() {}-----------------------------------------------------
	
</script>

<div style="display: flex;">
<div style="margin: auto; padding-left: 3%;">

	<h2 style="margin-bottom: 30px;">글내용보기</h2>

	<c:if test="${not empty requestScope.boardvo}">
		<table style="width: 1024px" class="table table-bordered table-dark">
			<tr>
				<th style="width: 15%">글번호</th>
				<td>${requestScope.boardvo.seq}</td>
			</tr>
			<tr>
				<th>성명</th>
				<td>${requestScope.boardvo.name}</td>
			</tr>
			<tr>
				<th>제목</th>
				<td>${requestScope.boardvo.subject}</td>
			</tr>
			<tr>
				<th>내용</th>
				<td>
					<p style="word-break: break-all;">${requestScope.boardvo.content}</p>
					<%-- 
						style="word-break: break-all; 은 공백없는 긴영문일 경우 width 크기를 뚫고 나오는 것을 막는 것임. 
						그런데 style="word-break: break-all; 나 style="word-wrap: break-word; 은
						테이블태그의 <td>태그에는 안되고 <p> 나 <div> 태그안에서 적용되어지므로 <td>태그에서 적용하려면
						<table>태그속에 style="word-wrap: break-word; table-layout: fixed;" 을 주면 된다.
					--%>
				</td>
			</tr>
			<tr>
				<th>조회수</th>
				<td>${requestScope.boardvo.readCount}</td>
			</tr>
			<tr>
				<th>작성일자</th>
				<td>${requestScope.boardvo.regDate}</td>
			</tr>
		</table>
		<br/>
		
		<div style="margin-bottom: 1%;">이전글제목&nbsp;&nbsp;<span class="move" onclick="javascript:location.href='view_2.action?seq=${requestScope.boardvo.previousseq}&readCountPermission=yes'">${requestScope.boardvo.previoussubject}</span></div>
		<div style="margin-bottom: 1%;">다음글제목&nbsp;&nbsp;<span class="move" onclick="javascript:location.href='view_2.action?seq=${requestScope.boardvo.nextseq}&readCountPermission=yes'">${requestScope.boardvo.nextsubject}</span></div>
		<br/>
		
		<button type="button" class="btn btn-secondary btn-sm mr-3" onclick="javascript:location.href='<%= request.getContextPath() %>/list.action'">전체목록보기</button>
		
		<button type="button" class="btn btn-secondary btn-sm mr-3" onclick="javascript:location.href='<%= request.getContextPath() %>/edit.action?seq=${requestScope.boardvo.seq}'">글수정하기</button>
		<button type="button" class="btn btn-secondary btn-sm mr-3" onclick="javascript:location.href='<%= request.getContextPath() %>/del.action?seq=${requestScope.boardvo.seq}'">글삭제하기</button>
		
		<%-- === #83. 댓글쓰기 폼 추가 === --%>
		<c:if test="${not empty sessionScope.loginuser}">
			<h3 style="margin-top: 50px;">댓글쓰기</h3>
		
			<form name="addWriteFrm" id="addWriteFrm" style="margin-top: 20px;">
				<table class="table" style="width: 1024px">
					<tr style="height: 30px;">
						<th width="10%" >성명</th>
						<td>
	                  		<input type="hidden" name="fk_userid" id="fk_userid" value="${sessionScope.loginuser.userid}" />
	                  		<input type="text" name="name" id="name" value="${sessionScope.loginuser.name}" readonly />
						</td>
					</tr>
					<tr style="height: 30px;">
						<th width="10%">댓글내용</th>
						<td>
							<input type="text" name="content" id="commentContent" size="100" />
							
							<%-- 댓글에 달리는 원게시물  글번호(즉, 댓글의 부모글 글번호) --%>
							<input type="hidden" name="parentSeq" id="parentSeq" value="${requestScope.boardvo.seq}"/>
						</td>
					</tr>
					<tr>
						<th colspan="2">
							<button type="button" class="btn btn-success btn-sm mr-3" onclick="goAddWrite()">댓글쓰기 확인</button>
							<button type="reset" class="btn btn-success btn-sm">댓글쓰기 취소</button>
						</th>
					</tr>										
				</table>
			</form>
		</c:if>
		
	</c:if>

	<c:if test="${empty requestScope.boardvo}">
		<div style="padding: 50px 0; font-size: 16pt; color: red;">존재하지 않습니다</div>
	</c:if>
		
</div>
</div>