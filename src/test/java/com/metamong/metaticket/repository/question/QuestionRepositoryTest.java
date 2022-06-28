package com.metamong.metaticket.repository.question;

import com.metamong.metaticket.domain.question.Question;
import com.metamong.metaticket.domain.question.dto.QuestionDTO;
import com.metamong.metaticket.repository.user.UserRepository;
import com.metamong.metaticket.service.question.QuestionService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@PropertySource("classpath:application.yml")
class QuestionRepositoryTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionService questionService;

    Question question;
    QuestionDTO.Quest dto;

    @BeforeEach
    void setUp() {
        question = Question.builder()
                .id(1L)
                .title("제목")
                .classify("분류")
                .answer("내용")
                .build();

        // 객체를 DTO 클래스로 변환
//        dto = QuestionDTO.Quest.builder()
//                .id(1L)
//                .title("제목")
//                .classify("분류")
//                .anwser("s내용")
//                .build();

    }

    @Test
    @DisplayName("문의사항 삽입 테스트1")
    public void InsertTest() {
        Question question = Question.builder().
                classify("테스트").
                title("테스트").
                answer("테스트").
                build();
        Question temp = questionRepository.save(question);
        System.out.println(temp.toString());
    }

    @Test
    @DisplayName("문의사항 수정 테스트")
    public void updateTest() throws Exception {

        Question findQuestion = questionRepository.findById(1L).get();
        //findQuestion.update("테스트1","테스트2","테스트3");
        Question updateNotice = questionRepository.save(findQuestion);

        assertAll(
                () -> assertEquals(updateNotice.getTitle(), "테스트2"),
                () -> assertEquals(updateNotice.getQuesContent(), "테스트3"),
                () -> assertEquals(updateNotice.getClassify(), "테스트1")
        );
    }

    @Test
    @DisplayName("문의사항 삭제 테스트")
    public void deleteTest() {
        Optional<Question> question = questionRepository.findById(1L);
        Assert.assertTrue(question.isPresent());


        question.ifPresent(selectNotice -> {
            questionRepository.delete(selectNotice);
        });

        Optional<Question> deletedQuest = questionRepository.findById(1L);

        Assert.assertFalse(deletedQuest.isPresent());

    }


    @Test
    @DisplayName("문의사항 조회 테스트")
    public void selectTest() {
        Long id = 1L;
        Optional<Question> result = questionRepository.findById(id);

        if (result.isPresent()) {
            Question question = result.get();
            assertEquals(question.getId(), 1L);

            System.out.println(question.toString());
        }

    }
    //객체로 변환

    //문의사항 등록
//    @Test
//    @DisplayName("등록")
//    void register() throws Exception {
//
//        boolean result = questionService.register(dto);
//        Question savedQuestion = questionRepository.save(question);
//
//        assertNotNull(savedQuestion);
//    }


    @Test
    @DisplayName("수정")
    void updateQuestion() throws Exception {
        Question updateQuestion = questionRepository.findById(dto.getId()).orElse(null);
        updateQuestion.update(dto);

    }

    @Test
    @DisplayName("삭제")
    void questionDelete() throws Exception {
        questionRepository.deleteById(1L);
    }


//    @Test
//    @DisplayName("리스트 조회")
//    void list() throws Exception {
//        List<QuestionDTO.Quest> list = questionService.allQuestionList();
//        for (QuestionDTO.Quest temp : list) {
//            System.out.println(temp);
//        }
//
//    }

//    @Test
//    @DisplayName("댓글 등록 여부")
//    void answer() throws Exception {
//
//        boolean result = questionService.register(dto);
//        Question savedQuestion = questionRepository.save(question);
//
//        assertNotNull(savedQuestion);
//    }


    @Test
    @DisplayName("댓글 등록")
    public void replyContent() throws Exception {
        Long id = 2L;
        String answer = "댓글222";
        Question ques = questionService.answer(id, answer);
        System.out.println("결과 : "+ques.toString());
    }


    @Test
    @DisplayName("삭제")
    void replyDelete() throws Exception {
        questionRepository.deleteById(3L);
    }



}
