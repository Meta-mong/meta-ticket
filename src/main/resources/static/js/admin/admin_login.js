 function adminlogin() {

         $.ajax({
             url:"/admin",
             type:"POST",
             data:{"loginId":$(".userId").val(),  "password":$(".userPw").val()},
             dataType:"json",
             success: function(data){
                 if(data == true){
                    window.location.href = "/concert/adminConcert"
                 }else{
                    window.location.href = "/admin"
                 }

             },
             error: function(){
                 alert("에러")
             }
         });

  }