package com.juaracoding.HRInformationSystem.controller;


import com.juaracoding.HRInformationSystem.configuration.OtherConfig;
import com.juaracoding.HRInformationSystem.dto.AksesDTO;
import com.juaracoding.HRInformationSystem.dto.DivisiDTO;
import com.juaracoding.HRInformationSystem.dto.ForgetPasswordDTO;
import com.juaracoding.HRInformationSystem.dto.UserDTO;
import com.juaracoding.HRInformationSystem.model.Userz;
import com.juaracoding.HRInformationSystem.service.AksesService;
import com.juaracoding.HRInformationSystem.service.UserService;
import com.juaracoding.HRInformationSystem.utils.ConstantMessage;
import com.juaracoding.HRInformationSystem.utils.ManipulationMap;
import com.juaracoding.HRInformationSystem.utils.MappingAttribute;
import com.juaracoding.HRInformationSystem.utils.PdfGenaratorUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/api/authz")
public class UserController {



    private UserService userService;

    private AksesService aksesService;

    @Autowired
    private ModelMapper modelMapper;

    private Map<String,Object> objectMapper = new HashMap<String,Object>();
    private Map<String,String> mapSorting = new HashMap<String,String>();

    private List<Userz> lsCPUpload = new ArrayList<Userz>();

    private String [] strExceptionArr = new String[2];

    private MappingAttribute mappingAttribute = new MappingAttribute();

    @Autowired
    PdfGenaratorUtil pdfGenaratorUtil;//wajib di deklarasikan kalau pakai thymeleaf engine

    private StringBuilder sBuild = new StringBuilder();

    public UserController(UserService userService, AksesService aksesService) {
        strExceptionArr[0]="UserController";
        mapSorting();
        this.userService = userService;
        this.aksesService = aksesService;
    }
    private void mapSorting()
    {
        mapSorting.put("id","idUser");
        mapSorting.put("nama","namaLengkap");
        mapSorting.put("uname","username");
        mapSorting.put("email","email");
        mapSorting.put("akses","namaAkses");
    }
    @GetMapping("/v1/userman/new")
    public String createUser(Model model,WebRequest request)
    {
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("Default@123");
        model.addAttribute("usr", userDTO);
        model.addAttribute("listAkses", aksesService.getAllAkses());//untuk parent nya
        return "user_management/create_user";
    }

    @GetMapping("/v1/userman/edit/{id}")
    public String editUser(Model model, WebRequest request, @PathVariable("id") Long id)
    {
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        objectMapper = userService.findById(id,request);
        UserDTO userDTO = (objectMapper.get("data")==null?null:(UserDTO) objectMapper.get("data"));
        if((Boolean) objectMapper.get("success"))
        {
            UserDTO userDTOForSelect = (UserDTO) objectMapper.get("data");
            model.addAttribute("usr", userDTO);
            model.addAttribute("listAkses", aksesService.getAllAkses());
            AksesDTO aksesDTO = userDTOForSelect.getAkses();
            String strSelected = aksesDTO==null?"null":aksesDTO.getIdAkses().toString();
            model.addAttribute("selectedValues", strSelected);
            return "user_management/edit_user";
        }
        else
        {
            model.addAttribute("usr", new UserDTO());
            return "redirect:/api/usrmgmnt/v1/userman/default";
        }
    }
    @PostMapping("/v1/userman/new")
    public String newUser(@ModelAttribute(value = "usr")
                          @Valid UserDTO userDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
    )
    {

        AksesDTO aksesDTO = userDTO.getAkses();
        String strIdAkses = aksesDTO==null?null:String.valueOf(aksesDTO.getIdAkses());
        if(strIdAkses==null || strIdAkses.equals("null") || strIdAkses.equals(""))
        {
            mappingAttribute.setErrorMessage(bindingResult,"AKSES WAJIB DIPILIH !!");
        }

        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }

        /* START VALIDATION */
        if(bindingResult.hasErrors())
        {
            model.addAttribute("USR",userDTO);
            model.addAttribute("status","error");
            model.addAttribute("listAkses", aksesService.getAllAkses());

            return "user_management/create_user";
        }
        Boolean isValid = true;

        if(!isValid)
        {
            model.addAttribute("usr",userDTO);
            model.addAttribute("listAkses", aksesService.getAllAkses());
            return "user_management/create_user";
        }
        /* END OF VALIDATION */

        Userz userz = modelMapper.map(userDTO, new TypeToken<Userz>() {}.getType());
        objectMapper = userService.saveUser(userz,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            return "redirect:/api/check/logout";
        }

        if((Boolean) objectMapper.get("success"))
        {
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("usr",new UserDTO());
            return "redirect:/api/authz/v1/userman/default";
//            return "redirect:/api/authz/v1/userman/fbpsb/0/asc/id?columnFirst=id&valueFirst="+idDataSave+"&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        }
        else
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("listAkses", aksesService.getAllAkses());
            model.addAttribute("usr",new UserDTO());
            return "user_management/create_user";
        }
    }

    @PostMapping("/v1/userman/edit/{id}")
    public String editUser(@ModelAttribute("usr")
                           @Valid UserDTO userDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
            , @PathVariable("id") Long id
    )
    {
        userDTO.setIdUser(id);
        AksesDTO aksesDTO = userDTO.getAkses();
        String strIdAkses = aksesDTO==null?null:String.valueOf(aksesDTO.getIdAkses());
        if(strIdAkses==null || strIdAkses.equals("null") || strIdAkses.equals(""))
        {
            mappingAttribute.setErrorMessage(bindingResult,"AKSES DIPILIH !!");
        }

        /* START VALIDATION */
        if(bindingResult.hasErrors())
        {
            model.addAttribute("usr",userDTO);
            model.addAttribute("listAkses", aksesService.getAllAkses());
            model.addAttribute("selectedValues", "");
            return "user_management/edit_user";
        }
        Boolean isValid = true;

        if(!isValid)
        {
            model.addAttribute("usr",userDTO);
            model.addAttribute("listAkses", aksesService.getAllAkses());
            return "user_management/edit_user";
        }
        /* END OF VALIDATION */

        Userz userz = modelMapper.map(userDTO, new TypeToken<Userz>() {}.getType());
        objectMapper = userService.updateUser(id,userz,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            return "redirect:/api/check/logout";
        }

        if((Boolean) objectMapper.get("success"))
        {
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("usr",new UserDTO());
            return "redirect:/api/authz/v1/userman/default";
        }
        else
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("usr",new UserDTO());
            model.addAttribute("listAKses", aksesService.getAllAkses());
            return "user_management/edit_user";
        }
    }


    @GetMapping("/v1/userman/default")
    public String getDefaultData(Model model,WebRequest request)
    {
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        Pageable pageable = PageRequest.of(0,5, Sort.by("idUser"));
        objectMapper = userService.findAllUser(pageable,request);
        mappingAttribute.setAttribute(model,objectMapper,request);

        model.addAttribute("usr",new UserDTO());
        model.addAttribute("sortBy","idUser");
        model.addAttribute("currentPage",1);
        model.addAttribute("asc","asc");
        model.addAttribute("columnFirst","");
        model.addAttribute("valueFirst","");
        model.addAttribute("sizeComponent",5);
        return "/user_management/user_man";
    }

    @GetMapping("/v1/userman/fbpsb/{page}/{sort}/{sortby}")
    public String findByUser(
            Model model,
            @PathVariable("page") Integer pagez,
            @PathVariable("sort") String sortz,
            @PathVariable("sortby") String sortzBy,
            @RequestParam String columnFirst,
            @RequestParam String valueFirst,
            @RequestParam String sizeComponent,
            WebRequest request
    ){
        sortzBy = mapSorting.get(sortzBy);
        sortzBy = sortzBy==null?"idUser":sortzBy;
        Pageable pageable = PageRequest.of(pagez==0?pagez:pagez-1,Integer.parseInt(sizeComponent.equals("")?"5":sizeComponent), sortz.equals("asc")?Sort.by(sortzBy):Sort.by(sortzBy).descending());
        objectMapper = userService.findByPage(pageable,request,columnFirst,valueFirst);
        mappingAttribute.setAttribute(model,objectMapper,request);
        model.addAttribute("usr",new UserDTO());
        model.addAttribute("currentPage",pagez==0?1:pagez);
        model.addAttribute("sortBy", ManipulationMap.getKeyFromValue(mapSorting,sortzBy));
        model.addAttribute("columnFirst",columnFirst);
        model.addAttribute("valueFirst",valueFirst);
        model.addAttribute("sizeComponent",sizeComponent);

        return "/user_management/user_man";
    }

    @GetMapping("/v1/userman/vermail")
    public String linkVerifyEmail(Model model,WebRequest request,@RequestParam String uid,String tkn, String mail)
    {
        objectMapper = userService.linkMailVerify(uid,tkn,mail);
        Boolean isSuccess = (Boolean) objectMapper.get("success");
        if(isSuccess)
        {
            ForgetPasswordDTO forgetPasswordDTO = new ForgetPasswordDTO();
            forgetPasswordDTO.setEmail(mail);
            model.addAttribute("forgetpwd",forgetPasswordDTO);
            return "auth/forget_password";
        }
        else
        {
            return "redirect:/api/check/logout";
        }


    }

    @GetMapping("/v1/userman/xportpdfthyme")
    public ResponseEntity exportToPDFThyme(
            Model model,
            @RequestParam String columnFirst,
            @RequestParam String valueFirst,
            WebRequest request,
            HttpServletResponse response
    ){
        Resource resource = null;
        try {

            mappingAttribute.setAttribute(model,request);//untuk set session ke attribut
            String property = OtherConfig.getPathThymeleafTemplateReport();
            String tempDir = System.getProperty(property);
            List<UserDTO> listUserDTO= userService.dataToExport(request,columnFirst,valueFirst);
            sBuild.setLength(0);
            String fileName = sBuild.append(tempDir).append(OtherConfig.getPathSeparatorReport()).append(UUID.randomUUID().toString()).append(".pdf").toString();
            Map<String,Object> data = new HashMap<String,Object>();
            data.put("content",listUserDTO);
            pdfGenaratorUtil.createPdf("reportz/thymeleaf_template_user",data,fileName);
            Path path = Paths.get(fileName);
            resource = new UrlResource(path.toUri());

        } catch (Exception e) {
            System.out.println(e);
        }
        sBuild.setLength(0);
        return ResponseEntity.ok()

                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_PDF_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, sBuild.append("attachment; filename=\"")
                        .append(resource.getFilename()).append("\"").toString())
                .body(resource);
    }

    @GetMapping("/v1/userman/delete/{id}")
    public String deleteDivisi(Model model, WebRequest request, @PathVariable("id") Long id)
    {
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        objectMapper = userService.deleteUser(id,request);
        mappingAttribute.setAttribute(model,objectMapper);//untuk set session
        model.addAttribute("divisi", new DivisiDTO());
        return "redirect:/api/authz/v1/userman/default";
    }


}