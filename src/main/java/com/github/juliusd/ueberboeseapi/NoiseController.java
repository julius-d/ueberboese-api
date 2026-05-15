package com.github.juliusd.ueberboeseapi;

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

  /**
   * Handles the speaker's initial registration call. Never-paired devices call GET
   * /?serialnumber=<SN> to discover their accountId. We return the serial number itself as the
   * accountId so the speaker stores it as margeAccountUUID and then calls GET
   * /streaming/account/{id}/full to fetch its sources.
   */
  @GetMapping(
      value = "/",
      params = "serialnumber",
      produces = "application/vnd.bose.streaming-v1.2+xml")
  public ResponseEntity<byte[]> indexWithSerialNumber(
      @RequestParam("serialnumber") String serialNumber) {
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<account id=\""
            + serialNumber
            + "\">"
            + "<accountStatus>ACTIVE</accountStatus>"
            + "<mode>global</mode>"
            + "<preferredLanguage>en</preferredLanguage>"
            + "</account>";
    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .body(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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
