package com.metamong.metaticket.service.question;

import com.metamong.metaticket.domain.admin.Admin;
import com.metamong.metaticket.domain.notice.dto.NoticeDTO;
import com.metamong.metaticket.domain.question.Question;
import com.metamong.metaticket.domain.question.dto.QuestionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

public interface QuestionService {


    //문의사항 전제 초회
    Page<QuestionDTO.Quest> allQuestionList(Pageable pageable)throws Exception;
    //문의사항 상세페이지 조회
    QuestionDTO.Quest questiondetail(Long questionId) throws Exception;


    //문의사항 등록을 위한 메서드
    public boolean register(QuestionDTO.AddQuest dto, HttpSession session) throws Exception;

    //

    //문의사항 삭제하는 메서드
    public void questionDelete (Long id) throws Exception;

    //문의사항 수정 -> 유저 내용 수정
    public Question updateQuestion (QuestionDTO.Quest dto) throws Exception;

    //문의사항 - 관리자 답변 추가(댓글)/수정
    public Question answer ( Long ques_id, String Answer) throws Exception;


    //문의사항 - 관리자 답변 삭제
    public void replyDelete(Long ques_id) throws Exception;



    //QnA 셀렉트 박스 정렬 - 리스트 보이기
    Page<QuestionDTO.Quest> qnaselet(String classify, Pageable pageable) throws Exception;
}
