package info.sanaulla.s3.model;

import lombok.Data;
import software.amazon.awssdk.services.s3.model.DeleteMarkerEntry;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

import java.time.Instant;

@Data
public class S3ObjectVersion {
    String key;
    String versionId;
    boolean latest;
    Instant lastModified;
    Long size;
    Boolean deleteMarker;

    public S3ObjectVersion(ObjectVersion objectVersion){
        this.key = objectVersion.key();
        this.versionId = objectVersion.versionId();
        this.latest = objectVersion.isLatest();
        this.lastModified = objectVersion.lastModified();
        this.size = objectVersion.size();
        this.deleteMarker = false;
    }

    public S3ObjectVersion(DeleteMarkerEntry deleteMarkerEntry){
        this.key = deleteMarkerEntry.key();
        this.versionId = deleteMarkerEntry.versionId();
        this.latest = deleteMarkerEntry.isLatest();
        this.lastModified = deleteMarkerEntry.lastModified();
        this.deleteMarker = true;
    }
}
