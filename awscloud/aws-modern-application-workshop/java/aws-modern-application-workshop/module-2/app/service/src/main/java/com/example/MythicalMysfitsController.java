package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletResponse;

@RestController
public class MythicalMysfitsController {

    @Autowired
    private MythicalMysfitsService mythicalMysfitsService = new MythicalMysfitsService();

    @RequestMapping(value="/mysfits", method=RequestMethod.GET)
    public Mysfits getMysfits(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        return mythicalMysfitsService.getAllMysfits();
    }


    @RequestMapping(value="/", method=RequestMethod.GET)
    public String healthCheckResponse() {
        return "Nothing here, used for health check. Try /mysfits instead.";
    }

}