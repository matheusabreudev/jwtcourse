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
			LOGGER.error("User not found by username: " + username);
			throw new UsernameNotFoundException("User not found by username: " + username);
		}else {
			userDom.setLastLoginDateDisplay(userDom.getLastLoginDate());
			userDom.setLastLoginDate(new Date());
			userRepository.save(userDom);
			UserPrincipal userPrincipal = new UserPrincipal(userDom);
			LOGGER.info("Returning found user by username: " + username);
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
		return null;
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
		if(isNotBlank(currentUsername)) {
			UserDom currentUser = findUserByUsername(currentUsername);
			if(currentUser == null) {
				throw new UserNotFoundException("No user found by username" + currentUsername);
			}
			UserDom userByNewUsername = findUserByUsername(newUsername);
			if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
				throw new UsernameExistException("Username already exists");
			}
			UserDom userByNewEmail = findUserByEmail(newEmail);
			if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
				throw new EmailExistException("Email already exists");
			}
			return currentUser;
		} else {
			UserDom currentUser = findUserByUsername(currentUsername);
			if(currentUser != null) {
				throw new UsernameExistException("Username already exists");
			}
			UserDom userByEmail = findUserByEmail(newEmail);
			if(userByEmail != null) {
				throw new EmailExistException("Email already exists");
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
