package info.sanaulla.s3.model;

import lombok.Data;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.services.s3.model.DeleteMarkerEntry;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

import java.time.Instant;

@Data
public class S3ObjectVersion {
    String key;
    String versionId;
    boolean latest;
    Instant lastModified;
    String size;
    Boolean deleteMarker;

    public S3ObjectVersion(ObjectVersion objectVersion){
        this.key = objectVersion.key();
        this.versionId = objectVersion.versionId();
        this.latest = objectVersion.isLatest();
        this.lastModified = objectVersion.lastModified();
        this.size = DataSize.ofBytes(objectVersion.size()).toKilobytes()+"KB";
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
