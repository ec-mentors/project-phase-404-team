package io.everyonecodes.cryptolog.service;

import io.everyonecodes.cryptolog.data.ConfirmationToken;
import io.everyonecodes.cryptolog.data.User;
import io.everyonecodes.cryptolog.repository.ConfirmationTokenRepository;
import io.everyonecodes.cryptolog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ConfirmationTokenRepository confirmationTokenRepository;
	
	@Autowired
	private EmailSenderService emailSenderService;
	
	private final ConfirmationTokenService confirmationTokenService;
	
	PasswordEncoder encoder = new BCryptPasswordEncoder(12);
	
	private final String email;
	
	public PasswordService(ConfirmationTokenService confirmationTokenService,
	                   @Value("${spring.mail.from.email}") String email) {
		this.confirmationTokenService = confirmationTokenService;
		this.email = email;
	}
	
	public ModelAndView showResetPassword(ModelAndView modelAndView, User user) {
		modelAndView.addObject("user", user);
		modelAndView.setViewName("forgotPassword");
		return modelAndView;
	}
	
	public ModelAndView getForgotUserPassword(ModelAndView modelAndView, User user) {
		Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
		if(existingUser.isPresent()) {
			
			String token = UUID.randomUUID().toString();
			User userE = existingUser.get();
			ConfirmationToken confirmationToken =confirmationTokenService.createToken(userE);
			
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(user.getEmail());
			mailMessage.setSubject("Complete Password Reset!");
			mailMessage.setFrom(email);
			mailMessage.setText("To complete the password reset process, please click here: "
					                    +"http://localhost:9200/confirm-reset?token="+confirmationToken.getToken());
			
			emailSenderService.sendEmail(mailMessage);
			
			modelAndView.addObject("message", "Request to reset password received. Check your inbox for the reset link.");
			modelAndView.setViewName("successForgotPassword");
			
		} else {
			modelAndView.addObject("message", "This email does not exist!");
			modelAndView.setViewName("error");
		}
		
		return modelAndView;
	}
	
	public ModelAndView validateToken(ModelAndView modelAndView, String confirmationToken)
	{
		Optional<ConfirmationToken> oToken = confirmationTokenRepository.findByToken(confirmationToken);
		
		if(oToken.isPresent()) {
			ConfirmationToken token = oToken.get();
			Optional<User> existingUser = userRepository.findByEmail(token.getUser().getEmail());
			if (existingUser.isPresent()) {
				User user = existingUser.get();
				user.setVerified(true);
				userRepository.save(user);
				modelAndView.addObject("user", user);
				modelAndView.addObject("emailId", user.getEmail());
				modelAndView.setViewName("resetPassword2");
			}
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("error");
		}
		return modelAndView;
	}
	
	public ModelAndView getResetUserPassword(ModelAndView modelAndView, User user) {
		
		if(user.getEmail() != null) {
			Optional<User>  otTokenUser = userRepository.findByEmail(user.getEmail());
			if (otTokenUser.isPresent()){
				
				User tokenUser = otTokenUser.get();
				tokenUser.setPassword(encoder.encode(user.getPassword()));
				userRepository.save(tokenUser);
				modelAndView.addObject("message", "Password successfully reset. You can now log in with the new credentials.");
				modelAndView.setViewName("successResetPassword");
			}
		}
		
		else {
			modelAndView.addObject("message","The link is invalid or broken!");
			modelAndView.setViewName("error");
		}
		
		return modelAndView;
	}
	
	
	public UserRepository getUserRepository() {
		return userRepository;
	}
	
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public ConfirmationTokenRepository getConfirmationTokenRepository() {
		return confirmationTokenRepository;
	}
	
	public void setConfirmationTokenRepository(ConfirmationTokenRepository confirmationTokenRepository) {
		this.confirmationTokenRepository = confirmationTokenRepository;
	}
	
	public EmailSenderService getEmailSenderService() {
		return emailSenderService;
	}
	
	public void setEmailSenderService(EmailSenderService emailSenderService) {
		this.emailSenderService = emailSenderService;
	}
}
