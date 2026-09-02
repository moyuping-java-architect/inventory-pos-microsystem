package com.psi.system.integration;

import com.psi.system.SystemApplication;
import com.psi.system.dto.SysLoginDTO;
import com.psi.common.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SystemApplication.class)
@AutoConfigureMockMvc
public class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_success() throws Exception {
        SysLoginDTO loginDTO = new SysLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("admin");

        MvcResult result = mockMvc.perform(post("/psi/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userInfo").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("data").get("token").asText();
        
        assert token != null && !token.isEmpty() : "Token should not be empty";
        assert JwtUtils.parseToken(token) != null : "Token should be valid JWT";
    }

    @Test
    void login_failure_invalidCredentials() throws Exception {
        SysLoginDTO loginDTO = new SysLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrong-password");

        mockMvc.perform(post("/psi/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_failure_emptyUsername() throws Exception {
        SysLoginDTO loginDTO = new SysLoginDTO();
        loginDTO.setUsername("");
        loginDTO.setPassword("admin");

        mockMvc.perform(post("/psi/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_failure_emptyPassword() throws Exception {
        SysLoginDTO loginDTO = new SysLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("");

        mockMvc.perform(post("/psi/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_success_returnsAuthorizationHeader() throws Exception {
        SysLoginDTO loginDTO = new SysLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("admin");

        mockMvc.perform(post("/psi/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(header().string("Authorization", org.hamcrest.Matchers.startsWith("Bearer ")));
    }
}
