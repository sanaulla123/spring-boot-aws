package info.sanaulla.s3.service;

import info.sanaulla.s3.S3Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3BucketService {
    @Autowired
    S3Client s3Client;

    public List<S3Bucket> getBuckets(){
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
        return listBucketsResponse.buckets().stream()
                .map(b -> new S3Bucket(b.name(), b.creationDate()))
                .collect(Collectors.toList());
    }
}
