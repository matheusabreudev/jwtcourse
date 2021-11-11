package com.getarraycourse.course.service;

import java.util.List;

import com.getarraycourse.course.domain.UserDom;
import com.getarraycourse.course.exception.domain.EmailExistException;
import com.getarraycourse.course.exception.domain.UserNotFoundException;
import com.getarraycourse.course.exception.domain.UsernameExistException;

public interface UserService{
	UserDom register (String firstName, String lastName, String username, String password) throws UserNotFoundException, UsernameExistException, EmailExistException;
	
	List<UserDom> gerUsers();
	
	UserDom findUserByUsername(String username);
	
	UserDom findUserByEmail(String email);
}
