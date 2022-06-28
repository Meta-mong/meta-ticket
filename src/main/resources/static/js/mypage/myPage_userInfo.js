window.addEventListener("load", function() {
    //check1
    function passwordRegex() {
        if ($("#passwd").val().trim().length < 1) {
            $("#passwdmsg").html('비밀번호를 입력해주세요.<br/>');
            return false;
        } else {
            var passwdRegExp = /^.*(?=^.{8,12}$)(?=.*\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).*$/;
            if (!passwdRegExp.test($("#passwd").val().trim())) {
                $("#passwdmsg").html('영문,숫자,특수문자가 각각 하나 이상 들어가는 8~12자 사이의 비밀번호를 입력해주세요.<br/>');
                return false;
            } else {
                $("#passwdmsg").html("");
                return true;
            }
        }
    }

    $("#passwd").focusout(function () {
        return passwordRegex();
    });

    //check2
    function passwordCheckRegex(){
        if ($("#passwdck").val().trim().length < 1) {
            $("#passwdckmsg").html("비밀번호를 확인해주세요.<br/>");
            return false;
        }else{
            if($("#passwdck").val() === $("#passwd").val()){
                $("#passwdckmsg").html("");
                return true;
            }else{
                $("#passwdckmsg").html("비밀번호가 같지 않습니다. 다시 확인해주세요.");
                return false;
            }
        }
    }

    $("#passwdck").focusout(function() {
        return passwordCheckRegex();
    });


    $("#modify_btn").on("click", function(){
        var check1 = passwordRegex();
        if(check1==false){
            alert("변경하실 비밀번호를 입력해주세요.");
            return;
        }
        var check2 = passwordCheckRegex();
        if(check2==false){
            alert("비밀번호 확인 칸도 채워주세요.");
            return;
        }
        $.ajax({
           url : "/user/modifyInfo",
           type : "POST",
           data : {"email" : $("#email").val(), "passwd" : $("#passwd").val(), "age" : $("#age").val()},
           dataType : "json",
           success : function (data){
               alert(data.result);
               location.replace("/");
           },
           error : function (data){
               alert("수정 실패. 다시 시도해주세요.");
           }
       })
    });

    //modal
    document.getElementById("resign_btn").onclick = function() {
        document.getElementById("modal").style.display="block";
    }

    document.getElementById("modal_cancle_btn").onclick = function() {
        document.getElementById("modal").style.display="none";
    }


    $("#modal_confirm_btn").on("click", function(){
        //var passwd = prompt('현재 비밀번호를 입력해주세요', '');
        var passwd = $("#resignPasswd").val();

        $.ajax({
            url : "/user/unregister",
            type: "POST",
            data : {"passwd" : passwd},
            dataType: "json",
            success : function (data){
                if(data.result==true){
                    alert("회원 탈퇴가 완료되었습니다.");
                    location.replace("/");
                }else {
                    alert("비밀번호가 일치하지 않습니다. 다시 시도해주세요.");
                    $("#resignPasswd").prop("value", "");
                }
            },
            error : function (data){
                alert("회원 탈퇴 실패. 다시 시도해주세요.");
                $("#resignPasswd").prop("value", "");
            }
        });
    });
});

