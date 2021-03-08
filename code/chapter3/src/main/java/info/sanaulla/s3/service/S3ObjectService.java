package info.sanaulla.s3.service;

import info.sanaulla.s3.model.S3Object;
import info.sanaulla.s3.model.S3ObjectList;
import info.sanaulla.s3.model.S3ObjectVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.MapUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectVersionsIterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class S3ObjectService {

    @Autowired S3Client s3Client;
    @Autowired S3Presigner s3Presigner;

    @Value("${app.s3-bucket-name}") String s3BucketName;

    public Map<String, List<String>> uploadFile(List<MultipartFile> files) throws IOException{
        Map<String, List<String>> uploadResponse = new HashMap<>();
        List<String> errorMsgs = new ArrayList<>();
        List<String> successMsgs = new ArrayList<>();
        for (MultipartFile file: files){
            String originalFilename = file.getOriginalFilename();
            uploadFile(file);

            String.format("File %s copied successfully", originalFilename);
            String.format("File %s couldn't be copied", originalFilename);
        }

        uploadResponse.put("successful", successMsgs);
        uploadResponse.put("failure", errorMsgs);
        return uploadResponse;
    }

    private boolean uploadFile(MultipartFile file) throws IOException {
        //get the file name of the file uploaded by the client.
        String originalFilename = file.getOriginalFilename();

        /*
        Sometimes the uploaded file name can contain the complete path.
        So we will extract only the file name
         */
        String fileNamePartOnly = getFilename(originalFilename);
        long fileSize = file.getSize();
        try {

            //Build the request object required for communicating with S3 service
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(fileNamePartOnly)
                    .contentLength(fileSize)
                    .contentType(file.getContentType())
                    .build();

            //copy the file contents into request body via InputStream
            RequestBody requestBody =
                    RequestBody.fromInputStream(file.getInputStream(), fileSize);

            PutObjectResponse putObjectResponse =
                    s3Client.putObject(putObjectRequest, requestBody);

            return putObjectResponse.sdkHttpResponse().isSuccessful();

        }catch (SdkException ex){
            log.error("Error occurred while uploading {} to bucket {}",
                    fileNamePartOnly, s3BucketName, ex);
            return false;
        }

    }

    /**
     * Get the file name from the uploaded file.
     * Sometimes it is possible that the file name can contain the complete path.
     * Hence we will check for that and extract the actual file name.
     * @param originalFileName
     * @return
     */
    private String getFilename(String originalFileName){
        int systemFsIdx = originalFileName.lastIndexOf(System.getProperty("file.separator"));
        int backSlashFsIdx = originalFileName.lastIndexOf("\\");
        int fsIdx = Math.max(systemFsIdx, backSlashFsIdx);

        if (fsIdx > 0) originalFileName = originalFileName.substring(fsIdx+1);
        return originalFileName;
    }

    public Map<String, List<S3ObjectVersion>> listObjectVersions(){
        ListObjectVersionsRequest listObjectVersionsRequest =
                ListObjectVersionsRequest.builder()
                        .bucket(s3BucketName)
                        .build();

        ListObjectVersionsResponse listObjectVersionsResponse =
                s3Client.listObjectVersions(listObjectVersionsRequest);

        Stream<S3ObjectVersion> deleteMarkersStream =
                listObjectVersionsResponse.deleteMarkers()
                        .stream()
                        .map(dm -> new S3ObjectVersion(dm));

        Stream<S3ObjectVersion> objectVersionsStream =
                listObjectVersionsResponse.versions()
                        .stream()
                        .map(ov -> new S3ObjectVersion(ov));

        Map<String, List<S3ObjectVersion>> objectVersionsWithMarker =
                Stream.concat(objectVersionsStream, deleteMarkersStream)
                        .sorted(Comparator.comparing(S3ObjectVersion::getLastModified).reversed())
                        .collect(Collectors.groupingBy(ov -> ov.getKey()));

        return objectVersionsWithMarker;
    }

    public List<Map<String, Object>> listObjects(){
        ListObjectsRequest listObjectsRequest =
                ListObjectsRequest.builder()
                        .bucket(s3BucketName)
                        .build();
        ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjectsRequest);

        return listObjectsResponse.contents().stream()
                .map(s3Object -> {
                    Map<String, Object> s3Map = new HashMap<>();
                    s3Map.put("key", s3Object.key());
                    s3Map.put("lastModified", s3Object.lastModified());
                    s3Map.put("size", DataSize.ofBytes(s3Object.size()).toKilobytes()+"KB");
                    return s3Map;
                }).collect(Collectors.toList());
    }

    public S3ObjectList listObjects(String startWithKey, int size){
        ListObjectsRequest.Builder listObjectsRequestBuilder =
                ListObjectsRequest.builder()
                        .bucket(s3BucketName)
                        .maxKeys(size);
        if (StringUtils.isNotEmpty(startWithKey)){
            listObjectsRequestBuilder.marker(startWithKey);
        }

        ListObjectsRequest listObjectsRequest = listObjectsRequestBuilder.build();

        ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjectsRequest);
        List<S3Object> objects = listObjectsResponse.contents().stream()
                .map(s3Object -> new S3Object(s3Object.key(), s3Object.size(), s3Object.lastModified()))
                .collect(Collectors.toList());
        String nextMarker = "";
        if ( listObjectsResponse.isTruncated()) {
            S3Object lastObject = objects.get(objects.size() - 1);
            nextMarker = lastObject.getKey();
        }
        return new S3ObjectList(objects, nextMarker, listObjectsResponse.isTruncated());
    }

    public ResponseInputStream<GetObjectResponse> getObjectVersionContent(
            String key, String versionId){

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key(key)
                .versionId(versionId)
                .build();

        ResponseInputStream<GetObjectResponse> responseInputStream =
                s3Client.getObject(getObjectRequest);

        return responseInputStream;

    }

    public boolean deleteObjectVersion(String key, String versionId){
        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder()
                        .bucket(s3BucketName)
                        .key(key)
                        .versionId(versionId)
                        .build();

        DeleteObjectResponse deleteObjectResponse =
                s3Client.deleteObject(deleteObjectRequest);

        return deleteObjectResponse.sdkHttpResponse().isSuccessful();
    }

    public boolean deleteObject(String key){
        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder()
                        .bucket(s3BucketName)
                        .key(key)
                        .build();

        DeleteObjectResponse deleteObjectResponse =
                s3Client.deleteObject(deleteObjectRequest);

        return deleteObjectResponse.sdkHttpResponse().isSuccessful();
    }

    public boolean deleteObjects(List<String> keys){

        List<ObjectIdentifier> objectIdentifiers = keys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .collect(Collectors.toList());

        Delete delete = Delete.builder()
                .objects(objectIdentifiers)
                .build();

        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(s3BucketName)
                .delete(delete)
                .build();

        DeleteObjectsResponse deleteObjectsResponse
                = s3Client.deleteObjects(deleteObjectsRequest);
        return deleteObjectsResponse.sdkHttpResponse().isSuccessful();
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

    public URL getObjectVersionPresignedUrlToRead(String key, String versionId){
        try {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(key)
                    .versionId(versionId)
                    .responseContentDisposition("attachment; filename=" + key)
                    .build();

            GetObjectPresignRequest objectPresignRequest =
                    GetObjectPresignRequest.builder()
                            .getObjectRequest(objectRequest)
                            .signatureDuration(Duration.ofMinutes(5))
                            .build();

            PresignedGetObjectRequest presignedGetObjectRequest =
                    s3Presigner.presignGetObject(objectPresignRequest);


            return presignedGetObjectRequest.url();
        }finally {
            s3Presigner.close();
        }

    }

    public URL getPresignedUrlToPost(){
        try {
            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(s3BucketName)
                            .build();

            PutObjectPresignRequest objectPresignRequest =
                    PutObjectPresignRequest.builder()
                            .putObjectRequest(putObjectRequest)
                            .build();

            PresignedPutObjectRequest presignedPutObjectRequest =
                    s3Presigner.presignPutObject(objectPresignRequest);

            return presignedPutObjectRequest.url();

        }finally {
            s3Presigner.close();
        }
    }
}
