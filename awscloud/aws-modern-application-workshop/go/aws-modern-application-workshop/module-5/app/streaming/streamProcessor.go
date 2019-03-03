package main

import (
    "github.com/aws/aws-lambda-go/events"
    "github.com/aws/aws-lambda-go/lambda"
    "encoding/json"
    "fmt"
    "net/http"
    "log"
    "io/ioutil"
    "os"
)

// Get Mysfit info from service API
func retrieveMysfit(mysfitId string) string {
    apiEndpoint := "REPLACE_ME_END_POINT"  // eg: https://ljqomqjzbf.execute-api.us-east-1.amazonaws.com/prod/

    resp, err := http.Get(apiEndpoint)
    if err != nil {
        log.Fatalln(err)
    }

    body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        log.Fatalln(err)
    }

    // Convert body to JSON
    var raw map[string]interface{}
    json.Unmarshal(body, &raw)
    raw["count"] = 1
    out, _ := json.Marshal(raw)

    return string(out)
}

// Lambda function handler
func handleRequest(evnt events.KinesisFirehoseEvent) (events.KinesisFirehoseResponse, error) {
    var response events.KinesisFirehoseResponse

    for _, record := range evnt.Records {
        fmt.Printf("RecordID: %s\n", record.RecordID)
        fmt.Printf("ApproximateArrivalTimestamp: %s\n", record.ApproximateArrivalTimestamp)

        // Transform data: ToUpper the data
        var transformedRecord events.KinesisFirehoseResponseRecord
        transformedRecord.RecordID = record.RecordID
        transformedRecord.Result = events.KinesisFirehoseTransformedStateOk
        // transformedRecord.Data = []byte(strings.ToUpper(string(record.Data)))

        response.Records = append(response.Records, transformedRecord)
    }

    fmt.Println(response)  // Or should this be log.Println???

    return response, nil
}

func main() {
    lambda.Start(handleRequest)
}
