import csv
import os
import sys

def content_statistics(visit, fetch):
	if not os.path.isfile(visit) or not os.path.isfile(fetch):
		print("Invalid File path")
		exit(-1)

	##Read data from the Fetch Document
	with open(fetch) as fetch_file:
		csv_reader = csv.reader(fetch_file)
		headers = next(csv_reader)
		fetch_data = []

		for line in csv_reader:
			fetch_data.append(line)

		print("Fetch Data Entries: ", len(fetch_data))

	##Read data from the visit document
	with open(visit) as visit_file:
		csv_reader = csv.reader(visit_file)
		headers = next(csv_reader)
		visit_data = []

		for line in csv_reader:
			visit_data.append(line)

		print("Visit Data Entries: ", len(visit_data))


	##Statistics Variables
	status_codes = dict()
	filesize_bucket = {"<1":0, "1-10":0, "10-100":0, "100-M":0, ">=M": 0}
	content_types = dict()

	##COLLATE STATUS CODE STATS
	for row in fetch_data:
		code = int(row[1].strip())
		if code in status_codes:
			status_codes[code] += 1
		else:
			status_codes[code] = 1

	##COLLATE FILE SIZE STATS
	for row in visit_data:
		size = int(row[1].split()[0])
		size = size//1024
		if size < 1024:
			filesize_bucket["<1"] += 1
		elif size >= 1024 and size < 10240:
			filesize_bucket["1-10"] += 1
		elif size >= 10240 and size < 102400:
			filesize_bucket["10-100"] += 1
		elif size >= 102400 and size < 1024000:
			filesize_bucket["100-M"] += 1
		elif size >= 1024000:
			filesize_bucket[">=M"] += 1

	##COLLATE CONTENT TYPES STATS
	for row in visit_data:
		content_type = row[3]
		if content_type in content_types:
			content_types[content_type] += 1
		else:
			content_types[content_type] = 1

	print("Statistics: \n")
	print("Status Codes: \n", status_codes)
	print("Fize Size Distribution: \n", filesize_bucket)
	print("Content Types Found: \n", content_types)

if __name__ == "__main__":
	visit, fetch = sys.argv[1:]
	content_statistics(visit, fetch)






