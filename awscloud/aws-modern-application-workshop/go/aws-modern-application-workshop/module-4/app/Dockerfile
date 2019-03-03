FROM golang:1.8

EXPOSE 8080

WORKDIR /go/src/app
COPY ./service/ .

RUN echo Fetching project dependencies
RUN go get -d -v

RUN echo Building and installing Mystical Mysfits Service
RUN go install -v

RUN echo Starting the Go service...
CMD ["app"]
