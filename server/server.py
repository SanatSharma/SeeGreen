import json
import re
from flask import Flask, request, redirect, g, render_template, jsonify
import requests
from watson_developer_cloud import AuthorizationV1
from watson_developer_cloud import NaturalLanguageUnderstandingV1
import watson_developer_cloud.natural_language_understanding.features.v1 as \
    features

app = Flask(__name__)

# IBM Bluemix credentials
NLU_URL = "https://gateway.watsonplatform.net/natural-language-understanding/api",
NLU_USERNAME="666623bf-33a6-48a3-befb-0e01904c1595"
NLU_PASSWORD="EO3wFQtWU1ai"

# Server side parameters
CLIENT_SIDE_URL = 'http://0991601a.ngrok.io'
PORT = 8080

# NLU
NLU = NaturalLanguageUnderstandingV1(
    version="2017-04-21",
    username="666623bf-33a6-48a3-befb-0e01904c1595",
    password="EO3wFQtWU1ai"
)

# MeaningCloud
MC_URL = "http://api.meaningcloud.com/class-1.1"
MC_HEADERS = {'content-type': 'application/x-www-form-urlencoded'}
MC_KEY = "3a24ec6fccad546a9f40e02d303ade5b"
MC_MODEL = "Recyclable-Compost-Trash"


@app.route("/")
def root():
    return render_template("index.html")

@app.route("/api/analyze", methods=['GET', 'POST'])
def analyze():
    if request.method == 'POST':
        content = request.get_json(force=True)
        query = content["text"]
    else:
        query = request.args.get("query")

    query = re.sub(r'[^\w\s]','',query)

    NLU_response = NLU.analyze(
        text=query,
        language="en",
        features=[features.Keywords()]
    )
    desc = ""
    item = ""
    if (not len(NLU_response["keywords"]) > 0):
        NLU_response["keywords"].append({"text": query})
    for i in NLU_response["keywords"]:
        item = i["text"]
        payload = "key={}&txt={}&model={}".format(MC_KEY, i["text"], MC_MODEL)
        MC_response = requests.request("POST", url=MC_URL, data=payload, headers=MC_HEADERS)
        MC_response = json.loads(MC_response.text)
        if (len(MC_response["category_list"]) > 0):
            if (MC_response["category_list"][0]["label"] == "Compostable"):
                desc = "Compostable"
                break;
            elif (MC_response["category_list"][0]["label"] == "Recyclable"):
                desc = "Recyclable"
                break;
    if not("Compostable" in desc or "Recyclable" in desc):
        desc = "Trash"
    item = re.sub(r'[^\w\s]','',item)
    callback = {
        "name": item,
        "value": desc
    }
    return jsonify(callback)


@app.route("/api/alexa_analyze", methods=['GET', 'POST'])
def alexa_analyze():
    if (request.method == 'POST'):
        content = request.get_json(force=True)
        name = content["request"]["intent"]["name"]
        if (name == "IsRecyclable"):
            query = content["request"]["intent"]["slots"]["recycle"]["value"]
        elif (name == "IsCompostable"):
            query = content["request"]["intent"]["slots"]["compost"]["value"]
        if (query):
            NLU_response = NLU.analyze(
                text=query,
                features=[features.Keywords()]
            )
            desc = str(NLU_response)
            for i in NLU_response["keywords"]:
                payload = "key={}&txt={}&model={}".format(MC_KEY, i["text"], MC_MODEL)
                MC_response = requests.request("POST", url=MC_URL, data=payload, headers=MC_HEADERS)
                MC_response = json.loads(MC_response.text)
                desc = ("No, you cannot recycle a " + query) if (name == "IsRecyclable") else ("No, you cannot compost a " + query)
                if (len(MC_response["category_list"]) > 0):
                    if (MC_response["category_list"][0]["label"] == "Compostable" and name == "IsCompostable"):
                        desc = "Yes, you can compost a {}".format(query)
                        break;
                    elif (MC_response["category_list"][0]["label"] == "Recyclable" and name == "IsRecyclable"):
                        desc = "Yes, you can recycle a {}".format(query)
                        break;
            alexa_response = {
                "version": "1.0",
                "response": {
                    "outputSpeech": {
                        "type": "PlainText",
                        "text": desc,
                    }
                }
            }
            return jsonify(alexa_response)
        else:
            desc = ("No, you cannot recycle a " + query) if (name == "IsRecyclable") else ("No, you cannot compost a " + query)
            alexa_response = {
                "version": "1.0",
                "response": {
                    "outputSpeech": {
                        "type": "PlainText",
                        "text": desc,
                    }
                }
            }
            return jsonify(alexa_response)

if __name__ == "__main__":
    app.run(debug=True, port=PORT)
