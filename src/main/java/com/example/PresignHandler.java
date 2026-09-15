package com.example;

import java.util.Map;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class PresignHandler implements RequestHandler<Map<String, Object>, String> {
    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        // Implement your logic to generate a presigned URL here
        return "Presigned URL generated successfully!";
    }

}
