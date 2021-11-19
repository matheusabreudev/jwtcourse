package com.getarraycourse.course.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.getarraycourse.course.domain.UserDom;
import com.getarraycourse.course.service.LoginAttemptService;

@Component
public class AuthenticationSucessListener {
	private LoginAttemptService loginAttemptService;
	
	@Autowired
	public AuthenticationSucessListener(LoginAttemptService loginAttemptService) {
		this.loginAttemptService = loginAttemptService;
	}
	
	@EventListener
	public void onAuthenticationSucess(AuthenticationSuccessEvent event) {
		Object principal = event.getAuthentication().getPrincipal();
		if(principal instanceof UserDom) {
			UserDom user = (UserDom) event.getAuthentication().getPrincipal();
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
		}
	}
	
	
}
