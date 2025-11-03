package com.github.juliusd.ueberboeseapi;

import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProviderApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceProvidersResponseApiDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UeberboeseController.class)
class UeberboeseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSourceProviders_integrationTest() throws Exception {
        mockMvc.perform(get("/streaming/sourceproviders")
                .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
                .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
                .header("User-agent", "Bose_Lisa/27.0.6"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.bose.streaming-v1.2+xml"))
                .andExpect(header().string("ETag", "\"1762017390476\""))
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET, POST, OPTIONS"))
                .andExpect(header().string("Access-Control-Expose-Headers", "Authorization"))
                .andExpect(content().contentType("application/vnd.bose.streaming-v1.2+xml"));
    }


}
