import requests
import collections
from urllib.parse import quote
import matplotlib.pyplot as plt

queries = ["Cannes",
"Congress",
"Democrats",
"Patriot Movement",
"Republicans",
"Senate",
"Olympics 2020",
"Stock",
"Virus"]

page_rank_obj = collections.defaultdict(set)
lucene_obj = collections.defaultdict(set)

for query in queries:
    page_rank_url = "http://localhost:8983/solr/foxnews/select?q=" + query.lower() + \
        "&sort=pageRankFile desc&fl=og_url"

    lucene_url = "http://localhost:8983/solr/foxnews/select?q=" + query.lower() + "&fl=og_url"

    page_rank_response = requests.get(page_rank_url).json()
    lucene_response = requests.get(lucene_url).json()

    page_rank_doc = page_rank_response.get("response").get("docs")
    lucence_doc = lucene_response.get("response").get("docs")

    page_rank_url_set = {doc.get("og_url")[0] for doc in page_rank_doc if doc.get("og_url")}
    lucene_url_set = {doc.get("og_url")[0] for doc in lucence_doc if doc.get("og_url")}

    page_rank_obj[query] = page_rank_url_set
    lucene_obj[query] = lucene_url_set

overlap_obj = collections.defaultdict(int)

for query in queries:
    overlap_obj[query] = len(page_rank_obj[query].intersection(lucene_obj[query]))

data = [v for v in overlap_obj.values()]

plt.bar(queries, data)
plt.title("Overlap between PageRank and Lucene")
plt.xlabel("Queries")
plt.ylabel("Amount of Overlap")
plt.show()


