from flask import Flask, jsonify, json, Response, request
from flask_cors import CORS

# A very basic API created using Flask that has two possible routes for requests.

app = Flask(__name__)
CORS(app)

# The service basepath has a short response just to ensure that healthchecks
# sent to the service root will receive a healthy response.
@app.route("/")
def healthCheckResponse():
    return jsonify({"message" : "Nothing here, used for health check. Try /mysfits instead."})

# The main API resource that the next version of the Mythical Mysfits website
# will utilize. It returns the data for all of the Mysfits to be displayed on
# the website.  Because we do not yet have any persistent storage available for
# our application, the mysfits are simply stored in a static JSON file. Which is
# read from the the filesystem, and directly used as the service response.
@app.route("/mysfits")
def getMysfits():

    # read the mysfits JSON from the listed file.
    response = Response(open("mysfits-response.json").read())

    # set the Content-Type header so that the browser is aware that the response
    # is formatted as JSON and our frontend JavaScript code is able to
    # appropriately parse the response.
    response.headers["Content-Type"]= "application/json"

    return response

# Run the service on the local server it has been deployed to,
# listening on port 8080.
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8080)
