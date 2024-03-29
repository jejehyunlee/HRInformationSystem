package com.juaracoding.HRInformationSystem.controller;

/*
Created By IntelliJ IDEA 2022.1.3 (Community Edition)
Build #IC-221.5921.22, built on June 21, 2022
@Author JEJE a.k.a Jefri S
Java Developer
Created On 3/4/2023 09:24
@Last Modified 3/4/2023 09:24
Version 1.0
*/

import com.juaracoding.HRInformationSystem.configuration.OtherConfig;
import com.juaracoding.HRInformationSystem.dto.UserDTO;
import com.juaracoding.HRInformationSystem.model.Userz;
import com.juaracoding.HRInformationSystem.service.UserService;
import com.juaracoding.HRInformationSystem.utils.GenerateMenuString;
import com.juaracoding.HRInformationSystem.utils.MappingAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private UserService userService;

    private String [] strExceptionArr = new String[2];

    @Autowired
    public AdminController(UserService userService) {
        strExceptionArr[0]="UserController";
        this.userService = userService;
    }

    private Map<String, Object> objectMapper = new HashMap<String, Object>();

    private MappingAttribute mappingAttribute = new MappingAttribute();


    @PostMapping("/v1/dashboard")
    public String login(@ModelAttribute("usr")
                        @Valid Userz userz,
                        BindingResult bindingResult,
                        Model model,
                        WebRequest request)
    {

        if(bindingResult.hasErrors())
        {
            return "auth/login";
        }

        objectMapper = userService.doLogin(userz,request);
        Boolean isSuccess = (Boolean) objectMapper.get("success");
        String userParse =  objectMapper.get("data")==null?null:"";
        if(userParse==null)
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("usr",new UserDTO());
            return "auth/login";
        }
        if(isSuccess)
        {
            Userz nextUser = (Userz) objectMapper.get("data");
//            nextUser.getAkses().getListMenuAkses().get(0).getMenuHeader().getNamaMenuHeader();
            //        System.out.println(WebRequest.SCOPE_REQUEST);//0
            //        System.out.println(WebRequest.SCOPE_SESSION);//1
            //0 = scope request artinya hanya saat login saja tidak menyimpan di memory server / database
            //1 = scope session artinya setelah login dan akan menyimpan data selama session masih aktif
            request.setAttribute("USR_ID",nextUser.getIdUser(),1);//cara ambil request.getAttribute("USR_ID",1)
            request.setAttribute("EMAIL",nextUser.getEmail(),1);//cara ambil request.getAttribute("EMAIL",1)
            request.setAttribute("NO_HP",nextUser.getNoHP(),1);//cara ambil request.getAttribute("NO_HP",1)
            request.setAttribute("USR_NAME",nextUser.getUsername(),1);//cara ambil request.getAttribute("USR_NAME",1)
            request.setAttribute("HTML_MENU", new GenerateMenuString().menuInnerHtml(nextUser.getAkses()),1);//cara ambil request.getAttribute("USR_NAME",1)
            mappingAttribute.setAttribute(model,objectMapper,request);//urutan nya ini terakhir
            return "admin/dashboard";
        }
        else
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            return "auth/login";
        }
    }

//    @GetMapping("/salestwo")
//    public String getSalesTwo(Model model, WebRequest request)
//    {
//        mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
//        return "sales/sales_two";
//    }




    @GetMapping("/form")
    public String form(Model model, WebRequest request){
        mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
        return "admin/form-elements";
    }

    @GetMapping("/tabel")
    public String tabel(){
        return "admin/table-elements";
    }

    @GetMapping("/karyawan")
    public String karyawan(){
        return "admin/master-karyawan";
    }

    @GetMapping("/absen")
    public String absen(){
        return "admin/master-absen";
    }

    @GetMapping("/report")
    public String report(Model model, WebRequest request){
        mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
        return "admin/report";
    }

    @GetMapping("/student")
    public String student(Model model, WebRequest request){
        mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
        return "admin/student";
    }


    @GetMapping("/logout")
    public String destroySession(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/";
    }


}
