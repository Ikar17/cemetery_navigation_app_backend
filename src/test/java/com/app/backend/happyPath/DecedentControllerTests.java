package com.app.backend.happyPath;

import com.app.backend.controller.DecedentController;
import com.app.backend.dto.DecedentDTO;
import com.app.backend.model.Cemetery;
import com.app.backend.model.Decedent;
import com.app.backend.model.User;
import com.app.backend.repository.CemeteryRepository;
import com.app.backend.repository.DecedentRepository;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.JwtService;
import com.app.backend.utilsTest.RandomDataUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DecedentController.class)
public class DecedentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DecedentRepository decedentRepository;

    @MockBean
    private CemeteryRepository cemeteryRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private Decedent testDecedent;
    private DecedentDTO testDecedentDTO;

    @BeforeEach
    public void setUp() {
        testDecedent = new Decedent();
        testDecedent.setId(RandomDataUtils.randomInt(1, 1000));
        testDecedent.setName(RandomDataUtils.randomFirstName());
        testDecedent.setSurname(RandomDataUtils.randomLastName());
        testDecedent.setBirthDate(LocalDate.now().minusYears(RandomDataUtils.randomInt(10, 50)));
        testDecedent.setDeathDate(testDecedent.getBirthDate().plusYears(RandomDataUtils.randomInt(5, 20)));

        testDecedentDTO = new DecedentDTO();
        testDecedentDTO.setName(testDecedent.getName());
        testDecedentDTO.setSurname(testDecedent.getSurname());
        testDecedentDTO.setBirthDate(testDecedent.getBirthDate());
        testDecedentDTO.setDeathDate(testDecedent.getDeathDate());
        testDecedentDTO.setCemeteryId(RandomDataUtils.randomInt(1, 100));
    }
    @Test
    @WithMockUser
    public void testAddDecedentHappyPath() throws Exception {
        String decedentJson = objectMapper.writeValueAsString(testDecedentDTO);
        MockMultipartFile tombstoneImageFile = new MockMultipartFile("tombstoneImage", "test-image.jpg", "image/jpeg", "randomImageContent".getBytes(StandardCharsets.UTF_8));

        Cemetery testCemetery = new Cemetery();
        testCemetery.setId(testDecedentDTO.getCemeteryId());

        Mockito.when(cemeteryRepository.findById(testDecedentDTO.getCemeteryId())).thenReturn(Optional.of(testCemetery));
        Mockito.when(decedentRepository.save(Mockito.any(Decedent.class))).thenReturn(testDecedent);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/decedent/add")
                        .file(tombstoneImageFile)
                        .param("decedent", decedentJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated());

        Mockito.verify(cemeteryRepository).findById(testDecedentDTO.getCemeteryId());
        Mockito.verify(decedentRepository).save(any(Decedent.class));
    }



    @Test
    @WithMockUser
    public void testGetDecedentByIdHappyPath() throws Exception {
        Mockito.when(decedentRepository.findById(testDecedent.getId())).thenReturn(Optional.of(testDecedent));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/decedent/{id}", testDecedent.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    public void testUpdateDecedentByIdHappyPath() throws Exception {
        User testUser = new User();
        testUser.setEmail("testuser@example.com");

        testDecedent.setUser(testUser);

        Decedent updatedDecedent = new Decedent();
        updatedDecedent.setId(testDecedent.getId());
        updatedDecedent.setName("Updated Name");
        updatedDecedent.setSurname("Updated Surname");
        updatedDecedent.setBirthDate(LocalDate.of(1991, 1, 1));
        updatedDecedent.setDeathDate(LocalDate.of(2021, 1, 1));
        updatedDecedent.setUser(testUser);

        Mockito.when(decedentRepository.findById(testDecedent.getId())).thenReturn(Optional.of(testDecedent));
        Mockito.when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        Mockito.when(decedentRepository.save(any(Decedent.class))).thenReturn(updatedDecedent);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/decedent/{id}", testDecedent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDecedent))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.surname").value("Updated Surname"));
    }

    @Test
    @WithMockUser
    public void testGetDecedentsByKeywordsHappyPath() throws Exception {
        List<Decedent> decedents = List.of(testDecedent);

        Mockito.when(decedentRepository.findByNameContainingIgnoreCaseAndSurnameContainingIgnoreCase(
                        Mockito.eq(testDecedent.getName()),
                        Mockito.eq(testDecedent.getSurname())))
                .thenReturn(decedents);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/decedent/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"" + testDecedent.getName() + "\", \"surname\": \"" + testDecedent.getSurname() + "\" }")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(testDecedent.getName()))
                .andExpect(jsonPath("$[0].surname").value(testDecedent.getSurname()));
    }


}
