package com.github.juliusd.ueberboeseapi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class XmlMessageConverterConfig implements WebMvcConfigurer {

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    MappingJackson2XmlHttpMessageConverter xmlConverter =
        new MappingJackson2XmlHttpMessageConverter();

    // Use the primary ObjectMapper bean we configured
    xmlConverter.setObjectMapper(xmlObjectMapper());

    // Add support for the custom Bose XML content type
    List<MediaType> supportedMediaTypes = new ArrayList<>(xmlConverter.getSupportedMediaTypes());
    supportedMediaTypes.add(MediaType.parseMediaType("application/vnd.bose.streaming-v1.2+xml"));
    xmlConverter.setSupportedMediaTypes(supportedMediaTypes);

    converters.add(0, new ByteArrayHttpMessageConverter());
    converters.add(1, xmlConverter);
  }

  public static ObjectMapper xmlObjectMapper() {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
    xmlMapper.enable(ToXmlGenerator.Feature.WRITE_STANDALONE_YES_TO_XML_DECLARATION);
    xmlMapper.registerModule(new JavaTimeModule());
    xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Create a custom module for OffsetDateTime serialization
    SimpleModule module = new SimpleModule();
    module.addSerializer(
        OffsetDateTime.class,
        new JsonSerializer<OffsetDateTime>() {
          @Override
          public void serialize(
              OffsetDateTime value, JsonGenerator gen, SerializerProvider serializers)
              throws IOException {
            // Format the date with explicit +00:00 timezone format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            String formattedDate = value.format(formatter);
            String timezone =
                value.getOffset().toString().equals("Z") ? "+00:00" : value.getOffset().toString();
            gen.writeString(formattedDate + timezone);
          }
        });
    xmlMapper.registerModule(module);

    return xmlMapper;
  }
}
