#!/usr/bin/env bash -x

# cd to script directory - https://stackoverflow.com/a/51651602/748503
cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# Count user_engagements grouped by date
cat ./data/bq-results-sample-data.jsons |
    grep user_engagement |
    perl -p -e 's/.*"event_date":.*?(\d+).*/$1/g' |
    sort -n |
    uniq -c |
    awk  '{ print "REPLACE INTO active_user_table ( date, active_user_count ) VALUES( "$2", "$1" );" }'
    # awk  '{ print "UPDATE active_user_table SET active_user_count = active_user_count + "$1" WHERE date = "$2";" }'


# Outputs:
# REPLACE INTO active_user_table ( date, active_user_count ) VALUES( 20191230, 37 );
# REPLACE INTO active_user_table ( date, active_user_count ) VALUES( 20191231, 38 );

