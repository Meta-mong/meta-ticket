window.addEventListener("load", function (){
    $("#genre").change(function (){
       location.href="/ticketopen/"+$("#genre").val();
    });
});

function genreCheck(data){
    if(data != ""){
        $("#genre").val(data).prop("selected", true);
    }
}