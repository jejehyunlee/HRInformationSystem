package com.juaracoding.HRInformationSystem.controller;

import com.juaracoding.HRInformationSystem.configuration.OtherConfig;
import com.juaracoding.HRInformationSystem.model.Student;
import com.juaracoding.HRInformationSystem.service.StudentService;
import com.juaracoding.HRInformationSystem.utils.MappingAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/school")
public class StudentController {
	
	private StudentService studentService;

	private MappingAttribute mappingAttribute = new MappingAttribute();
	private Map<String,Object> objectMapper = new HashMap<String,Object>();

	@Autowired
	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}
	
	// handler method to handle list students and return mode and view
	@GetMapping("/v1/students")
	public String listStudents(Model model, WebRequest request) {
		if(OtherConfig.getFlagSessionValidation().equals("y"))
		{
			mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
			if(request.getAttribute("USR_ID",1)==null){
				return "redirect:/api/check/logout";
			}
		}
		model.addAttribute("students", studentService.getAllStudents());
		model.addAttribute("students1", new Student());
		return "students";
	}
	
	@GetMapping("/v1/students/new")
	public String createStudentForm(Model model,WebRequest request) {
		if(OtherConfig.getFlagSessionValidation().equals("y")) {
			mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
			if (request.getAttribute("USR_ID", 1) == null) {
				return "redirect:/api/check/logout";
			}
		}
		// create student object to hold student form data
		Student student = new Student();
		model.addAttribute("student", student);
		return "create_student";
		
	}
	
	@PostMapping("/v1/students")
	public String saveStudent(@ModelAttribute("student") Student student,Model model,WebRequest request) {
		if(OtherConfig.getFlagSessionValidation().equals("y")) {
			mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
			if (request.getAttribute("USR_ID", 1) == null) {
				return "redirect:/api/check/logout";
			}
		}
		studentService.saveStudent(student);
		return "redirect:/api/school/v1/students";
	}
	
	@GetMapping("/v1/students/edit/{id}")
	public String editStudentForm(@PathVariable Long id, Model model,WebRequest request) {
		if(OtherConfig.getFlagSessionValidation().equals("y")) {
			mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
			if (request.getAttribute("USR_ID", 1) == null) {
				return "redirect:/api/check/logout";
			}
		}
		model.addAttribute("student", studentService.getStudentById(id));
		return "edit_student";
	}

	@PostMapping("/v1/students/{id}")
	public String updateStudent(@PathVariable Long id,
			@ModelAttribute("student") Student student,
			Model model, WebRequest request) {
		if(OtherConfig.getFlagSessionValidation().equals("y")) {
			mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
			if (request.getAttribute("USR_ID", 1) == null) {
				return "redirect:/api/check/logout";
			}
		}
		// get student from database by id
		Student existingStudent = studentService.getStudentById(id);
		existingStudent.setId(id);
		existingStudent.setFirstName(student.getFirstName());
		existingStudent.setLastName(student.getLastName());
		existingStudent.setEmail(student.getEmail());
		
		// save updated student object
		studentService.updateStudent(existingStudent);
		return "redirect:/api/school/v1/students";
	}
	
	// handler method to handle delete student request
	
	@GetMapping("/v1/students/{id}")
	public String deleteStudent(@PathVariable Long id, Model model, WebRequest request) {
		if(OtherConfig.getFlagSessionValidation().equals("y")) {
			mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
			if (request.getAttribute("USR_ID", 1) == null) {
				return "redirect:/api/check/logout";
			}
		}
		studentService.deleteStudentById(id);
		return "redirect:/students";
	}
	
}
