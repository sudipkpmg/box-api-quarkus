package gov.tn.dhs.quarkus.box.util;

import gov.tn.dhs.quarkus.box.model.PostTransactionRequest;
import gov.tn.dhs.quarkus.box.service.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordTransaction {

    private static Logger logger = LoggerFactory.getLogger(RecordTransaction.class);

    public static final String SERVICE_URL = "https://api-call-tracker-3scale-apicast-staging.apps.kpmgocp.stotenrhos.com:443/transaction?user_key=263a0141941df8e92d916138837a2ebe";
//    public static final String SERVICE_URL = "http://api-call-tracker-api-call-tracker.apps.kpmgocp.stotenrhos.com/transaction";
//    public static final String SERVICE_URL = "http://localhost:8080/transaction";

//    public static void recordTransaction(PostTransactionRequest postTransactionRequest) {
//        Unirest.config().verifySsl(false);
//        HttpResponse<JsonNode> response = Unirest.post(SERVICE_URL)
//                .header("Content-Type", "application/json")
//                .body(postTransactionRequest)
//                .asJson();
//        logger.info(JsonUtil.toJson(response));
//    }
//
//    public static void recordTransaction(
//            String apiName,
//            String apiType,
//            String verb,
//            String user_key
//    ) {
//        PostTransactionRequest postTransactionRequest = new PostTransactionRequest();
//        postTransactionRequest.setApiName(apiName);
//        postTransactionRequest.setApiType(apiType);
//        postTransactionRequest.setVerb(verb);
//        postTransactionRequest.setSubscriberId(user_key == null ? "sudip" : user_key);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date now = new Date();
//        String nowStr = simpleDateFormat.format(now);
//        postTransactionRequest.setRequestDateTime(nowStr);
//        Unirest.config().verifySsl(false);
//        HttpResponse<JsonNode> response = Unirest.post(SERVICE_URL)
//                .header("Content-Type", "application/json")
//                .body(postTransactionRequest)
//                .asJson();
//        logger.info(JsonUtil.toJson(response));
//    }

    public static void recordTransaction(
            String apiName,
            String apiType,
            String verb,
            String user_key
    ) {
        PostTransactionRequest postTransactionRequest = new PostTransactionRequest();
        postTransactionRequest.setApiName(apiName);
        postTransactionRequest.setApiType(apiType);
        postTransactionRequest.setVerb(verb);
        postTransactionRequest.setSubscriberId(user_key == null ? "6574c1909fe0da5a8ebf108c7c8fc0f6" : user_key);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String nowStr = simpleDateFormat.format(now);
        postTransactionRequest.setRequestDateTime(nowStr);
        String requestBody = JsonUtil.toJson(postTransactionRequest);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVICE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("response code = {}", response.statusCode());
            logger.info("response body = {}", response.body());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

}
