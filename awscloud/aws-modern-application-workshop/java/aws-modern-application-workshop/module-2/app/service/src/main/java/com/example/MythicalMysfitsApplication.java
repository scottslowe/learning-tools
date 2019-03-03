package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class MythicalMysfitsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MythicalMysfitsApplication.class, args);
    }

    @Bean
    // Bean to read in the json file and and save it to the Mysfits list
    CommandLineRunner runner(MythicalMysfitsService mythicalMysfitsService) {
        return args -> {
            String jsonFile = "/json/mysfits-response.json";
            ObjectMapper jsonMapper = new ObjectMapper();
            InputStream inputStream = TypeReference.class.getResourceAsStream(jsonFile);

            try {
                Mysfits allMysfits = jsonMapper.readValue(inputStream, Mysfits.class);
                mythicalMysfitsService.save(allMysfits);
            }
            catch (IOException e) {
                System.out.println("Unable to get Mysfits: " + e.getMessage());
            }
        };
    }
}