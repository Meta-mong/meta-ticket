window.addEventListener("load", function(){
    $("#dropdown").on("change", function(){
        location.href="/admin/aqlist/"+$("#dropdown").val();
    });

    classifyCheck($("#selectedclassify").text());
});

function classifyCheck(data){
    if(data != ""){
        $("#dropdown").val(data).prop("selected", true);
    }
}

