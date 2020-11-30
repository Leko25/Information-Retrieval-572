import requests
from bs4 import BeautifulSoup
import json
import collections
from urllib.parse import unquote, quote, urlparse, ParseResult
import re
import os
import time

class Parser:
    def __init__(self):
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1.2 Safari/605.1.15",
            "Access-Control-Origin": "*",
            "Access-Control-Methods": "GET",
            "Access-Control-Allow-Headers": "Content-Type"
        }
    
    @staticmethod
    def preProcess(urls):
        def getKey(url):
            # Remove url scheme
            parsed_result = urlparse(url)
            parsed_result = ParseResult('', *parsed_result[1:]).geturl()

            i = 0

            while i < len(parsed_result) and parsed_result[i] == "/":
                i += 1
            
            parsed_result = parsed_result[i:]
            parsed_result = parsed_result.lower()

            if parsed_result[-1] == "/":
                parsed_result = parsed_result[:-1]

            return parsed_result

        ht = set()

        results = []
        for url in urls:
            key = getKey(url)
            if key not in ht:
                results.append(url)
            ht.add(key)
        return results
        
    
    def spider(self, url):
        req = requests.get(url, headers=self.headers)

        if req.status_code != requests.codes.ok:
            req.raise_for_status()
            exit(-1)
        bsObj = BeautifulSoup(req.content, "html.parser")
        links = bsObj.findAll("a", {"class": "ac-algo fz-l ac-21th lh-24"})

        results = set()

        for link in links:
            href = link.attrs["href"]

            if not re.search(r"RU=.*", href):
                results.add(unquote(url))
                continue
            href_arr = href.split("/")
            for param in href_arr:
                if re.search(r"RU=.*", param):
                    val = param.split("=")[-1]
                    results.add(unquote(val))
        results = Parser.preProcess(results)
        return results[:10] if len(results) > 10 else results

if __name__ == "__main__":
    folder = "datasets"
    queries = "100QueriesSet2.txt"

    file_path = os.path.join(folder, queries)

    parser = Parser()

    result = {}

    if os.path.isfile(file_path):
        query_list = None
        with open(file_path, "r") as file:
            query_list = file.readlines()
            base_url = "http://www.search.yahoo.com/search?p="

        for query in query_list:
            print("Searching for -> " + query)
            query = query.rstrip()
            url = base_url + quote(query) + "&n=20"
            links = parser.spider(url)
            result[query] = links

            # sleep for 20s
            time.sleep(20)

    if len(result) > 0:
        with open("hw1.json", "w") as file:
            json.dump(result, file)