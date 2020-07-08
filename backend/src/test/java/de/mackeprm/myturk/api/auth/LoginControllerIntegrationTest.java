package de.mackeprm.myturk.api.auth;

import de.mackeprm.myturk.mturk.MturkApiService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class LoginControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MturkApiService mturkApiService;

    @Test
    public void loginRequestTriggersMturkApiCall() throws Exception {
        final JSONObject mockLoginRequest = new JSONObject();
        mockLoginRequest.put("awsAccessKeyId", "test");
        mockLoginRequest.put("awsSecretAccessKey", "test");
        mockMvc.perform(
                post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mockLoginRequest.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You can pass")));
        verify(mturkApiService,times(2)).login(any(),eq("test"), eq("test"));
    }

    @Test
    public void incompleteLoginDataReturnsError() throws Exception {
        mockMvc.perform(
                post("/api/v1/login"))
                .andExpect(status().isBadRequest());

        final JSONObject accessKeyOnlyRequets = new JSONObject();
        accessKeyOnlyRequets.put("awsAccessKeyId", "test");
        mockMvc.perform(
                post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accessKeyOnlyRequets.toString()))
                .andExpect(status().isBadRequest());

        final JSONObject secretAccessKeyOnly = new JSONObject();
        secretAccessKeyOnly.put("awsSecretAccessKey", "test");
        mockMvc.perform(
                post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secretAccessKeyOnly.toString()))
                .andExpect(status().isBadRequest());
    }
}