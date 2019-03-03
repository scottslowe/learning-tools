package main

import (
    "fmt"
    "net/http"
    "os"
    "strings"
)

// CORS:
func setupResponse(w *http.ResponseWriter) {
    (*w).Header().Set("Content-Type", "text/html; charset=utf-8")
    (*w).Header().Set("Access-Control-Allow-Origin", "*")
    (*w).Header().Set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE")
    (*w).Header().Set("Access-Control-Allow-Headers", "Accept, Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization")
}

func getContentType() string {
    contentType := "application/json"

    switch DefaultFormat {
    case "JSON":
        Init(os.Stderr, JSON)
        contentType = "application/json"
    case "HTML":
        Init(os.Stderr, HTML)
        contentType = "application/html"
    case "TEXT":
        Init(os.Stderr, STRING)
        contentType = "text/html; charset=utf-8"
    default:
        Init(os.Stderr, JSON)
        contentType = "application/json"
    }

    return contentType
}

// Handle GET requests
func getHandler(w http.ResponseWriter, r *http.Request, t string) (string, string) {
    setupResponse(&w)
    
    // We handle (in local testing):
    // /mysfits                              returns all mysfits
    // /mysfits?filter=FILTER&value=VALUE    returns a mysfit where FILTER is has VALUE
    // /mysfits/{mysfitsId}                  returns a mysfit by their MysfitId

    var path = r.URL.Path

    // If just /, return simple message
    if path == "/" {
        // We must set the format to text, otherwise we get a JSON format error
        return "Nothing here, used for health check. Try /mysfits instead.", "TEXT"
    }

    // If just /mysfits, get them all
    if path == "/mysfits" {
        return GetAllMysfits(), t
    }

    // Did we get a filter request?
    filter := r.URL.Query().Get("filter")
    if filter != "" {
        fmt.Println("Got filter: " + filter)
        value := r.URL.Query().Get("value")
        if value != "" {
            fmt.Println("Got value: " + value)
            return QueryMysfits(filter, value), t
        }
    }

    // We have a path like: /mysfits/abc123
    // First make sure it's not /mysfits/abc123/xyz
    s := strings.Split(path, "/")

    // Splitting /mysfits/abc123 gives us:
    // s[0]: ""
    // s[1]: "mysfits"
    // s[2]: "abc123"

    if len(s) == 3 {
        id := s[2]
        return GetMysfit(id), t
    }

    // We must set the format to text, otherwise we get a JSON format error
    return "Got bad GET request", "TEXT"
}

// Handle POST requests
func postHandler(w http.ResponseWriter, r *http.Request, t string) (string, string) {
    setupResponse(&w)
    
    // We support:
    // /mysfits/<mysfitId>/like     increments the likes for mysfit with mysfitId
    // /mysfits/<mysfitId>/adopt    enables adopt for mysfit with mysfitId

    path := r.URL.Path

    s := strings.Split(path, "/")

    // Splitting /mysfits/abc123/adopt gives us:
    // s[0] == ""
    // s[1] == "mysfits"
    // s[2] == "abc123"
    // s[3] == "adopt"

    if len(s) == 4 {
        id := s[2]
        action := s[3]

        switch action {
        case "like":
            IncMysfitLikes(id)
            return "Incremented likes for " + id, "TEXT"
        case "adopt":
            SetMysfitAdopt(id)
            return "Enabled adoption for " + id, "TEXT"
        default:
            return "Unknown action: " + action, "TEXT"
        }
    }

    return "Unknown request", "TEXT"
}

// Handle everything here
func mainHandler(w http.ResponseWriter, r *http.Request) {
    setupResponse(&w)
    
    // Show path and method
    fmt.Println("")
    fmt.Println("In mainHandler")
    fmt.Println("Method: " + r.Method)
    fmt.Println("Path:   " + r.URL.Path)

    content := ""
    contentType := getContentType()

    // If GET, send it to getHandler
    switch r.Method {
    case "GET":
        content, contentType = getHandler(w, r, contentType)
    case "POST":
        content, contentType = postHandler(w, r, contentType)
    default:
        content = "Bad HTTP request method: " + r.Method
        contentType = "TEXT"
    }

    // Add content to web page
    body := []byte(content)
    w.Header().Set("Content-Type", contentType)
    w.Write(body)
}

// Defaults
var DefaultFormat = "JSON"
var DefaultPort = ":8088"

func main() {
    // Check environment
    port := os.Getenv("PORT")
    if port != "" {
        DefaultPort = port
    }

    format := os.Getenv("FORMAT")
    if format != "" {
        DefaultFormat = format
    }

    mux := http.NewServeMux()
    mux.Handle("/", http.HandlerFunc(mainHandler))
    http.ListenAndServe(DefaultPort, mux)

    fmt.Println("Running on: ")
    fmt.Println("http://localhost/" + port)
    fmt.Println("Use the following to get ALL mysfits:")
    fmt.Println("http://localhost/" + port + "/mysfits")
}
