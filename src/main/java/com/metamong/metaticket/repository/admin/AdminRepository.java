package com.metamong.metaticket.repository.admin;

import com.metamong.metaticket.domain.admin.Admin;
import com.metamong.metaticket.domain.question.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,Long> {

    Admin findByLoginId(String login_id);


}
