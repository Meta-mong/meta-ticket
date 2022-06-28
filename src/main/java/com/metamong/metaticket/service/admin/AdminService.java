package com.metamong.metaticket.service.admin;

import com.metamong.metaticket.domain.admin.Admin;
import com.metamong.metaticket.domain.question.dto.QuestionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import javax.servlet.http.HttpSession;

public interface AdminService {

    public boolean adminLogin (String loginId, String password) throws Exception;
    public void adminLogout (HttpSession session);

    public Admin adminInfo (String loginId);



}
