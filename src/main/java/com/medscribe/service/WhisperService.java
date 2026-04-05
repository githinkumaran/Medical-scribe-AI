package com.medscribe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WhisperService {

    @Value("${whisper.service.url}")
    private String whisperServiceUrl;

    @Value("${audio.storage.path}")
    private String audioStoragePath;

    private final RestTemplate restTemplate;

    public String transcribe(MultipartFile audioFile) throws IOException {

        String fileName = System.currentTimeMillis()
                + "_" + audioFile.getOriginalFilename();
        Path savePath = Paths.get(audioStoragePath + fileName);
        Files.createDirectories(savePath.getParent());
        Files.write(savePath, audioFile.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                whisperServiceUrl + "/transcribe",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        Map<String, String> responseBody = response.getBody();
        return responseBody != null ? responseBody.get("transcript") : "";
    }
}