package main

import (
    "strconv"

    "github.com/aws/aws-sdk-go/aws"
    "github.com/aws/aws-sdk-go/aws/session"
    "github.com/aws/aws-sdk-go/service/dynamodb"
    "github.com/aws/aws-sdk-go/service/dynamodb/dynamodbattribute"

    "flag"
    "fmt"
    "io"
    "log"
    "os" // for logging in main routine
)

// Info is for logging
var Info *log.Logger

// Outformat holds the output format
type Outformat int

const (
    HTML   Outformat = iota // 0
    JSON                    // 1
    STRING                  // 2
)

var defaultFormat = JSON

// Init initializes the logger and output format
func Init(infoHandle io.Writer, f Outformat) {
    Info = log.New(infoHandle,
        "INFO: ",
        log.Ldate|log.Ltime|log.Lshortfile)

    switch f {
    case HTML:
        defaultFormat = HTML
        return

    case JSON:
        defaultFormat = JSON
        return

    case STRING:
        defaultFormat = STRING
        return
    }
}

// Mysfit is a value returned by query
type Mysfit struct {
    MysfitId        string `json:"MysfitId"`
    Name            string `json:"Name"`
    Species         string `json:"Species"`
    Description     string `json:"Description"`
    Age             int    `json:"Age"`
    GoodEvil        string `json:"GoodEvil"`
    LawChaos        string `json:"LawChaos"`
    ThumbImageUri   string `json:"ThumbImageUri"`
    ProfileImageUri string `json:"ProfileImageUri"`
    Likes           int    `json:"Likes"`
    Adopted         bool   `json:"Adopted"`
}

func (m Mysfit) toString() string {
    output := ""

    output += "MysfitId:        " + m.MysfitId + "\n"
    output += "Name:            " + m.Name + "\n"
    output += "Species:         " + m.Species + "\n"
    output += "Description:     " + m.Description + "\n"
    output += "Age:             " + strconv.Itoa(m.Age) + "\n"
    output += "GoodEvil:        " + m.GoodEvil + "\n"
    output += "LawChaos:        " + m.LawChaos + "\n"
    output += "ThumbImageUri:   " + m.ThumbImageUri + "\n"
    output += "ProfileImageUri: " + m.ProfileImageUri + "\n"
    output += "Likes:           " + strconv.Itoa(m.Likes) + "\n"

    if m.Adopted {
        output += "Adopted:         True\n"
    } else {
        output += "Adopted:         False\n"
    }

    return output
}

func (m Mysfit) toHtml() string {
    output := ""

    output += "<table>\n"

    output += "  <tr><td>MysfitId</td><td>" + m.MysfitId + "</td></tr>\n"
    output += "  <tr><td>Name</td><td>" + m.Name + "</td></tr>\n"
    output += "  <tr><td>Species</td><td>" + m.Species + "</td></tr>\n"
    output += "  <tr><td>Description</td><td>" + m.Description + "</td></tr>\n"
    output += "  <tr><td>Age</td><td>" + strconv.Itoa(m.Age) + "</td></tr>\n"
    output += "  <tr><td>GoodEvil</td><td>" + m.GoodEvil + "</td></tr>\n"
    output += "  <tr><td>LawChaos</td><td>" + m.LawChaos + "</td></tr>\n"
    output += "  <tr><td>ThumbImageUri</td><td>" + m.ThumbImageUri + "</td></tr>\n"
    output += "  <tr><td>ProfileImageUri</td><td>" + m.ProfileImageUri + "</td></tr>\n"
    output += "<tr><td>Likes</td><td>" + strconv.Itoa(m.Likes) + "</td></tr>\n"

    if m.Adopted {
        output += "  <tr><td>Adopted</td><td>True</td></tr>\n"
    } else {
        output += "  <tr><td>Adopted</td><td>False</td></tr>\n"
    }

    output += "</table>\n"

    return output
}

// getItemStringAsJson(a, b) should return:
// "a": "b"
func getItemStringAsJson(name string, item string) string {
    return "\"" + name + "\": \"" + item + "\""
}

func getItemIntAsJson(name string, item int) string {
    return "\"" + name + "\": " + strconv.Itoa(item)
}

func getItemBoolAsJson(name string, item bool) string {
    if item {
        return "\"" + name + "\": true"
    } else {
        return "\"" + name + "\": false"
    }
}

func (m Mysfit) toJson() string {
    output := "{"

    output += getItemStringAsJson("mysfitId", m.MysfitId) + ", "
    output += getItemStringAsJson("name", m.Name) + ", "
    output += getItemStringAsJson("species", m.Species) + ", "
    output += getItemStringAsJson("description", m.Description) + ", "

    output += getItemIntAsJson("age", m.Age) + ", "

    output += getItemStringAsJson("goodEvil", m.GoodEvil) + ", "
    output += getItemStringAsJson("lawChaos", m.LawChaos) + ", "
    output += getItemStringAsJson("thumbImageUri", m.ThumbImageUri) + ", "
    output += getItemStringAsJson("profileImageUri", m.ProfileImageUri) + ", "

    output += getItemIntAsJson("likes", m.Likes) + ", "

    output += getItemBoolAsJson("adopted", m.Adopted)

    output += "}"

    return output
}

// Mysfits stores a list of Mysfit items
type Mysfits []Mysfit

func (ms Mysfits) toString() string {
    output := ""

    for _, m := range ms {
        output += m.toString() + "\n"
    }

    return output
}

func (ms Mysfits) toHtml() string {
    output := ""

    for _, m := range ms {
        output += m.toHtml() + "<p>&nbsp;</p>"
    }

    return output
}

func (ms Mysfits) toJson() string {
    length := len(ms)

    output := "{\"mysfits\": ["

    for i, m := range ms {
        output += m.toJson()

        if i < length-1 {
            output += ", "
        }
    }

    output += "]}"

    return output
}

// Get items as array of structs
func getItems(items []map[string]*dynamodb.AttributeValue) Mysfits {
    var mysfitList Mysfits

    err := dynamodbattribute.UnmarshalListOfMaps(items, &mysfitList)
    if err != nil {
        println("Got error unmarshalling items:")
        println(err.Error())
        return nil
    }

    return mysfitList
}

// getStringFromItems creates string from the items from a scan or query
func getStringFromItems(items []map[string]*dynamodb.AttributeValue) string {
    ms := Mysfits{}

    err := dynamodbattribute.UnmarshalListOfMaps(items, &ms)
    if err != nil {
        return ""
    }

    output := ""

    switch defaultFormat {
    case HTML:
        output = ms.toHtml()

    case JSON:
        output = ms.toJson()

    case STRING:
        output = ms.toString()
    }

    return output
}

// getJSONStringFromItems creates a JSON string from the items from a scan or query
func getJSONStringFromItems(items []map[string]*dynamodb.AttributeValue) string {
    ms := Mysfits{}

    err := dynamodbattribute.UnmarshalListOfMaps(items, &ms)
    if err != nil {
        return ""
    }

    return ms.toJson()
}

// GetAllMysfits gets all table items
func GetAllMysfits() string {
    // Create a DynamoDB client using our default credentials and region.
    sess := session.Must(session.NewSessionWithOptions(session.Options{
        SharedConfigState: session.SharedConfigEnable,
    }))

    // Create DynamoDB client
    svc := dynamodb.New(sess)

    // Retrieve all Mysfits from DynamoDB using the DynamoDB scan operation.
    // Note: The scan API can be expensive in terms of latency when a DynamoDB
    // table contains a high number of records and filters are applied to the
    // operation that require a large amount of data to be scanned in the table
    // before a response is returned by DynamoDB. For high-volume tables that
    // receive many requests, it is common to store the result of frequent/common
    // scan operations in an in-memory cache. DynamoDB Accelerator (DAX) or
    // use of ElastiCache can provide these benefits. But, because out Mythical
    // Mysfits API is low traffic and the table is very small, the scan operation
    // will suit our needs for this workshop.

    input := &dynamodb.ScanInput{
        TableName: aws.String("MysfitsTable"),
    }

    result, err := svc.Scan(input)
    if err != nil {
        Info.Print("Got error scanning table:")
        return ""
    }

    Info.Print(result.Items)

    output := getStringFromItems(result.Items)

    return output
}

// QueryMysfits gets only the specified items
func QueryMysfits(filter string, value string) string {
    Info.Println("Filter: " + filter)
    Info.Println("Value: " + value)

    // We only have two secondary indexes: GoodEvil(Index) and LawChaos(Index) and one primary index MysfitId(Index)
    if filter != "MysfitId" && filter != "GoodEvil" && filter != "LawChaos" {
        Info.Print("We only allow quering for MysfitId, GoodEvil, or LawChaos")
        return ""
    }

    // Create a DynamoDB client using our default credentials and region.
    sess := session.Must(session.NewSessionWithOptions(session.Options{
        SharedConfigState: session.SharedConfigEnable,
    }))

    // Create DynamoDB client
    svc := dynamodb.New(sess)

    // Use the DynamoDB scan API to retrieve mysfits from the table that are
    // equal to the selected filter values.
    input := &dynamodb.ScanInput{
        ExpressionAttributeValues: map[string]*dynamodb.AttributeValue{
            ":a": {
                S: aws.String(value),
            },
        },
        FilterExpression: aws.String(filter + " = :a"),
        TableName:        aws.String("MysfitsTable"),
    }

    result, err := svc.Scan(input)
    if err != nil {
        Info.Print("Got error getting item:")
        return ""
    }

    Info.Print(result.Items)

    output := getStringFromItems(result.Items)

    return output
}

// To test from command line change this and the top package name to main
func dummy() {
    filterPtr := flag.String("filter", "", "The table attribute to query")
    valuePtr := flag.String("value", "", "The value of the table attribute")
    flag.Parse()
    filter := *filterPtr
    value := *valuePtr

    var output string

    // Initialize logging
    Init(os.Stderr, JSON)

    if filter != "" && value != "" {
        fmt.Println("Getting filtered values")
        output = QueryMysfits(filter, value)
    } else {
        fmt.Println("Getting all values")
        output = GetAllMysfits()
    }

    // Convert []byte to string
    fmt.Print(output)
}
