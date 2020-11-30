from app import app
import json
from flask import jsonify, request, make_response, render_template, redirect
import requests
from urllib.parse import quote
import csv
from app.spell import Spell
import re

@app.route("/", methods=["GET", "POST"])
def index():
    return render_template("public/index.html")

@app.route("/autocomplete", methods=["POST"])
def autocomplete():
    try:
        obj = request.json
        query = obj.get("q")

        response = []

        print("here")
        url = "http://localhost:8983/solr/foxnews/suggest?q=" + quote(query)
        suggestions = requests.get(url).json()

        suggestions = suggestions.get("suggest")
    
        suggestions = suggestions["suggest"]
        arr = []

        term = suggestions[query]
        for item in term["suggestions"]:
            arr.append(item["term"])
        return make_response(jsonify({"suggestions": arr}), 200)
    except Exception as e:
        return make_response(jsonify({"suggestions": []}), 200)

@app.route("/search", methods=["POST"])
def search():
    # Get request object
    obj = request.json
    query = obj.get("search")
    algo = obj.get("algo")
    category = obj.get("category")
    resubmit = obj.get("reSubmit")

    response_obj = {"data":[]}

    if query and not query == "":
        q = query.lower()
        misspelling = q

        # Autocomplete section
        isCorrectSpelling = False
        if not resubmit:
            isCorrectSpelling, q = auto_correct(q)
        else:
            isCorrectSpelling = True

        if category != "None":
            q += " AND prism_section:" + category

        url = "http://localhost:8983/solr/foxnews/select?q=" + quote(q)
        if algo == "pageRank":
            url += "&sort=pageRankFile desc"

        solr_obj = requests.get(url).json()
        response_obj = parse_json(solr_obj)

        if not isCorrectSpelling:
            response_obj["correctSpelling"] = q
            response_obj["misSpelling"] = misspelling
    return make_response(jsonify(response_obj), 200)

def auto_correct(words):
    '''
    :param words: string of query words
    '''
    correction = []
    arr = words.split()
    spell = Spell("big.txt")

    for word in arr:
        if len(word.split(".")) == 2:
            if word.split(".")[0].isdigit() and word.split(".")[1].isdigit():
                correction.append(word)
        else:
            correction.append(spell.correction(word))
    return correction == arr, " ".join(correction)

def get_file_to_url_object():
    file_to_url_obj = {}
    with open("FOXNEWS/URLtoHTML_fox_news.csv") as file:
        csv_file = csv.reader(file, delimiter=",")
        next(csv_file, None)
        for row in csv_file:
            file_to_url_obj[row[0]] = row[1]
        file.close()
    return file_to_url_obj

def parse_json(obj):
    data = {"data": []}

    response = obj.get("response")
    docs = response.get("docs")

    file_to_url_obj = get_file_to_url_object()

    if docs:
        for doc in docs:
            id = doc.get("id")
            title = doc.get("title")
            description = doc.get("description")
            url = doc.get("og_url")

            if not description:
                description = ["N/A"]
            
            if not url:
                key = id.split("/")[-1]
                url = [file_to_url_obj[key]]
            
            temp_obj = {
                "id": id.strip(),
                "title": title[0].strip(),
                "url": url[0].strip(),
                "description": description[0].strip(),
            }
            data["data"].append(temp_obj)
    return data
            