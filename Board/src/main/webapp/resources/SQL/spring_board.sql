------ ***** spring 기초 ***** ------

show user;
-- USER이(가) "MYMVC_USER"입니다.

create table spring_test
(no         number
,name       varchar2(100)
,writeday   date default sysdate
);

select *
from spring_test;


select no, name, to_char(writeday, 'yyyy-mm-dd hh24:mi:ss') AS writeday  
from spring_test
order by writeday desc;

-----------------------------------------------------------------------

select * from tab;

select * 
from TBL_MAIN_IMAGE;

select imgfilename 
from TBL_MAIN_IMAGE
order by imgno desc;


select * 
from tbl_member;

desc tbl_member;

SELECT userid, name, email, mobile, postcode, address, detailaddress, extraaddress, gender  
     , birthyyyy, birthmm, birthdd, coin, point, registerday, pwdchangegap  
     , nvl(lastlogingap, trunc( months_between(sysdate, registerday) ) ) AS lastlogingap 
FROM 
 ( 
  select userid, name, email, mobile, postcode, address, detailaddress, extraaddress, gender 
        , substr(birthday,1,4) AS birthyyyy, substr(birthday,6,2) AS birthmm, substr(birthday,9) AS birthdd  
        , coin, point, to_char(registerday, 'yyyy-mm-dd') AS registerday 
        , trunc( months_between(sysdate, lastpwdchangedate) ) AS pwdchangegap 
  from tbl_member 
  where status = 1 and userid = 'seoyh' and pwd = '9695b88a59a1610320897fa84cb7e144cc51f2984520efb77111d94b402a8382' 
 ) M 
CROSS JOIN 
(
 select trunc( months_between(sysdate, max(logindate)) ) AS lastlogingap
 from tbl_loginhistory
 where fk_userid = 'seoyh'
) H;


select *
from tbl_loginhistory
order by logindate desc;

select *
from tbl_loginhistory
where fk_userid = 'choibj'
order by logindate desc;

update tbl_loginhistory set logindate = add_months(logindate, 13)
where fk_userid = 'choibj';

commit;

select *
from tbl_member
where userid = 'choibj';

update tbl_member set idle = 0
where userid = 'choibj';

commit

update tbl_member set lastpwdchangedate = add_months(lastpwdchangedate, -4)
where userid = 'choibj';

commit


    ------- **** spring 게시판(답변글쓰기가 없고, 파일첨부도 없는) 글쓰기 **** -------

show user;
-- USER이(가) "MYMVC_USER"입니다.    
    
    
desc tbl_member;

create table tbl_board
(seq         number                not null    -- 글번호
,fk_userid   varchar2(20)          not null    -- 사용자ID
,name        varchar2(20)          not null    -- 글쓴이 
,subject     Nvarchar2(200)        not null    -- 글제목
,content     Nvarchar2(2000)       not null    -- 글내용   -- clob (최대 4GB까지 허용) 
,pw          varchar2(20)          not null    -- 글암호
,readCount   number default 0      not null    -- 글조회수
,regDate     date default sysdate  not null    -- 글쓴시간
,status      number(1) default 1   not null    -- 글삭제여부   1:사용가능한 글,  0:삭제된글
,constraint PK_tbl_board_seq primary key(seq)
,constraint FK_tbl_board_fk_userid foreign key(fk_userid) references tbl_member(userid)
,constraint CK_tbl_board_status check( status in(0,1) )
);

create sequence boardSeq
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;

select *
from tbl_board
order by seq desc;


select seq, fk_userid, name, subject  
     , readcount, to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
from tbl_board
where status = 1
order by seq desc;

commit


select seq, fk_userid, name, subject, content, readCount 
     , to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
from tbl_board
where status = 1 and seq = 1;


select seq, fk_userid, name, subject, content, readCount 
     , to_char(regDate, 'yyyy-mm-dd hh24:mi:ss') as regDate
from tbl_board
where status = 1 and seq = '1';