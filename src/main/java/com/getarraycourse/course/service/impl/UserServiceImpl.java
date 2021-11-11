package com.getarraycourse.course.service.impl;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import static org.apache.commons.lang3.StringUtils.*;

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
import static com.getarraycourse.course.enumeration.Role.*;
import com.getarraycourse.course.exception.domain.EmailExistException;
import com.getarraycourse.course.exception.domain.UserNotFoundException;
import com.getarraycourse.course.exception.domain.UsernameExistException;
import com.getarraycourse.course.repository.UserRepository;
import com.getarraycourse.course.service.UserService;

@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService{

	private static final String NO_USER_FOUND_BY_USERNAME = "No user found by username";
	private static final String USERNAME_ALREADY_EXISTS = "Username already exists";
	private static final String EMAIL_ALREADY_EXISTS = "Email already exists";
	private static final String FOUND_USER_BY_USERNAME = "Username found";
	private Logger LOGGER = org.slf4j.LoggerFactory.getLogger(getClass());
	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		UserDom userDom = userRepository.findUserByUsername(username); 
		if(userDom == null) {
			LOGGER.error(NO_USER_FOUND_BY_USERNAME + username);
			throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
		}else {
			userDom.setLastLoginDateDisplay(userDom.getLastLoginDate());
			userDom.setLastLoginDate(new Date());
			userRepository.save(userDom);
			UserPrincipal userPrincipal = new UserPrincipal(userDom);
			LOGGER.info(FOUND_USER_BY_USERNAME + username);
			return userPrincipal;
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
		return ServletUriComponentsBuilder.fromCurrentRequest().path("/user/image/profile/temp").toUriString();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserDom findUserByUsername(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserDom findUserByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}
}
