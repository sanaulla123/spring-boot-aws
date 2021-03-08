package info.sanaulla.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${app.aws-region}")
    private String amazonRegion;

    @Value("${app.aws-profile-name}")
    private String awsProfileName;

    @Bean
    public S3Client s3Client(){
        Region region = Region.of(amazonRegion);
        S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider
                        .builder()
                        /*
                        I have named my AWS profile as sanaulla.
                        If you havent provided a profile name while configuring CLI then you can remove the below line
                        If you have a different profile name then you can update it here accordingly.
                         */
                        .profileName(awsProfileName)
                        .build()
                )
                .build();
        return s3;
    }

    @Bean
    public S3Presigner s3Presigner(){
        S3Presigner s3Presigner = S3Presigner.builder()
                .credentialsProvider(
                        DefaultCredentialsProvider
                                .builder()
                                .profileName(awsProfileName)
                                .build())
                .build();

        return s3Presigner;
    }
}
