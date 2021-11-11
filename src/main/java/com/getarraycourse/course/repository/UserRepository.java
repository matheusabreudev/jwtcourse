package com.getarraycourse.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.getarraycourse.course.domain.UserDom;

public interface UserRepository extends JpaRepository<UserDom, Long>{
	
	UserDom findUserByUsername(String username);
	UserDom findUserByEmail(String email);

}
