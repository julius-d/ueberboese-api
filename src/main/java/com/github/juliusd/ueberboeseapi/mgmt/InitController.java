package com.github.juliusd.ueberboeseapi.mgmt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/mgmt/init")
public class InitController {

  @GetMapping(value = "/set-up-this-speaker.sh", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> setupScript() throws IOException {
    String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

    ClassPathResource resource = new ClassPathResource("templates/set-up-this-speaker.sh");
    String template = resource.getContentAsString(StandardCharsets.UTF_8);
    String script = template.replace("{{BASE_URL}}", baseUrl);

    return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(script);
  }
}
