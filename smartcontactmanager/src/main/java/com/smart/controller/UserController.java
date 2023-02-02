package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data from response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("username" + userName);
		User user = userRepository.getUserByUserName(userName);
		System.out.println(user);
		model.addAttribute("user", user);
	}

	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// add contact handler
	@GetMapping("/add-contact")
	public String openAddCOntactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_from";
	}

	@PostMapping("/process-contact")
	public String pocessContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			// processing to uploadubg file
			if (file.isEmpty()) {
				System.out.println("image is empty");
				contact.setImage("contact.png");
			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("/static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded");
			}

			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);
			System.out.println(contact);
			System.out.println("add to database");

			// message success.....
			session.setAttribute("message", new Message("Your contact is add !! Add more...", "success"));

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("eroor" + e.getMessage());
			e.printStackTrace();
			// error message
			session.setAttribute("message", new Message("Something went wrong !! Try again...", "danger"));
		}

		return "normal/add_contact_from";
	}

	// show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show User Title");

		// getting contact from database
		// your can user this
		/*
		 * String userName = principal.getName(); User user =
		 * this.userRepository.getUserByUserName(userName); List<Contact> contacts =
		 * user.getContacts();
		 */
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page, 8);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// showing particular contacts details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		Optional<Contact> optional = this.contactRepository.findById(cId);
		Contact contact = optional.get();
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("title", contact.getName());
			model.addAttribute("contact", contact);
		}
		return "normal/contact_detail";
	}

	// deleteing contacts
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, HttpSession session,Principal principal) {
		Contact contact = this.contactRepository.findById(cid).get();
//		contact.setUser(null);
//		this.contactRepository.delete(contact);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("contact deleted successfully....", "success"));
		return "redirect:/user/show-contacts/0";
	}

	// update from
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {

		model.addAttribute("title", "Update Form");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}

	// update process
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, Principal principal, HttpSession session) {
		try {
			
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			//image
			if(!file.isEmpty()) {
				//rewrite
				//delete old photo
				File deleteFile = new ClassPathResource("/static/img").getFile();
				File file1 = new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();
				
				
				//update old photo
				File saveFile = new ClassPathResource("/static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(oldcontactDetail.getImage());
			}
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated", "success"));
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Your Profile");
		return "normal/profile";
	}
	
	
	
	
	

}
