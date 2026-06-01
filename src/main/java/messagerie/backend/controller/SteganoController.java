package messagerie.backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import messagerie.backend.dto.SteganoDtos.SteganoTextRequest;
import messagerie.backend.dto.SteganoDtos.SteganoTextResponse;
import messagerie.backend.service.SteganoService;

@RestController
@RequestMapping("/api/stegano")
public class SteganoController {
    private final SteganoService steganoService;

    public SteganoController(SteganoService steganoService) {
        this.steganoService = steganoService;
    }

    @PostMapping(value = "/encode", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SteganoTextResponse encode(@RequestBody SteganoTextRequest request) {
        return steganoService.encode(request);
    }

    @PostMapping(value = "/decode", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SteganoTextResponse decode(@RequestBody SteganoTextRequest request) {
        return steganoService.decode(request);
    }

    @PostMapping(value = "/encode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> encodeImage(
        @RequestParam("image") MultipartFile image,
        @RequestParam("message") String message,
        @RequestParam(value = "key", required = false) String key
    ) {
        byte[] encodedImage = steganoService.encodeImage(image, message, key);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"securechat-stegano.png\"")
            .contentType(MediaType.IMAGE_PNG)
            .body(encodedImage);
    }

    @PostMapping(value = "/decode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SteganoTextResponse decodeImage(
        @RequestParam("image") MultipartFile image,
        @RequestParam(value = "key", required = false) String key
    ) {
        return steganoService.decodeImage(image, key);
    }
}
