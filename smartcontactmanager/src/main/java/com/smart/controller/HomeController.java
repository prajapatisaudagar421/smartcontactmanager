package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	/*
	 * @Autowired private UserRepository userRepository;
	 * 
	 * @GetMapping("/test")
	 * 
	 * @ResponseBody public String test() {
	 * 
	 * User user = new User(); user.setName("saudagar");
	 * user.setEmail("saudagar2@gmail.com");
	 * 
	 * Contact contact = new Contact(); user.getContacts().add(contact);
	 * 
	 * userRepository.save(user); return "working"; }
	 */
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {
		try {
			if (!agreement) {
				System.out.println("your are not accept term and condition");
				throw new Exception("your are not accept term and condition");
			}
			if (result1.hasErrors()) {
				System.out.println(result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println(agreement);
			System.out.println(user);
			User result = this.userRepository.save(user);
			model.addAttribute("user", new User());

			session.setAttribute("message", new Message("successfully registered", "alert-success"));
			return "signup";

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("something went wrong" + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}

	
	//handler for login user
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login - Smart Contract Manager");
		return "login";
	}
	
	
	
	
}
