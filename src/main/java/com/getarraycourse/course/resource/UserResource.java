package com.getarraycourse.course.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.getarraycourse.course.domain.UserDom;
import com.getarraycourse.course.domain.UserPrincipal;
import com.getarraycourse.course.exception.domain.EmailExistException;
import com.getarraycourse.course.exception.domain.ExceptionHandling;
import com.getarraycourse.course.exception.domain.UserNotFoundException;
import com.getarraycourse.course.exception.domain.UsernameExistException;
import com.getarraycourse.course.service.UserService;
import com.getarraycourse.course.utility.JWTTokenProvider;
import static com.getarraycourse.course.constant.SecurityConstant.*;

@RestController
@RequestMapping(path = {"/", "/user"})
public class UserResource extends ExceptionHandling{
	private UserService userService;
	private AuthenticationManager authenticationManager;
	private JWTTokenProvider jwtTokenProvider;
	
	@Autowired
	public UserResource(UserService userService, AuthenticationManager authenticationManager,
			JWTTokenProvider jwtTokenProvider) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	
	@PostMapping("/login")
	public ResponseEntity<UserDom> login(@RequestBody UserDom user) {
		autheticate(user.getUsername(), user.getPassword());
		UserDom loginUser = userService.findUserByUsername(user.getUsername());
		UserPrincipal userPrincipal = new UserPrincipal(loginUser);
		HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
		return new ResponseEntity<>(loginUser, jwtHeader,  HttpStatus.OK);
	}


	@PostMapping("/register")
	public ResponseEntity<UserDom> register(@RequestBody UserDom user) throws UserNotFoundException, UsernameExistException, EmailExistException {
		UserDom newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
		return new ResponseEntity<>(newUser, HttpStatus.OK);
	}
	
	private HttpHeaders getJwtHeader(UserPrincipal user) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
		return headers;
	}

	private void autheticate(String username, String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
	}

}
