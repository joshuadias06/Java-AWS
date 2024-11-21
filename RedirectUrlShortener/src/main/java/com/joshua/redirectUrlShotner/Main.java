package com.joshua.redirectUrlShotner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final S3Client s3Client =S3Client.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {

        String pathParameters = (String) input.get("rawPath");
        String shortUrlCode = (String) pathParameters.replace("/", "");

        if(shortUrlCode == null || shortUrlCode.isEmpty()){
            throw new IllegalArgumentException("Invalid input: 'shortUrlCode' is required.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("url-shortener-storage-joshua")
                .key(shortUrlCode + "json")
                .build();

        InputStream s3ObjectStream;

        try{
            s3ObjectStream = s3Client.getObject(getObjectRequest);
        }catch(Exception e){
            throw new RuntimeException("Error fetching URL data from S3" + e.getMessage(), e);
        }

        OriginalUrlData urlData;
        try{
            urlData = objectMapper.readValue(s3ObjectStream, OriginalUrlData.class);
        }catch (Exception e){
            throw new RuntimeException("Error deserializing URL data from S3" + e.getMessage(), e);
        }

        long currentTime = System.currentTimeMillis() / 1000;
        Map<String, Object> response = new HashMap<>();
        if(urlData.getExpirationTime() < currentTime){
            response.put("statusCode", 410);
            response.put("body", "This URL has expired");
            return response;
        }
            response.put("statusCode", 302);
            Map<String, String> headers = new HashMap<>();
            headers.put("Location", urlData.getOriginalUrl());
            response.put("headers", headers);

            return response;

    }
}