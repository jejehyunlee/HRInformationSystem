package com.juaracoding.HRInformationSystem.controller;

/*
Created By IntelliJ IDEA 2022.1.3 (Community Edition)
Build #IC-221.5921.22, built on June 21, 2022
@Author JEJE a.k.a Jefri S
Java Developer
Created On 3/16/2023 18:00
@Last Modified 3/16/2023 18:00
Version 1.0
*/


import com.juaracoding.HRInformationSystem.dto.ForgetPasswordDTO;
import com.juaracoding.HRInformationSystem.dto.UserDTO;
import com.juaracoding.HRInformationSystem.handler.FormatValidation;
import com.juaracoding.HRInformationSystem.model.Userz;
import com.juaracoding.HRInformationSystem.service.UserService;
import com.juaracoding.HRInformationSystem.utils.ConstantMessage;
import com.juaracoding.HRInformationSystem.utils.GenerateMenuString;
import com.juaracoding.HRInformationSystem.utils.MappingAttribute;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/authz")
public class AuthController {

    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;
    private Map<String,Object> objectMapper = new HashMap<String,Object>();

    private List<Userz> lsCPUpload = new ArrayList<Userz>();

    private String [] strExceptionArr = new String[2];

    private MappingAttribute mappingAttribute = new MappingAttribute();

    @Autowired
    public AuthController(UserService userService) {
        strExceptionArr[0]="AuthzController";
        this.userService = userService;
    }

    /*
        VALIDASI FORM REGISTRASI
     */
    @PostMapping("/v1/register")
    public String doRegis(@ModelAttribute("usr")
                          @Valid UserDTO userz
            , BindingResult bindingResult
            , Model model
            , WebRequest request
    )
    {
        /* START VALIDATION */
        if(bindingResult.hasErrors())
        {
            model.addAttribute("usr",userz);
            return "auth/register";
        }
        Boolean isValid = true;
        if(!FormatValidation.phoneNumberFormatValidation(userz.getNoHP(),null))
        {
            isValid = false;
            mappingAttribute.setErrorMessage(bindingResult, ConstantMessage.ERROR_PHONE_NUMBER_FORMAT_INVALID);
        }

        if(!FormatValidation.emailFormatValidation(userz.getEmail(),null))
        {
            isValid = false;
            mappingAttribute.setErrorMessage(bindingResult, ConstantMessage.ERROR_EMAIL_FORMAT_INVALID);
        }
        if(!isValid)
        {
            model.addAttribute("users",userz);
            return "auth/register";
        }
        /* END OF VALIDATION */

        Userz users = modelMapper.map(userz, new TypeToken<Userz>() {}.getType());
        objectMapper = userService.checkRegis(users,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            return "redirect:/api/check/logout";
        }

        if((Boolean) objectMapper.get("success"))
        {
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("verifyEmail",userz.getEmail());
            model.addAttribute("users",new Userz());

            return "auth/verifikasi";
        }
        else
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("users",users);
            return "auth/register";
        }
    }

    /*
        VERIFIKASI TOKEN SETELAH MENGINPUT FORM REGISTRASI
     */
    @GetMapping("/v1/taketoken")
    public String requestTokenz(@ModelAttribute("usr")
                                @Valid UserDTO userz,
                                BindingResult bindingResult, Model model, @RequestParam String email, WebRequest request)
    {

        if(email == null || email.equals("") || !FormatValidation.emailFormatValidation(email,null))
        {
            return "redirect:/api/check/logout";//LANGSUNG LOGOUT KARENA FLOW TIDAK VALID / MUNGKIN HIT API INI BUKAN DARI WEB
        }

        objectMapper = userService.getNewToken(email,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            return "redirect:/api/check/logout";
        }
        Boolean isSuccess = (Boolean) objectMapper.get("success");
        if(isSuccess)
        {
            model.addAttribute("verifyEmail",email);
            model.addAttribute("usr",new UserDTO());
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            return "auth/verifikasi";
        }
        else
        {
            model.addAttribute("usr",new UserDTO());
            return "auth/signin";
        }
    }

    /*
        VERIFIKASI TOKEN SETELAH MENGINPUT FORM REGISTRASI
     */
    @PostMapping("/v1/verify")
    public String verifyRegis(@ModelAttribute("usr")
                              @Valid Userz userz,
                              BindingResult bindingResult,
                              Model model,
                              @RequestParam String email,
                              WebRequest request)
    {
        //tidak ada bindingResult karena tidak memerlukan validasi di masing-masing field

        String verToken = userz.getToken();
        int lengthToken = verToken.length();
        if(email== null || email.equals("") || !FormatValidation.emailFormatValidation(email,null))
        {
            return "redirect:/api/check/logout";//Flow sudah salah kalau ini kosong ATAU FORMAT EMAIL TIDAK SESUAI
        }

        if(verToken.equals(""))//token tidak boleh kosong
        {
            mappingAttribute.setErrorMessage(bindingResult,ConstantMessage.ERROR_TOKEN_IS_EMPTY);
            model.addAttribute("verifyEmail",email);
            return "auth/verifikasi";
        }
        else if(lengthToken!=6)//token HARUS 6 DIGIT
        {
            mappingAttribute.setErrorMessage(bindingResult,ConstantMessage.ERROR_TOKEN_INVALID);
            model.addAttribute("verifyEmail",email);
            return "auth/verifikasi";
        }

        objectMapper = userService.confirmRegis(userz,email,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            return "redirect:/api/check/logout";
        }

        if((Boolean) objectMapper.get("success"))
        {
            mappingAttribute.setErrorMessage(bindingResult,"REGISTRASI BERHASIL SILAHKAN LOGIN");
            model.addAttribute("users",new Userz());//agar field kosong
            return "auth/login";
        }
        else
        {
            model.addAttribute("verifyEmail",email);
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            return "auth/verifikasi";
        }
    }

    /*
        API UNTUK LOGIN
     */
    @PostMapping("/v1/login")
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

    /*
        PENGECEKAN PERTAMA KALI UNTUK LUPA PASSWORD
     */
    @PostMapping("/v1/forgetpwd")
    public String sendMailForgetPwd(@ModelAttribute("forgetpwd")
                                    @Valid ForgetPasswordDTO forgetPasswordDTO,
                                    BindingResult bindingResult
            ,Model model
            ,WebRequest request

    )
    {
        String emailz = forgetPasswordDTO.getEmail();
        Boolean isInvalid = false;

        if(emailz== null || emailz.equals(""))
        {
            isInvalid = true;
            mappingAttribute.setErrorMessage(bindingResult,ConstantMessage.ERROR_EMAIL_IS_EMPTY);//FLOW AWAL UNTUK VALIDASI EMAIL

        }
        if(!FormatValidation.emailFormatValidation(emailz,null))
        {
            isInvalid = true;
            mappingAttribute.setErrorMessage(bindingResult, ConstantMessage.ERROR_EMAIL_FORMAT_INVALID);
        }

        if(isInvalid)// AGAR KELUAR KEDUA VALIDASI INI DI NOTIFIKASI NYA
        {
            return "auth/forget_pwd_email";
        }

        objectMapper = userService.sendMailForgetPwd(emailz,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            return "redirect:/api/check/logout";
        }
        Boolean isSuccess = (Boolean) objectMapper.get("success");
        ForgetPasswordDTO nextForgetPasswordDTO = new ForgetPasswordDTO();
        if(isSuccess)
        {
            mappingAttribute.setAttribute(model,objectMapper);
            nextForgetPasswordDTO.setEmail(emailz);
            model.addAttribute("forgetpwd",nextForgetPasswordDTO);
            return "auth/forget_pwd_verifikasi";
        }
        else
        {

            model.addAttribute("forgetpwd",nextForgetPasswordDTO);
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            return "auth/forget_pwd_email";
        }

    }

    /*
        VERIFIKASI TOKEN YANG SUDAH DIKIRIM KE EMAIL UNTUK LUPA PASSWORD PROSES YANG PERTAMYA KALI
     */
    @PostMapping("/v1/vertokenfp")
    public String verifyTokenForgetPwd(@ModelAttribute("forgetpwd")
                                       @Valid ForgetPasswordDTO forgetPasswordDTO,
                                       BindingResult bindingResult
            ,Model model
            ,WebRequest request
    )
    {

        String emailz = forgetPasswordDTO.getEmail();
        String tokenz = forgetPasswordDTO.getToken();
        int intTokenLength = tokenz.length();
        Boolean isValid = true;
        Boolean isInvalidFlow = false;

        if(emailz== null || emailz.equals(""))
        {
            isInvalidFlow = true;// KALAU SUDAH KOSONG ARTINYA DIA GAK AKAN PERNAH BISA MELANJUTKAN PROSES JADI SEBAIKNYA DILOGOUT SAJA
        }
        if(!FormatValidation.emailFormatValidation(emailz,null))
        {
            isInvalidFlow = true;// KALAU SUDAH KOSONG ARTINYA DIA GAK AKAN PERNAH BISA MELANJUTKAN PROSES JADI SEBAIKNYA DILOGOUT SAJA
        }

        if(isInvalidFlow)
        {
            return "redirect:/api/check/logout";
        }

        /*START VALIDATION*/
        if(intTokenLength!=6)
        {
            mappingAttribute.setErrorMessage(bindingResult,ConstantMessage.ERROR_TOKEN_INVALID);
            isValid = false;
        }
        if(tokenz==null || tokenz.equals(""))
        {
            mappingAttribute.setErrorMessage(bindingResult,ConstantMessage.ERROR_TOKEN_IS_EMPTY);
            isValid = false;
        }

        if(!isValid)
        {
            model.addAttribute("forgetpwd",forgetPasswordDTO);
            return "auth/forget_pwd_verifikasi";
        }/*END OF VALIDATION*/

        objectMapper = userService.confirmTokenForgotPwd(forgetPasswordDTO,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))
        {
            return "redirect:/api/check/logout";
        }

        Boolean isSuccess = (Boolean) objectMapper.get("success");
        if(isSuccess)
        {
            ForgetPasswordDTO nextForgetPasswordDTO = new ForgetPasswordDTO();
            mappingAttribute.setAttribute(model,objectMapper);
            nextForgetPasswordDTO.setEmail(emailz);
            model.addAttribute("forgetpwd",nextForgetPasswordDTO);
            return "auth/forget_password";
        }
        else
        {
            model.addAttribute("forgetpwd",forgetPasswordDTO);
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            return "auth/forget_pwd_verifikasi";
        }

    }

    /*
        LOGIC UNTUK COMPARE PASSWORD LAMA , PASSWORD BARU DAN PASSWORD KONFIRMASI
     */
    @PostMapping("/v1/cfpwd")
    public String verifyForgetPwd(@ModelAttribute("forgetpwd")
                                  @Valid ForgetPasswordDTO forgetPasswordDTO,
                                  BindingResult bindingResult
            ,Model model
            ,WebRequest request
    )
    {
        if(bindingResult.hasErrors())
        {
            model.addAttribute("forgetpwd",forgetPasswordDTO);
            return "auth/forget_password";
        }

        String emailz = forgetPasswordDTO.getEmail();

        if(emailz== null || emailz.equals(""))
        {
            return "redirect:/api/check/logout";
        }

        objectMapper = userService.confirmPasswordChange(forgetPasswordDTO,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))
        {
            return "redirect:/api/check/logout";
        }

        Boolean isSuccess = (Boolean) objectMapper.get("success");
        if(isSuccess)
        {
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("usr",new UserDTO());
            return "auth/login";
        }
        else
        {
            model.addAttribute("forgetpwd",forgetPasswordDTO);
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            return "auth/forget_password";
        }
    }

    /*
        API GENERATE NEW TOKEN UNTUK LUPA PASSWORD PROSES SELANJUTNYA SETELAH PROSES KIRIM TOKEN YANG PERTAMA
     */
    @GetMapping("/v1/ntverfp")
    public String requestTokenzForgetPwd(@ModelAttribute("forgetpwd")
                                         @Valid ForgetPasswordDTO forgetPasswordDTO,
                                         BindingResult bindingResult,
                                         Model model,
                                         @RequestParam String emailz,
                                         WebRequest request)
    {
        forgetPasswordDTO.setToken("");//DIKOSONGKAN UNTUK MENGHILANGKAN INPUTAN USER DI FIELD TOKEN
        forgetPasswordDTO.setEmail(emailz);

        /*
            API GENERATE NEW TOKEN UNTUK LUPA PASSWORD PROSES SELANJUTNYA SETELAH PROSES KIRIM TOKEN YANG PERTAMA
        */
        String email = forgetPasswordDTO.getEmail();
        if(email == null || email.equals(""))
        {
            return "redirect:/api/check/logout";
        }

        objectMapper = userService.getNewToken(email,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))
        {
            return "redirect:/api/check/logout";
        }
        Boolean isSuccess = (Boolean) objectMapper.get("success");
        if(isSuccess)
        {
            model.addAttribute("forgetPwd",forgetPasswordDTO);
            mappingAttribute.setAttribute(model,objectMapper);
            return "auth/forget_pwd_verifikasi";
        }
        else
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("forgetPwd",forgetPasswordDTO);
            return "auth/login";
        }
    }
}
