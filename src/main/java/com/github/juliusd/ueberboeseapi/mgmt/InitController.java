package com.github.juliusd.ueberboeseapi.mgmt;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mgmt/init")
public class InitController {

  @GetMapping(value = "/set-up-this-speaker.sh", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> setupScript(HttpServletRequest request) throws IOException {
    String scheme = Optional.ofNullable(request.getHeader("X-Forwarded-Proto")).orElse("http");
    String host = request.getHeader("Host");
    String baseUrl = scheme + "://" + host;

    ClassPathResource resource = new ClassPathResource("templates/set-up-this-speaker.sh");
    String template = resource.getContentAsString(StandardCharsets.UTF_8);
    String script = template.replace("{{BASE_URL}}", baseUrl);

    return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(script);
  }
}
