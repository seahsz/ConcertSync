package vttp.server.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    @Value("${do.space.bucket}")
    private String bucketName;

    @Value("${do.space.endpoint}")
    private String endPoint;

    @Autowired
    private S3Client s3Client;

    // returns the URL to fetch the image
    public String uploadFile(byte[] fileBytes, String originalFileName) throws Exception {
        String fileName = UUID.randomUUID() + "_" + originalFileName;
        PutObjectRequest putObjReq = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build();
        s3Client.putObject(putObjReq, RequestBody.fromBytes(fileBytes));
        return endPoint + "/" + bucketName + "/" + fileName;
    }

    // deleteFile(fileName)
    public void deleteFile(String fileName) {
        DeleteObjectRequest deleteObjReq = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build();
        s3Client.deleteObject(deleteObjReq);
    }
    
}
