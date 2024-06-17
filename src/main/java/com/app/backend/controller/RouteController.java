package com.app.backend.controller;

import com.app.backend.dto.RouteDTO;
import com.app.backend.model.Decedent;
import com.app.backend.model.Route;
import com.app.backend.model.User;
import com.app.backend.repository.DecedentRepository;
import com.app.backend.repository.RouteRepository;
import com.app.backend.repository.UserRepository;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/decedent/route")
public class RouteController {

    @Autowired
    private DecedentRepository decedentRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Storage storage;
    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file,
                                              @RequestParam("id") Integer id) {
        try {
            Optional<Decedent> decedentOptional = decedentRepository.findById(id);
            if (decedentOptional.isEmpty() || file == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            Decedent decedent = decedentOptional.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> userOptional = userRepository.findByEmail(authentication.getName());
            if (userOptional.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            User user = userOptional.get();

            Long numberOfDecedentRoute = routeRepository.countByDecedent(decedent) + 1;
            String filename = decedent.getId().toString() + "_" + numberOfDecedentRoute + ".mp4";

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filename)
                    .setContentType("video/mp4")
                    .build();
            storage.create(blobInfo, file.getBytes());

            Route route = Route.builder()
                    .decedent(decedent)
                    .user(user)
                    .videoName(filename)
                    .build();
            routeRepository.save(route);

            return ResponseEntity.ok("");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<List<RouteDTO>> getRoutesByDecedentId(@PathVariable("id") Integer id) {
        try {
            List<Route> routes = routeRepository.findByDecedent_Id(id);
            List<RouteDTO> response = new ArrayList<>();

            TimeUnit unit = TimeUnit.MINUTES;
            int durationInMinutes = 120;

            for (Route route : routes) {
                RouteDTO routeDTO = new RouteDTO();
                BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, route.getVideoName())
                        .setContentType("video/mp4")
                        .build();

                URL signedUrl = storage.signUrl(
                        blobInfo,
                        durationInMinutes,
                        unit,
                        Storage.SignUrlOption.withV4Signature(),
                        Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                        Storage.SignUrlOption.withQueryParams(Map.of("response-content-disposition", "inline"))
                );

                routeDTO.setUrl(signedUrl.toString());

                response.add(routeDTO);
            }
            System.out.println("Done with sending video to frontend");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
