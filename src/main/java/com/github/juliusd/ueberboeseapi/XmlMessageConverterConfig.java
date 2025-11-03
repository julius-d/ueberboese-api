package com.github.juliusd.ueberboeseapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class XmlMessageConverterConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter();

        // Configure XML mapper for proper XML generation
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        xmlMapper.findAndRegisterModules(); // This will register JSR310 module for Java 8 time support
        xmlConverter.setObjectMapper(xmlMapper);

        // Add support for the custom Bose XML content type
        List<MediaType> supportedMediaTypes = new ArrayList<>(xmlConverter.getSupportedMediaTypes());
        supportedMediaTypes.add(MediaType.parseMediaType("application/vnd.bose.streaming-v1.2+xml"));
        xmlConverter.setSupportedMediaTypes(supportedMediaTypes);

        converters.add(0, xmlConverter); // Add at the beginning to ensure it's used first
    }
}