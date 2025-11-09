package com.github.juliusd.ueberboeseapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UeberboeseApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(UeberboeseApiApplication.class, args);
  }
}
