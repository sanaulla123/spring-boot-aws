package info.sanaulla.s3.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3ObjectList {
    List<S3Object> objects;
    String markerKey;
    boolean truncated;
}
