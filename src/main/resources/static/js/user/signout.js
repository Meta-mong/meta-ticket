window.addEventListener("load", function (){
    $("#signout").on("click", function (){
        var signoutCheck = confirm("로그아웃 하시겠습니까?");
        if(signoutCheck==false) return;
        location.href="/signout";
    })
});