package com.app.backend.service;

import com.app.backend.dto.DecedentDTO;
import com.app.backend.model.Decedent;
import com.app.backend.repository.DecedentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DecedentService {

    @Autowired
    private DecedentRepository decedentRepository;

    @Transactional(readOnly = true)
    public List<DecedentDTO> findByNameAndSurnameAndCity(String name, String surname, String city) {
        List<Decedent> decedents = decedentRepository.findByNameStartingWithIgnoreCaseAndSurnameStartingWithIgnoreCase(name, surname);
        if (!city.isEmpty()) {
            decedents = decedents.stream()
                    .filter(decedent -> decedent.getCemetery() != null && city.equals(decedent.getCemetery().getCity()))
                    .collect(Collectors.toList());
        }
        return decedents.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public DecedentDTO convertToDTO(Decedent decedent) {
        DecedentDTO dto = new DecedentDTO();
        dto.setId(decedent.getId());
        dto.setName(decedent.getName());
        dto.setSurname(decedent.getSurname());
        dto.setBirthDate(decedent.getBirthDate());
        dto.setDeathDate(decedent.getDeathDate());
        dto.setDescription(decedent.getDescription());
        dto.setLatitude(decedent.getLatitude());
        dto.setLongitude(decedent.getLongitude());
        dto.setCemeteryId(decedent.getCemetery().getId());
        dto.setUserId(decedent.getUser().getId());
        dto.setCity(decedent.getCemetery().getCity());

        if (decedent.getTombstoneImage() != null) {
            dto.setImageBase64(Base64.getEncoder().encodeToString(decedent.getTombstoneImage()));
        }

        return dto;
    }

}
