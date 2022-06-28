package com.metamong.metaticket.service.question;

import com.metamong.metaticket.domain.question.Question;
import com.metamong.metaticket.domain.question.dto.QuestionDTO;
import com.metamong.metaticket.domain.user.User;
import com.metamong.metaticket.domain.user.dto.UserDTO;
import com.metamong.metaticket.repository.admin.AdminRepository;
import com.metamong.metaticket.repository.question.QuestionRepository;
import com.metamong.metaticket.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;


@Service
public class QuestionServiceImpl implements QuestionService{
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    //객체로 변환

    public Question dtoToEntity (QuestionDTO.Quest dto){
        Question question = Question.builder()
                //.id(dto.getId())
                .user(userRepository.findById(dto.getUserId()).get())
                .classify(dto.getClassify())
                .title(dto.getTitle())
                .quesContent(dto.getQuesContent())
                .answer(dto.getAnswer())
                .build();

        return question;
    }

    // 객체를 DTO 클래스로 변환
    public static QuestionDTO.Quest entityToDTO(Question question){
        if(question.getAnswer()==null) question.setAnswer("");
        QuestionDTO.Quest dto =QuestionDTO.Quest.builder()
                .id(question.getId())
                .userId(question.getUser().getId())
                .userName(question.getUser().getName())
                .title(question.getTitle())
                .classify(question.getClassify())
                .quesContent(question.getQuesContent())
                .answer(question.getAnswer())
                .createDate(LocalDate.from(question.getCreatedDate()))
                .updateDate(LocalDate.from(question.getUpdatedDate()))
                .build();
        return dto;
    }

    //문의사항 조회
    @Override
    public Page<QuestionDTO.Quest> allQuestionList(Pageable pageable) throws Exception {

        int page = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1); // page는 index 처럼 0부터 시작
        pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<Question> listpage = questionRepository.findAll(pageable);
        Page<QuestionDTO.Quest> dto = listpage.map(QuestionServiceImpl::entityToDTO);

        return dto;
    }

    //상세조회
    @Override
    public QuestionDTO.Quest questiondetail(Long questionId) throws Exception {

        Question question = questionRepository.findById(questionId).orElse(null);
        QuestionDTO.Quest questionDTO = entityToDTO(question);
        return questionDTO;
    }


    //문의사항 등록
    @Override
    public boolean register(QuestionDTO.AddQuest dto, HttpSession session) throws Exception {
        try {
            UserDTO.SESSION_USER_DATA userDto = (UserDTO.SESSION_USER_DATA)session.getAttribute("user");
            User user = userRepository.findById(userDto.getId()).get();
            Question question =Question.builder()
                    .title(dto.getTitle())
                    .classify(dto.getClassify())
                    .quesContent(dto.getQuesContent())
                    .user(user)
                    .build();
            questionRepository.save(question);
            return true;
        }catch (Exception e){
            throw e;
        }

    }

    //문의사항 삭제
    @Override
    @Transactional
    public void questionDelete(Long id) throws Exception {
        Question quest = questionRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id=" + id));
        questionRepository.delete(quest);
    }


    //문의사항 수정
    @Override
    public Question updateQuestion(QuestionDTO.Quest dto) throws Exception {
        Question updateQuestion = questionRepository.findById(dto.getId()).orElse(null);
        updateQuestion.setTitle(dto.getTitle());
        updateQuestion.setQuesContent(dto.getQuesContent());
        updateQuestion.setClassify(dto.getClassify());
        questionRepository.save(updateQuestion);
        return updateQuestion;
    }


    //답글 추가 / 수정
    @Override
    public Question answer(Long ques_id, String answer) throws Exception {
        Question question = questionRepository.findById(ques_id).orElse(null);
        question.setAnswer(answer);
        Question quest = questionRepository.save(question);

        return  quest;
    }


    //댓글 삭제
    @Override
    @Transactional
    public void replyDelete(Long id) throws Exception {
        Question reply = questionRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 답글 존재하지 않습니다. id=" + id));
        questionRepository.delete(reply);
    }

    @Override
    public Page<QuestionDTO.Quest> qnaselet(String classify, Pageable pageable) throws Exception {
        int page = (pageable.getPageNumber() == 0) ? 0 : (pageable.getPageNumber() - 1); // page는 index 처럼 0부터 시작
        pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<Question> questions = questionRepository.findByClassifyOrderByCreatedDateDesc(classify,pageable);
        Page<QuestionDTO.Quest> dto = questions.map(QuestionServiceImpl::entityToDTO);

        return dto;
    }


}
