#!/usr/bin/env bash

# cd to script directory - https://stackoverflow.com/a/51651602/748503
cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# Count user_engagements grouped by date
cat bq-results-sample-data.jsons | grep user_engagement | perl -p -e 's/.*"event_date":.*?(\d+).*/$1/g' | sort -n |  uniq -c

# Outputs
#     37 20191230
#     38 20191231

