package com.juaracoding.HRInformationSystem.controller;

/*
Created By IntelliJ IDEA 2022.1.3 (Community Edition)
Build #IC-221.5921.22, built on June 21, 2022
@Author JEJE a.k.a Jefri S
Java Developer
Created On 3/18/2023 00:42
@Last Modified 3/18/2023 00:42
Version 1.0
*/

import com.juaracoding.HRInformationSystem.configuration.OtherConfig;
import com.juaracoding.HRInformationSystem.dto.DivisiDTO;
import com.juaracoding.HRInformationSystem.model.Divisi;
import com.juaracoding.HRInformationSystem.service.DivisiService;
import com.juaracoding.HRInformationSystem.utils.ConstantMessage;
import com.juaracoding.HRInformationSystem.utils.ManipulationMap;
import com.juaracoding.HRInformationSystem.utils.MappingAttribute;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/usrmgmnt")
public class DivisiController {

    private DivisiService divisiService;

    @Autowired
    private ModelMapper modelMapper;

    private Map<String,Object> objectMapper = new HashMap<String,Object>();
    private Map<String,String> mapSorting = new HashMap<String,String>();

    private List<Divisi> lsCPUpload = new ArrayList<Divisi>();

    private String [] strExceptionArr = new String[2];

    private MappingAttribute mappingAttribute = new MappingAttribute();

    @Autowired
    public DivisiController(DivisiService divisiService) {
        strExceptionArr[0] = "DivisiController";
        mapSorting();
        this.divisiService = divisiService;
    }

    private void mapSorting()
    {
        mapSorting.put("id","idDivisi");
        mapSorting.put("nama","namaDivisi");
        mapSorting.put("kode","kodeDivisi");
    }

    @GetMapping("/v1/divisi/new")
    public String createDivisi(Model model, WebRequest request)
    {
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        model.addAttribute("divisi", new DivisiDTO());
        return "divisi/create_divisi";
    }

    @GetMapping("/v1/divisi/edit/{id}")
    public String editDivisi(Model model, WebRequest request, @PathVariable("id") Long id)
    {
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        objectMapper = divisiService.findById(id,request);
        DivisiDTO divisiDTO = (objectMapper.get("data")==null?null:(DivisiDTO) objectMapper.get("data"));
        if((Boolean) objectMapper.get("success"))
        {
            DivisiDTO divisiDTOForSelect = (DivisiDTO) objectMapper.get("data");
            model.addAttribute("divisi", divisiDTO);
            return "divisi/edit_divisi";
        }
        else
        {
            model.addAttribute("divisi", new DivisiDTO());
            return "redirect:/api/usrmgmnt/v1/divisi/default";
        }
    }
    @PostMapping("/v1/divisi/new")
    public String newDivisi(@ModelAttribute(value = "divisi")
                            @Valid DivisiDTO divisiDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
    )
    {
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
            model.addAttribute("divisi",divisiDTO);

            return "divisi/create_divisi";
        }
        Boolean isValid = true;

        if(!isValid)
        {
            model.addAttribute("divisi",divisiDTO);
            return "divisi/create_divisi";
        }
        /* END OF VALIDATION */

        Divisi divisi = modelMapper.map(divisiDTO, new TypeToken<Divisi>() {}.getType());
        objectMapper = divisiService.saveDivisi(divisi,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            return "redirect:/api/check/logout";
        }

        if((Boolean) objectMapper.get("success"))
        {
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("message","DATA BERHASIL DISIMPAN");
            Long idDataSave = objectMapper.get("idDataSave")==null?1:Long.parseLong(objectMapper.get("idDataSave").toString());
            return "redirect:/api/usrmgmnt/v1/divisi/fbpsb/0/asc/idDivisi?columnFirst=id&valueFirst="+idDataSave+"&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        }
        else
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("divisi",new DivisiDTO());
            model.addAttribute("status","error");
            return "divisi/create_divisi";
        }
    }

    @PostMapping("/v1/divisi/edit/{id}")
    public String editDivisi(@ModelAttribute(value = "divisi")
                             @Valid DivisiDTO divisiDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
            , @PathVariable("id") Long id
    )
    {
        divisiDTO.setIdDivisi(id);//agar validasi bisa berjalan di form nya
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }

        System.out.println("BINDING " + bindingResult);

        /* START VALIDATION */
        if(bindingResult.hasErrors())
        {
            model.addAttribute("divisi",divisiDTO);
            return "divisi/edit_divisi";
        }
        Boolean isValid = true;

        if(!isValid)
        {

            model.addAttribute("divisi",divisiDTO);
            return "divisi/edit_divisi";
        }
        /* END OF VALIDATION */

        Divisi divisi = modelMapper.map(divisiDTO, new TypeToken<Divisi>() {}.getType());
        objectMapper = divisiService.updateDivisi(id,divisi,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            return "redirect:/api/check/logout";
        }

        if((Boolean) objectMapper.get("success"))
        {
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("divisi",new DivisiDTO());
            return "redirect:/api/usrmgmnt/v1/divisi/fbpsb/0/asc/idDivisi?columnFirst=id&valueFirst="+id+"&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        }
        else
        {
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("divisi",new DivisiDTO());
            return "divisi/edit_divisi";
        }
    }


    @GetMapping("/v1/divisi/default")
    public String getDefaultData(Model model,WebRequest request)
    {
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        Pageable pageable = PageRequest.of(0,5, Sort.by("idDivisi"));
        objectMapper = divisiService.findAllDivisi(pageable,request);
        mappingAttribute.setAttribute(model,objectMapper,request);

        model.addAttribute("divisi",new DivisiDTO());
        model.addAttribute("sortBy","idDivisi");
        model.addAttribute("currentPage",1);
        model.addAttribute("asc","asc");
        model.addAttribute("columnFirst","");
        model.addAttribute("valueFirst","");
        model.addAttribute("sizeComponent",5);
        return "/divisi/divisi";
    }

    @GetMapping("/v1/divisi/fbpsb/{page}/{sort}/{sortby}")
    public String findByDivisi(
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
        sortzBy = sortzBy==null?"idDivisi":sortzBy;
        Pageable pageable = PageRequest.of(pagez==0?pagez:pagez-1,Integer.parseInt(sizeComponent.equals("")?"5":sizeComponent), sortz.equals("asc")?Sort.by(sortzBy):Sort.by(sortzBy).descending());
        objectMapper = divisiService.findByPage(pageable,request,columnFirst,valueFirst);
        mappingAttribute.setAttribute(model,objectMapper,request);
        model.addAttribute("divisi",new DivisiDTO());
        model.addAttribute("currentPage",pagez==0?1:pagez);
        model.addAttribute("sortBy", ManipulationMap.getKeyFromValue(mapSorting,sortzBy));
        model.addAttribute("columnFirst",columnFirst);
        model.addAttribute("valueFirst",valueFirst);
        model.addAttribute("sizeComponent",sizeComponent);

        return "/divisi/divisi";
    }


    @GetMapping("/v1/divisi/delete/{id}")
    public String deleteDivisi(Model model, WebRequest request, @PathVariable("id") Long id)
    {
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            mappingAttribute.setAttribute(model,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        objectMapper = divisiService.deleteDivisi(id,request);
        mappingAttribute.setAttribute(model,objectMapper);//untuk set session
        model.addAttribute("divisi", new DivisiDTO());
        return "redirect:/api/usrmgmnt/v1/divisi/default";
    }
}
