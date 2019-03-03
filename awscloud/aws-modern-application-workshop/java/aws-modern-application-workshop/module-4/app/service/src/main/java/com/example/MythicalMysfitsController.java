package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@RestController
public class MythicalMysfitsController {

    @Autowired
    private MythicalMysfitsService mythicalMysfitsService = new MythicalMysfitsService();

    @RequestMapping(value="/mysfits", method=RequestMethod.GET)
    public Mysfits getMysfits(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "value", required = false) String value ) {

        Mysfits mysfits;

        if (filter != null)
            mysfits = mythicalMysfitsService.queryMysfits(filter, value);
        else
            mysfits = mythicalMysfitsService.getAllMysfits();

        return mysfits;
    }


    @RequestMapping(value="/", method=RequestMethod.GET)
    public String healthCheckResponse() {
        return "Nothing here, used for health check. Try /mysfits instead.";
    }

    @RequestMapping(value="/mysfits/{mysfitId}", method=RequestMethod.GET)
    public Mysfit getMysfit(@PathVariable("mysfitId") String mysfitId) {

        Mysfit mysfit;
        mysfit = mythicalMysfitsService.getMysfit(mysfitId);

        return mysfit;
    }

    @RequestMapping(value="/mysfits/{mysfitId}/like", method=RequestMethod.POST)
    public ResponseEntity likeMysfit(@PathVariable("mysfitId") String mysfitId) {

        mythicalMysfitsService.likeMysfit(mysfitId);

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value="/mysfits/{mysfitId}/adopt", method=RequestMethod.POST)
    public ResponseEntity adoptMysfit(@PathVariable("mysfitId") String mysfitId) {

        mythicalMysfitsService.adoptMysfit(mysfitId);

        return new ResponseEntity(HttpStatus.OK);
    }

}