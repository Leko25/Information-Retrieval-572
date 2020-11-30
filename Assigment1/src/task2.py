import os
import json
import pandas as pd
import collections
from urllib.parse import urlparse, ParseResult
from csv import writer

pd.set_option("display.max_rows", 5)

#TODO --> CSV metrics
def get_data_obj(google_file, result_file):
    google_obj = None
    result_obj = None

    with open(google_file, "r") as g_file, open(result_file, "r") as r_file:
        google_obj = json.load(g_file)
        result_obj = json.load(r_file)
    return google_obj, result_obj


def overlap(google_file, result_file):
    google_obj, result_obj = get_data_obj(google_file, result_file)
    
    overlap = {}

    for query in result_obj:
        if google_obj.get(query):
            google_lst = google_obj[query]
            result_lst = result_obj[query]

            google_keys = [getKey(url) for url in google_lst]
            result_keys = [getKey(url) for url in result_lst]

            intersect = list(set(google_keys).intersection(set(result_keys)))

            # overlap[query] = Fraction(len(intersect), len(google_keys))
            n = len(intersect)
            overlap[query] = (n, float((n/len(google_keys)) * 100))
    return overlap


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


def correlation(google_file, result_file):
    google_obj, result_obj = get_data_obj(google_file, result_file)

    spearman = {}

    for query in result_obj:
        if google_obj.get(query):
            google_lst = google_obj[query]
            result_lst = result_obj[query]

            google_keys = [getKey(url) for url in google_lst]
            result_keys = [getKey(url) for url in result_lst]

            intersect = list(set(google_keys).intersection(set(result_keys)))
            
            n = len(intersect)

            rank_google = {link: index + 1 for index, link in enumerate(google_keys)}
            rank_result = {link: index + 1 for index, link in enumerate(result_keys)}

            if n == 1:
                if rank_google[intersect[0]] == rank_result[intersect[0]]:
                    spearman[query] = 1.0
                else:
                    spearman[query] = 0.0
                continue
                        
            d2 = 0
            for i in intersect:
                d2 += (rank_google[i] - rank_result[i])**2
            
            if n == 0:
                rho = 0.0
            else:
                rho = 1.0 - float((6 * d2)/(n * (n**2 - 1)))
            spearman[query] = rho
    return spearman

if __name__ == "__main__":
    folder = "datasets"
    google_queries_file = "GoogleResults2.json"

    g_file = os.path.join(folder, google_queries_file)
    r_file = "hw1.json"

    overlap_obj = overlap(g_file, r_file)

    spearman_obj = correlation(g_file, r_file)

    queries = None

    with open("datasets/100QueriesSet2.txt", "r") as file:
        queries = file.readlines()

    d = collections.defaultdict(list)

    for index, query in enumerate(queries):
        query = query.rstrip()

        overlap = overlap_obj[query]

        d["Queries"].append("Query " + str(index + 1))
        d["Number of overlapping Results"].append(overlap[0])
        d["Percent Overlap"].append(overlap[1])
        d["Spearman Coefficient"].append(spearman_obj[query])
    
    df = pd.DataFrame(data=d)
    
    avg = {
        "Queries": "Averages",
        "Number of overlapping Results": df["Number of overlapping Results"].mean(),
        "Percent Overlap": df["Percent Overlap"].mean(),
        "Spearman Coefficient": df["Spearman Coefficient"].mean()
    }

    df = df.append(avg, ignore_index=True)
    
    if not os.path.exists("hw1.csv"):
        df.to_csv("hw1.csv", index=False)

