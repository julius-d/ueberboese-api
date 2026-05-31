package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.dtos.AccountResponseApiDto;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class NoiseController {

  @GetMapping(
      value = "/",
      params = "serialnumber",
      produces = "application/vnd.bose.streaming-v1.2+xml")
  public ResponseEntity<AccountResponseApiDto> indexWithSerialNumber(
      @RequestParam("serialnumber") String serialNumber) {
    AccountResponseApiDto account =
        new AccountResponseApiDto()
            .id(serialNumber)
            .accountStatus("ACTIVE")
            .mode("global")
            .preferredLanguage("en");
    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .body(account);
  }

  @GetMapping("/")
  public ResponseEntity<Void> index(HttpServletRequest request) {
    String queryString = request.getQueryString();
    String fullUrl = queryString != null ? "/?" + queryString : "/";
    log.info("{} requested", fullUrl);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/favicon.ico")
  public ResponseEntity<byte[]> favicon() throws IOException {
    ClassPathResource resource = new ClassPathResource("static/icons/favicon.ico");
    byte[] content;
    try (InputStream inputStream = resource.getInputStream()) {
      content = inputStream.readAllBytes();
    }
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("image/x-icon"));
    return ResponseEntity.ok().headers(headers).body(content);
  }
}
