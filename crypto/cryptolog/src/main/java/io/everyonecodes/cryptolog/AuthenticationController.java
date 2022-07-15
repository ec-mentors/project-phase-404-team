package io.everyonecodes.cryptolog;

import io.everyonecodes.cryptolog.data.ConfirmationToken;
import io.everyonecodes.cryptolog.data.User;
import io.everyonecodes.cryptolog.service.ConfirmationTokenService;
import io.everyonecodes.cryptolog.service.UserService;
import io.everyonecodes.cryptolog.service.VerificationEmailSenderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class AuthenticationController {
    private final UserService userService;
    private final VerificationEmailSenderService emailSenderService;
    private final ConfirmationTokenService confirmationTokenService;

    public AuthenticationController(UserService userService, VerificationEmailSenderService emailSenderService, ConfirmationTokenService confirmationTokenService) {
        this.userService = userService;
        this.emailSenderService = emailSenderService;
        this.confirmationTokenService = confirmationTokenService;
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/register")
    String register(Model model) {
        User user = new User();
        model.addAttribute(user);
        return "register";
    }

    @GetMapping("/home")
    String home() {
        return "home";
    }

    @PostMapping("/register")
    public String registerUser(@Valid User user, BindingResult bindingResult, Model model) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            model.addAttribute("successMessage", "Please write the fields in the right format!");
            return "register";
        } else if (userService.isUserAlreadyPresent(user)) {
            model.addAttribute("successMessage", "User already exists!");
        } else {
            userService.saveUser(user);
            model.addAttribute("successMessage", "User is registered successfully, please verify your email.");// <a href=\"/login\">Login</a>");
            // modelAndView.setViewName("firstlogin");
            ConfirmationToken confirmationToken = confirmationTokenService.createToken(user);
            emailSenderService.sendEmail(user, confirmationToken);
        }
        model.addAttribute("user", new User());
        return "register";
    }

    @GetMapping(path = "confirm")
    public ModelAndView confirm(@RequestParam("token") String token) {
        return confirmationTokenService.confirmToken(token);
    }
}

