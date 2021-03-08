package info.sanaulla.s3.controller.rest;

import info.sanaulla.s3.service.S3ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/objects")
public class S3ObjectAPIController {

    @Autowired S3ObjectService s3ObjectService;
    @Value("${app.s3-bucket-name}") String s3BucketName;

    @GetMapping
    public ResponseEntity<?> getObjectVersions(){

        return ResponseEntity.ok(s3ObjectService.listObjectVersions());
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getObjectVersions(
            @RequestParam(required = false) String markerKey,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(s3ObjectService.listObjects(markerKey, size));
    }

    @GetMapping("/{key}/versions/{versionId}")
    public ResponseEntity<StreamingResponseBody> getObjectVersionContent(
            @PathVariable String key,
            @PathVariable String versionId) {
        HttpHeaders respHeaders = new HttpHeaders();

        ResponseInputStream<GetObjectResponse> objectVersionContent =
                s3ObjectService.getObjectVersionContent(key, versionId);

        GetObjectResponse objectResponse = objectVersionContent.response();

        respHeaders.add(HttpHeaders.CONTENT_TYPE, objectResponse.contentType());

        respHeaders.setContentLength(objectResponse.contentLength());

        respHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + key);

        StreamingResponseBody resource = outputStream -> {
            objectVersionContent.transferTo(outputStream);
        };

        return new ResponseEntity<>(resource, respHeaders, HttpStatus.OK);
    }

    @GetMapping("/{key}/versions/{versionId}/presigned")
    public void getObjectVersionContentPresigned(
            @PathVariable String key,
            @PathVariable String versionId,
            HttpServletResponse response
    ) throws IOException {
        URL presignedUrl = s3ObjectService.getObjectVersionPresignedUrlToRead(key, versionId);
        response.sendRedirect(presignedUrl.toString());
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<?> deleteObject(@PathVariable String key){
        s3ObjectService.deleteObject(key);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{key}/versions/{versionId}")
    public ResponseEntity<?> deleteObjectVersion(
            @PathVariable String key, @PathVariable String versionId){
        s3ObjectService.deleteObjectVersion(key, versionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> uploadedFiles) throws IOException {
        Map<String, List<String>> response = s3ObjectService.uploadFile(uploadedFiles);
        return ResponseEntity.ok(response);
    }
}
