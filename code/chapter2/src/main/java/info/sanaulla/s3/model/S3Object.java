package info.sanaulla.s3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3Object {
    String key;
    long size;
    Instant lastModified;
}
