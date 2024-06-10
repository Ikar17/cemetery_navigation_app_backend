package com.app.backend.happyPath;

import com.app.backend.controller.CemeteryController;
import com.app.backend.model.Cemetery;
import com.app.backend.repository.CemeteryRepository;
import com.app.backend.service.JwtService;
import com.app.backend.utilsTest.RandomDataUtils;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CemeteryController.class)
public class CemeteryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CemeteryRepository cemeteryRepository;

    @MockBean
    private JwtService jwtService;

    private Cemetery testCemetery;

    @BeforeEach
    public void setUp() {
        testCemetery = new Cemetery();
        testCemetery.setName(RandomDataUtils.randomString(10));
        testCemetery.setAddress(RandomDataUtils.randomString(10));
    }

    @Test
    @WithMockUser
    public void testAddNewCemeteryHappyPath() throws Exception {
        Mockito.when(cemeteryRepository.save(any(Cemetery.class))).thenReturn(testCemetery);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cemetery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + testCemetery.getName() + "\", \"address\":\"" + testCemetery.getAddress() + "\"}")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    public void testGetAllCemeteriesHappyPath() throws Exception {
        List<Cemetery> cemeteryList = new ArrayList<>();
        cemeteryList.add(testCemetery);
        Mockito.when(cemeteryRepository.findAll()).thenReturn(cemeteryList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cemetery")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"name\":\"" + testCemetery.getName() + "\", \"address\":\"" + testCemetery.getAddress() + "\"}]"));
    }

    @Test
    @WithMockUser
    public void testGetCemeteryByIdHappyPath() throws Exception {
        Mockito.when(cemeteryRepository.findById(anyInt())).thenReturn(Optional.of(testCemetery));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cemetery/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"name\":\"" + testCemetery.getName() + "\", \"address\":\"" + testCemetery.getAddress() + "\"}"));
    }
}
