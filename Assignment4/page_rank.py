import networkx as nx
import math

g = nx.read_edgelist("edgeList.txt", create_using=nx.DiGraph())

rank = nx.pagerank(g, alpha=0.85, personalization=None, max_iter=30, tol=1e-06, nstart=None, weight='weight', dangling=None)

with open("external_pageRankFile.txt", "w", encoding="utf-8") as file:
    for doc_id in rank:
        file.write(
            "/Users/chukuemekaogudu/Documents/solr-7.7.2/CrawlData/foxnews/" + doc_id + "=" + str(math.log(rank[doc_id])) + "\n"
            )