// 찜버튼
$(".like_btn2 .btn-secondary").click(function() {
    alert("로그인이 필요합니다.");
    window.location.href = '/signin';
});

function like_click(id){
    var concertId = id;
    $.ajax({
            url:"/interests/"+concertId,
            method:"POST",
            data: "",
            success:function(data){
                $('.btn-secondary').toggleClass('done');
            },
            error:function(data){
                alert("실패")
             }
          });
}

function load_interest(id){
 var concertId = id;
 $.ajax({
            url:"/interests/"+concertId,
            method:"GET",
            data: "",
            success:function(data){
                if(data == 'false'){
                    $('.btn-secondary').toggleClass('done');
                }
            },
            error:function(data){
                alert("실패")
             }
          });
}