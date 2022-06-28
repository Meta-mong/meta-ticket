window.addEventListener("load", function(){
    $("#dropdown").change(function(){
        location.href="/question/qlist/"+$("#dropdown").val();
    });

});


