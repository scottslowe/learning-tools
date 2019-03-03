package com.example;

import org.springframework.stereotype.Service;


@Service
public class MythicalMysfitsService {

    private Mysfits allMysfits;

    public void save(Mysfits mysfits) {
        allMysfits = mysfits;
    }

    public Mysfits getAllMysfits() {
        return allMysfits;
    }

}
