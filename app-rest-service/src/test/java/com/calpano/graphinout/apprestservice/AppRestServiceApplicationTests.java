package com.calpano.graphinout.apprestservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AppRestServiceApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class AppRestServiceApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void pingPong() throws Exception {
        mvc.perform(get("/api/status").contentType(MediaType.TEXT_PLAIN)) //
                .andExpect(status().isOk()) //
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)) //
                .andExpect(content().string("OK"));
    }


}
