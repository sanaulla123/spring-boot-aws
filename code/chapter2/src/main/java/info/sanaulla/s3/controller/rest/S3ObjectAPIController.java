package info.sanaulla.s3.controller.rest;

import info.sanaulla.s3.service.S3ObjectService;
import org.apache.commons.io.IOUtils;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/objects")
public class S3ObjectAPIController {

    @Autowired S3ObjectService s3ObjectService;
    @Value("${app.s3-bucket-name}") String s3BucketName;

    @GetMapping("/{key}/versions")
    public ResponseEntity<?> getObjectVersions(@PathVariable String key){
        return ResponseEntity.ok(s3ObjectService.getObjectVersions(s3BucketName, key));
    }

    @GetMapping
    public ResponseEntity<?> getObjectsInBucket(){
        return ResponseEntity.ok(s3ObjectService.listObjects());
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getObjectsInBucket(
            @RequestParam(required = false) String markerKey,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(s3ObjectService.listObjects(markerKey, size));
    }

    @GetMapping("/{key}")
    public ResponseEntity<StreamingResponseBody> getObjectContent(@PathVariable String key) {
        HttpHeaders respHeaders = new HttpHeaders();
        StreamingResponseBody resource = outputStream -> {
            GetObjectResponse objectResponse =
                    s3ObjectService.getObjectContent(key, outputStream);
            respHeaders.add(HttpHeaders.CONTENT_TYPE, objectResponse.contentType());
            respHeaders.setContentLength(objectResponse.contentLength());
        };
        respHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment");
        return new ResponseEntity<>(resource, respHeaders, HttpStatus.OK);

    }

    @DeleteMapping("/{key}")
    public ResponseEntity<?> deleteObject(@PathVariable String key){
        s3ObjectService.deleteObject(key);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteObjects(@RequestBody List<String> keys){
        s3ObjectService.deleteObjects(keys);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> uploadedFiles) throws IOException {
        Map<String, List<String>> response = s3ObjectService.uploadFile(uploadedFiles);
        return ResponseEntity.ok(response);
    }
}
