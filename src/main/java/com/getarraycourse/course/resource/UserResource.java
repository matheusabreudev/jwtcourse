package com.getarraycourse.course.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.getarraycourse.course.domain.UserDom;
import com.getarraycourse.course.exception.domain.EmailExistException;
import com.getarraycourse.course.exception.domain.ExceptionHandling;
import com.getarraycourse.course.exception.domain.UserNotFoundException;
import com.getarraycourse.course.exception.domain.UsernameExistException;
import com.getarraycourse.course.service.UserService;

@RestController
@RequestMapping(path = {"/", "/user"})
public class UserResource extends ExceptionHandling{
	private UserService userService;
	
	@Autowired
	public UserResource(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<UserDom> register(@RequestBody UserDom user) throws UserNotFoundException, UsernameExistException, EmailExistException {
		UserDom newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
		return new ResponseEntity<>(newUser, HttpStatus.OK);
	}

}
