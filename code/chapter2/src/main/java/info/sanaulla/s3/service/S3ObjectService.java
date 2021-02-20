package info.sanaulla.s3.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectVersionsIterable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class S3ObjectService {

    @Autowired S3Client s3Client;
    @Value("${app.s3-bucket-name}") String s3BucketName;

    public void uploadFile(List<MultipartFile> files) throws IOException{
        for (MultipartFile file: files){
            uploadFile(file);
        }
    }

    private void uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String keyName = FilenameUtils.normalizeNoEndSeparator(originalFilename);
        long fileSize = file.getSize();
        try {
            PutObjectResponse putObjectResponse = s3Client.putObject(PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(keyName)
                    .contentLength(fileSize)
                    .contentType(file.getContentType())
                    .build(), RequestBody.fromInputStream(file.getInputStream(), fileSize));
        }catch (SdkException ex){
            log.error("Error occurred while uploading {} to bucket {}", keyName, s3BucketName, ex);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while uploading the file to S3 bucket");
        }
    }

    public List<Map<String, Object>> getObjectVersions(String bucket, String key){
        ListObjectVersionsRequest listObjectVersionsRequest = ListObjectVersionsRequest.builder()
                .bucket(bucket)
                .prefix(key)
                .delimiter("")
                .build();
        ListObjectVersionsIterable listObjectVersionsResponses = s3Client.listObjectVersionsPaginator(listObjectVersionsRequest);

        return listObjectVersionsResponses.versions().stream().map(objectVersion -> {

            Map<String, Object> ovMap = new HashMap<>();
            ovMap.put("key", objectVersion.key());
            ovMap.put("version", objectVersion.versionId());
            ovMap.put("created-on", objectVersion.lastModified());
            ovMap.put("latest", objectVersion.isLatest());
            return ovMap;
        }).collect(Collectors.toList());
    }
}
