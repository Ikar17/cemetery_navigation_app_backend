package com.app.backend.happyPath;

import com.app.backend.controller.RouteController;
import com.app.backend.dto.RouteDTO;
import com.app.backend.model.Decedent;
import com.app.backend.model.Route;
import com.app.backend.model.User;
import com.app.backend.repository.DecedentRepository;
import com.app.backend.repository.RouteRepository;
import com.app.backend.repository.UserRepository;
import com.app.backend.service.JwtService;
import com.app.backend.utilsTest.RandomDataUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
public class RouteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DecedentRepository decedentRepository;

    @MockBean
    private RouteRepository routeRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private Storage storage;

    @MockBean
    private JwtService jwtService; // Dodano brakującą zależność

    @Autowired
    private ObjectMapper objectMapper;

    private Decedent testDecedent;
    private User testUser;
    private Route testRoute;

    @BeforeEach
    public void setUp() {
        testDecedent = new Decedent();
        testDecedent.setId(RandomDataUtils.randomInt(1, 1000));
        testDecedent.setName(RandomDataUtils.randomFirstName());
        testDecedent.setSurname(RandomDataUtils.randomLastName());

        testUser = new User();
        testUser.setEmail("testuser@example.com");

        testRoute = new Route();
        testRoute.setDecedent(testDecedent);
        testRoute.setUser(testUser);
        testRoute.setVideoName("test_video");
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    public void testUploadVideoHappyPath() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test_video.mp4", "video/mp4", "test content".getBytes());

        Mockito.when(decedentRepository.findById(testDecedent.getId())).thenReturn(Optional.of(testDecedent));
        Mockito.when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        Mockito.when(routeRepository.countByDecedent(testDecedent)).thenReturn(0L);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/decedent/route/upload")
                        .file(file)
                        .param("id", String.valueOf(testDecedent.getId()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        Mockito.verify(storage).create(any(BlobInfo.class), any(byte[].class));
        Mockito.verify(routeRepository).save(any(Route.class));
    }

    @Test
    @WithMockUser
    public void testGetRoutesByDecedentIdHappyPath() throws Exception {
        List<Route> routes = List.of(testRoute);
        URL signedUrl = new URL("http://signed-url");

        Mockito.when(routeRepository.findByDecedent_Id(testDecedent.getId())).thenReturn(routes);

        Mockito.when(storage.signUrl(
                        Mockito.any(BlobInfo.class),
                        Mockito.anyLong(),
                        Mockito.any(TimeUnit.class),
                        Mockito.any(Storage.SignUrlOption.class),
                        Mockito.any(Storage.SignUrlOption.class)))
                .thenReturn(signedUrl);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/decedent/route/{id}", testDecedent.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value(signedUrl.toString()));

        ArgumentCaptor<BlobInfo> captor = ArgumentCaptor.forClass(BlobInfo.class);
        Mockito.verify(storage).signUrl(
                captor.capture(),
                Mockito.eq(120L),
                Mockito.eq(TimeUnit.MINUTES),
                Mockito.any(Storage.SignUrlOption.class),
                Mockito.any(Storage.SignUrlOption.class));

        BlobInfo capturedBlobInfo = captor.getValue();
        assertNotNull(capturedBlobInfo);
        System.out.println("Captured BlobInfo: " + capturedBlobInfo);
    }


}
