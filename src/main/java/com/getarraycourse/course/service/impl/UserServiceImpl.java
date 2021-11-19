package com.getarraycourse.course.service.impl;

import static com.getarraycourse.course.enumeration.Role.ROLE_USER;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.getarraycourse.course.domain.UserDom;
import com.getarraycourse.course.domain.UserPrincipal;
import com.getarraycourse.course.exception.domain.EmailExistException;
import com.getarraycourse.course.exception.domain.UserNotFoundException;
import com.getarraycourse.course.exception.domain.UsernameExistException;
import com.getarraycourse.course.repository.UserRepository;
import com.getarraycourse.course.service.LoginAttemptService;
import com.getarraycourse.course.service.UserService;
import static com.getarraycourse.course.constant.UserImplConstant.*;

@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService{

	
	private Logger LOGGER = org.slf4j.LoggerFactory.getLogger(getClass());
	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;
	private LoginAttemptService loginAttemptService;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.loginAttemptService = loginAttemptService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserDom userDom = userRepository.findUserByUsername(username); 
		if(userDom == null) {
			LOGGER.error(NO_USER_FOUND_BY_USERNAME + username);
			throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
		}else {
			validateLoginAttempt(userDom);
			userDom.setLastLoginDateDisplay(userDom.getLastLoginDate());
			userDom.setLastLoginDate(new Date());
			userRepository.save(userDom);
			UserPrincipal userPrincipal = new UserPrincipal(userDom);
			LOGGER.info(FOUND_USER_BY_USERNAME + username);
			return userPrincipal;
		}
	}

	private void validateLoginAttempt(UserDom userDom) {
		if(userDom.isNotLocked()) {
			if(loginAttemptService.hasExceededMaxAttempts(userDom.getUsername())) {
				userDom.setNotLocked(false);
			}else {
				userDom.setNotLocked(true);
			}
		}else {
			loginAttemptService.evictUserFromLoginAttemptCache(userDom.getUsername());
		}
	}

	@Override
	public UserDom register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException {
		validateNewUsernameAndEmail(EMPTY, username, email);
		UserDom user = new UserDom();
		user.setUserId(generateUserId());
		String password = generatePassword();
		String encodedPassword = encodePassword(password);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setEmail(email);
		user.setJoinDate(new Date());
		user.setPassword(encodedPassword);
		user.setActive(true);
		user.setNotLocked(true);
		user.setRole(ROLE_USER.name());
		user.setAuthorities(ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl());
		userRepository.save(user);
		LOGGER.info("New user password: " + password);
		return user;
	}

	private String getTemporaryProfileImageUrl() {
		return ServletUriComponentsBuilder.fromCurrentRequest().path(DEFAUL_USER_IMAGE_PATH).toUriString();
	}

	private String encodePassword(String password) {
		return passwordEncoder.encode(password);
	}

	private String generatePassword() {
		return RandomStringUtils.randomAlphanumeric(10);
	}

	private String generateUserId() {
		return RandomStringUtils.randomNumeric(10);
	}

	private UserDom validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException{
		UserDom userByNewUsername = findUserByUsername(newUsername);
		UserDom userByNewEmail = findUserByEmail(newEmail);
		if(isNotBlank(currentUsername)) {
			UserDom currentUser = findUserByUsername(currentUsername);
			if(currentUser == null) {
				throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
			}
			if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
				throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
			}
			if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
				throw new EmailExistException(EMAIL_ALREADY_EXISTS);
			}
			return currentUser;
		} else {
			if(userByNewUsername != null) {
				throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
			}
			if(userByNewEmail != null) {
				throw new EmailExistException(EMAIL_ALREADY_EXISTS);
			}
			return null;
		}
		
	}

	@Override
	public List<UserDom> gerUsers() {
		return userRepository.findAll();
	}

	@Override
	public UserDom findUserByUsername(String username) {
		return userRepository.findUserByUsername(username);
	}

	@Override
	public UserDom findUserByEmail(String email) {
		return userRepository.findUserByEmail(email);
	}
}
