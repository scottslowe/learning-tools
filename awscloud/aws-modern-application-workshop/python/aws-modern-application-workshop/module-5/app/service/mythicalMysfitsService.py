from flask import Flask, jsonify, json, Response, request
from flask_cors import CORS
import mysfitsTableClient

app = Flask(__name__)
CORS(app)

# The service basepath has a short response just to ensure that healthchecks
# sent to the service root will receive a healthy response.
@app.route("/")
def healthCheckResponse():
    return jsonify({"message" : "Nothing here, used for health check. Try /mysfits instead."})

# Retrive mysfits from DynamoDB based on provided querystring params, or all
# mysfits if no querystring is present.
@app.route("/mysfits", methods=['GET'])
def getMysfits():

    filterCategory = request.args.get('filter')
    if filterCategory:
        filterValue = request.args.get('value')
        queryParam = {
            'filter': filterCategory,
            'value': filterValue
        }
        serviceResponse = mysfitsTableClient.queryMysfits(queryParam)
    else:
        serviceResponse = mysfitsTableClient.getAllMysfits()

    flaskResponse = Response(serviceResponse)
    flaskResponse.headers["Content-Type"] = "application/json"

    return flaskResponse

# retrieve the full details for a specific mysfit with their provided path
# parameter as their ID.
@app.route("/mysfits/<mysfitId>", methods=['GET'])
def getMysfit(mysfitId):
    serviceResponse = mysfitsTableClient.getMysfit(mysfitId)

    flaskResponse = Response(serviceResponse)
    flaskResponse.headers["Content-Type"] = "application/json"

    return flaskResponse

# increment the number of likes for the provided mysfit.
@app.route("/mysfits/<mysfitId>/like", methods=['POST'])
def likeMysfit(mysfitId):
    serviceResponse = mysfitsTableClient.likeMysfit(mysfitId)

    flaskResponse = Response(serviceResponse)
    flaskResponse.headers["Content-Type"] = "application/json"

    return flaskResponse

# indicate that the provided mysfit should be marked as adopted.
@app.route("/mysfits/<mysfitId>/adopt", methods=['POST'])
def adoptMysfit(mysfitId):
    serviceResponse = mysfitsTableClient.adoptMysfit(mysfitId)

    flaskResponse = Response(serviceResponse)
    flaskResponse.headers["Content-Type"] = "application/json"

    return flaskResponse

# Run the service on the local server it has been deployed to,
# listening on port 8080.
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8080)
