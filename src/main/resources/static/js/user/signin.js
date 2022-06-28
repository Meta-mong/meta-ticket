window.addEventListener("load", function(event){


    var signinbtn = document.getElementById("signinbtn");

    var email = document.getElementById("email");
    var emailmsg = document.getElementById("emailmsg");
    var passwd = document.getElementById("passwd");
    var passwdmsg = document.getElementById("passwdmsg");

    email.addEventListener("focusout", function (event) {
        if (email.value.trim().length < 1) {
            emailmsg.innerHTML = 'email은 필수 입력입니다.<br/>';
        } else {
            var emailRegExp = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
            if (!emailRegExp.test(email.value.trim())) {
                emailmsg.innerHTML = '잘못된 이메일 형식입니다.<br/>';
            }else{
                emailmsg.innerText = "";
            }
        }
    });

    passwd.addEventListener("focusout", function () {
        if (passwd.value.trim().length < 1) {
            passwdmsg.innerHTML = '비밀번호를 입력해주세요.<br/>';
        }else{
            passwdmsg.innerText = "";
        }
    });

    $("#passwd").keypress(function (e){
        if(e.keyCode == 13){
            signinbtn.click();
            e.preventDefault(); //클릭 이벤트 외에 별도의 브라우저 행동을 막기 위해 사용하는 메서드
        }
    });

    signinbtn.addEventListener("click", function(event) {
        var flag = true;

        if (email.value.trim().length < 1) {
            emailmsg.innerHTML = 'email은 필수 입력입니다.<br/>';
            flag=false;
        } else {
            var emailRegExp = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
            if (!emailRegExp.test(email.value.trim())) {
                emailmsg.innerHTML = '잘못된 이메일 형식입니다.<br/>';
                flag=false;
            }
        }

        if (passwd.value.trim().length < 1) {
            passwdmsg.innerHTML = '비밀번호를 입력해주세요.<br/>';
            flag=false;
        }

        if (flag == false) {
            return;
        }

        var formdata = $("#login").serialize();

        $.ajax({
            type: "POST",
            url: "/sign/signin",
            //contentType: "application/json; charset=utf-8",
            data: formdata,
            dataType: "json",
            success: function (data, statusText) {
                var temp = JSON.stringify(data);
                var map = JSON.parse(temp);
                if (map.result == 1) {
                    alert(map.msg);
                    location.replace("/");
                }else if(map.result == -1){
                    alert("이미 탈퇴한 계정입니다. 다시 회원가입해주세요.");
                }else if(map.result == 2){
                    var resign = confirm("이미 탈퇴한 계정입니다. 계정을 복구하시겠습니까?");
                    if(resign==true){
                        $.ajax({
                            url : "/resign",
                            type: "POST",
                            data : {"email": $("#email").val()},
                            dataType: "json",
                            success : function (data){
                                if (data.result == true){
                                    alert("계정이 복구되었습니다. 다시 로그인해주세요.");
                                    location.replace("/signin");
                                }else{
                                    alert("계정 복구가 실패되었습니다. 다시 시도해주세요.");
                                }
                            },
                            error : function (data) {
                                alert("에러 발생");
                            }
                        });
                    }else{
                        location.replace("/");
                    }
                }else{
                    alert("계정정보가 일치하지 않습니다.");
                    location.replace("/signin");
                }
            },
            error: function (data, statusText) {
                alert("에러 발생");
                var temp = JSON.stringify(data);
                var map = JSON.parse(temp);
            }
        });
    });

    Kakao.init($("#rest-api").text());
    Kakao.isInitialized();

    $("#kakao").on("click", function(){
        Kakao.Auth.authorize({
            redirectUri : $("#redirect-uri").text()
        });
    });


});