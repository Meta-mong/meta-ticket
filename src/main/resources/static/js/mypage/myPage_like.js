function like_click(id){
    var concertId = id;
    $.ajax({
            url:"/interests/"+concertId,
            method:"POST",
            data: "",
            success:function(data){
               window.location.href = "/interests";
            },
            error:function(data){
                alert("실패")
             }
          });
}

  // 상세조회 페이지 이동
  function move_description(id){
    var id = id;
    window.location.href = "/concert/Contents/"+id+"/detail";
  }