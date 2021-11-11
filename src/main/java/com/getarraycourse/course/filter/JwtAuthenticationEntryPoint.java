package com.getarraycourse.course.filter;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.*;

import static org.springframework.http.MediaType.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import static com.getarraycourse.course.constant.SecurityConstant.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getarraycourse.course.domain.HttpResponse;

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint{

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
			throws IOException {
		
		HttpResponse httpResponse = new HttpResponse(
				FORBIDDEN.value(),
				FORBIDDEN, 
				FORBIDDEN.getReasonPhrase().toUpperCase(), 
				FORBIDDEN_MESSAGE);
		
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setStatus(FORBIDDEN.value());
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(outputStream, httpResponse);
		outputStream.flush();
	}
	
}
