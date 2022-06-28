new Swiper('.concertSwiper  .swiper-container', {
   direction: 'vertical', // 수직 슬라이드
   autoplay: {
    delay: 3000,
    disableOnInteraction: false,
    }, // 자동 재생 여부
   loop: true, // 반복 재생 여부
  });

  // 상세조회 페이지 이동
  function move_description(id){
    var id = id;
    window.location.href = "/concert/Contents/"+id+"/detail";
  }