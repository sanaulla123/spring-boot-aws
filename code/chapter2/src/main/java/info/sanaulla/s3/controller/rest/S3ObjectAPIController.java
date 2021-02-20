package info.sanaulla.s3.controller.rest;

import info.sanaulla.s3.service.S3ObjectService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/objects")
public class S3ObjectAPIController {

    @Autowired S3ObjectService s3ObjectService;
    @Value("${app.s3-bucket-name}") String s3BucketName;

    @GetMapping("/{key}/versions")
    public ResponseEntity<?> getObjectVersions(@PathVariable String key){
        return ResponseEntity.ok(s3ObjectService.getObjectVersions(s3BucketName, key));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> uploadedFiles) throws IOException {
        s3ObjectService.uploadFile(uploadedFiles);
        return ResponseEntity.ok().build();
    }
}
