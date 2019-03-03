/*
 * The code to be used as an AWS Lambda function for processing real-time
 * user click records from Kinesis Firehose and adding additional attributes
 * to them before they are stored in Amazon S3.
 */

package com.amazonaws.lambda.streamprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import java.net.HttpURLConnection;
import java.net.URL;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.Result;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.Record;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LambdaFunctionHandler implements RequestHandler<KinesisFirehoseEvent, KinesisAnalyticsInputPreprocessingResponse> {

	/* Handler method invoked by Lambda with events
	 * This case includes a record from the Kinesis Firehose Delivery System.
	 */
    @Override
    public KinesisAnalyticsInputPreprocessingResponse handleRequest(KinesisFirehoseEvent event, Context context) {

        // Using the aws-lambda-java-libs library response object
        KinesisAnalyticsInputPreprocessingResponse response = new KinesisAnalyticsInputPreprocessingResponse();

        List<Record> transformedRecordsList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        /*
         * retrieve the list of records included with the event and loop through
         * them to retrieve the full list of mysfit attributes and add the additional
         * attributes that a hypothetical BI/Analyitcs team would like to analyze.
         */
        for (KinesisFirehoseEvent.Record record : event.getRecords()) {

            Record transformedRecord = new Record();

            // Setup fields required by Firehose
        	transformedRecord.setRecordId(record.getRecordId());
        	transformedRecord.setResult(Result.Ok);

            try {

                //Get string version of the record data and map to click record object
                String convertedData = new String(record.getData().array(), "UTF-8");
                ClickRecord updatedClickRecord = mapper.readValue(convertedData, ClickRecord.class);

                //Retrieve rest of mysfits info from the Mythical Mysfits service API
                Mysfit mysfit = retrieveMysfit(updatedClickRecord.getMysfitId(), context);

                //Set additional mysfit attributes in the updated record
                updatedClickRecord.setGoodevil(mysfit.getGoodevil());
                updatedClickRecord.setLawchaos(mysfit.getLawchaos());
                updatedClickRecord.setSpecies(mysfit.getSpecies());

                //printing results for testing
                context.getLogger().log("For Mysfit: " + updatedClickRecord.getMysfitId());
                context.getLogger().log("click record: " + mapper.writeValueAsString(updatedClickRecord));

                //Convert updated record to ByteBuffer expected by Kinesis Firehose
                byte[] clickByteArray = mapper.writeValueAsString(updatedClickRecord).getBytes("UTF-8");
                transformedRecord.setData(ByteBuffer.wrap(clickByteArray));

            }
            catch (JsonParseException e) {
                e.printStackTrace();
            }
            catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            //add the updated record to the record list
            transformedRecordsList.add(transformedRecord);
        }

        //update response with new records
        response.setRecords(transformedRecordsList);

        return response;
    }

    /*
     * Send a request to the Mysfits Service API that we have created in previous
     * modules to retrieve all of the attributes for the included MysfitId.
     */
    public Mysfit retrieveMysfit(String mysfitId, Context context) {

        String apiEndpoint = "REPLACE_ME_API_ENDPOINT" + "/mysfits/" + mysfitId; // eg: 'https://ljqomqjzbf.execute-api.us-east-1.amazonaws.com/prod/'

        Mysfit mysfit = new Mysfit();

        try {

            URL url = new URL(apiEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output = "";
            String line;
            while ((line = br.readLine()) != null) {
                output += line;
            }

            context.getLogger().log("Final output: " + output);

            // Map the response from the service API to a mysfit object
            mysfit = new ObjectMapper().readValue(output, Mysfit.class);

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

        return mysfit;

    }

}
