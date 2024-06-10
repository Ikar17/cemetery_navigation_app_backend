package com.app.backend.happyPath;

import com.app.backend.controller.UserController;
import com.app.backend.model.User;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.JwtService;
import com.app.backend.service.UserService;
import com.app.backend.utilsTest.RandomDataUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static com.app.backend.utilsTest.AccountType.USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(RandomUtils.nextInt(1, 100));
        testUser.setEmail(RandomDataUtils.randomEmail());
        testUser.setPassword(RandomDataUtils.randomPassword());
        testUser.setFirstName(RandomDataUtils.randomString(10));
        testUser.setLastName(RandomDataUtils.randomString(10));
        testUser.setAccountType(USER);
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    public void testGetUserIdHappyPath() throws Exception {
        Mockito.when(userService.findByEmail(anyString())).thenReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(testUser.getId())));
    }

    @Test
    @WithMockUser
    public void testDeleteUserHappyPath() throws Exception {
        Mockito.doNothing().when(userRepository).deleteById(anyInt());

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/delete/{id}", 1)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testGetAllUsersHappyPath() throws Exception {
        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        Mockito.when(userService.findAllUsers()).thenReturn(userList);

        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(testUser.getEmail()))
                .andExpect(jsonPath("$[0].firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$[0].lastName").value(testUser.getLastName()));
    }
}
