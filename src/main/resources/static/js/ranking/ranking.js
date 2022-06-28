function ranking(value){
    var genre = value;

    $.ajax({
        url:"/ranking/"+genre,
        type : "get",
        dataType: 'json',
        data:'',
        success: function(data){
         $('.ranking-page-main-div').empty();

        for(i=0; i<data.content.length; i++){
        $('.ranking-page-main-div').append(" <div class = 'concertRanking'><div class='main-div-ranking-page'><span>"+(i+1)+"</span></div><div class='concert-name'><img src='/concert/readImg/"+data.content[i].phamplet+"' alt='' style='width: 100px; height: 150px;'><div><span class='onlySelling'>단독판매</span><span>"
                                      + data.content[i].title+"</span></div></div> <div class='concert-time'><p>"+data.content[i].concertDate.substring(0,10)+"</p> </div><div class='concert-place'><span>" +data.content[i].address+"</span></div><div class='reservation'><span>" + data.content[i].visitCnt + " </span></div></div>");}
        },
        error: function(data){
        alert("실패")
        }
    });
}