 function noticeupdate(id){
         var noticeId = id;
 //        alert(noticeId);
         $.ajax({
             url : "/admin/noticeupdate/"+noticeId,
             type : "POST",
             data : {"title" : $("#title").val(), "content" : $("#content").val(), "classify" : $("#classify").val()},
             dataType : "json",
             success : function (data){
                if(data.result==true){
                     alert("공지사항 수정 완료");
                     location.replace("/admin/anlist");
                }else{
                     alert("공지사항 수정 실패")
                }
             },
             error : function (data){
                alert("공지사항 수정 실패. 다시 시도해주세요.");
             }
        })
 }