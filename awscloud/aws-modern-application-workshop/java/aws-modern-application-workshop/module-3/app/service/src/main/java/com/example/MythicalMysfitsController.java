package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletResponse;

@RestController
public class MythicalMysfitsController {

    @Autowired
    private MythicalMysfitsService mythicalMysfitsService = new MythicalMysfitsService();

    @RequestMapping(value="/mysfits", method=RequestMethod.GET)
    public Mysfits getMysfits(HttpServletResponse response,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "value", required = false) String value ) {

        response.addHeader("Access-Control-Allow-Origin", "*");

        Mysfits mysfits;

        if (filter != null)
            mysfits = mythicalMysfitsService.queryMysfitItems(filter, value);
        else
            mysfits = mythicalMysfitsService.getAllMysfits();

        return mysfits;
    }


    @RequestMapping(value="/", method=RequestMethod.GET)
    public String healthCheckResponse() {
        return "Nothing here, used for health check. Try /mysfits instead.";
    }

}