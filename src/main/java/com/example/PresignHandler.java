package com.example;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class PresignHandler implements RequestHandler<Map<String, Object>, String> {
    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        // Implement your logic to generate a presigned URL here
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) event.get("headers");

        if (headers == null || !API_SECRET.equals(headers.get("x-api-key"))) {
            return "Process is Error!";
        }

        String createDate = headers.get("create-date");
        if (createDate == null || !KEY_DATE.matcher(createDate).matches()) {
            return "Process is Error! \n create-date is cause!";
        }

        String fileExt = headers.get("file-ext");
        if (fileExt == null) {
            return "Process is Error! \n file-ext is cause!";
        }
        fileExt = fileExt.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(fileExt)) {
            return "Process is Error! \n file-ext is cause!";
        }

        String key = "rensuke/" + createDate.substring(0, 4) + "/" + createDate.substring(4, 6) + "/" + createDate + "."
                + fileExt;

        return createPresignedUrl(BUCKET_NAME, key);
    }

    private static final S3Presigner PRESIGNER = S3Presigner.create();
    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private static final String API_SECRET = System.getenv("API_SECRET");
    private static final Pattern KEY_DATE = Pattern.compile("^\\d{8}_\\d{6}$");
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "heic", "mov", "mp4");

    /* Create a presigned URL to use in a subsequent PUT request */
    public String createPresignedUrl(String bucketName, String keyName) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // The URL expires in 10 minutes.
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = PRESIGNER.presignPutObject(presignRequest);
        String myURL = presignedRequest.url().toString();

        System.out.println("Presigned URL to upload a file to: " + myURL);
        System.out.println("HTTP method: " + presignedRequest.httpRequest().method());

        return presignedRequest.url().toString();
    }
}
