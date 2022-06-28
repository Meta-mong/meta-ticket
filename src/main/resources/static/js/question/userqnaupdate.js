window.addEventListener("load", function() {
    $("#userqnaupdate-btn").on("click", function(){
        $.ajax({
            url : "/question/userqnaupdate/"+$("#questionId").text(),
            type : "POST",
            data : {"title" : $("#title").val(), "content" : $("#content").val(),"classify" : $("#classify").val()},
            dataType : "json",
            success : function (data){
               if(data.result==true){
                    alert("QnA 수정 완료");
                    location.replace("/question/qlist");
               }else{
                    alert("QnA 수정 실패")
               }
            },
            error : function (data){
               alert("QnA 수정 실패. 다시 시도해주세요.");
            }
       })
    });



 });
