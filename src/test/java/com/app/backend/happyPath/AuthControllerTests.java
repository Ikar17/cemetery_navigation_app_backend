package com.app.backend.happyPath;

import com.app.backend.controller.AuthController;
import com.app.backend.dto.LoginDTO;
import com.app.backend.model.User;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.EmailService;
import com.app.backend.service.JwtService;
import com.app.backend.utilsTest.RandomDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    private User testUser;
    private String testJwtToken;
    private RandomDataUtils RandomDataUtil;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setEmail(RandomDataUtils.randomEmail());
        testUser.setPassword(RandomDataUtils.randomPassword());
        testUser.setFirstName(RandomDataUtils.randomFirstName());
        testUser.setLastName(RandomDataUtils.randomLastName());
        testJwtToken = RandomDataUtils.randomString(10);
    }

    @Test
    @WithMockUser
    public void testSignInHappyPath() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(testUser.getEmail());
        loginDTO.setPassword(testUser.getPassword());

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn(testJwtToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signin").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"" + testUser.getPassword() + "\"}").with(SecurityMockMvcRequestPostProcessors.csrf())).andDo(print()).andExpect(status().isOk()).andExpect(content().string(testJwtToken));
    }

    @Test
    @WithMockUser
    public void testSignUpHappyPath() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"password\",\"firstName\":\"" + testUser.getFirstName() + "\",\"lastName\":\"" + testUser.getLastName() + "\"}").with(SecurityMockMvcRequestPostProcessors.csrf())).andDo(print()).andExpect(status().isCreated()).andExpect(content().string("User registered successfully"));
    }
}
