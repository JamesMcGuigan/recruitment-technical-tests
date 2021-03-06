#!/usr/bin/env bash -x

# cd to script directory - https://stackoverflow.com/a/51651602/748503
cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# Count user_engagements grouped by date
cat ./data/bq-results-sample-data.jsons |
    # Grep Data Filters
    grep user_engagement |                                       # We also get an event called “user_engagement” for each session of use
    grep -P '"engagement_time_msec"\D+([3-9]|\d\d+)\d{3}' |      # We say a user is active if the engagement time is at least 3 seconds
    grep -P '_event",' |                                         # And any valuable events occurred at least once (all 3s+ engagement times contain valuable events)

    ### Extract timestamp and convert timezones
    # perl -p -e 's/.*"event_date":.*?(\d+).*/$1/g'    |     # OLD: UCT date method
    perl -p -e 's/.*"event_timestamp":.*?(\d+).*/$1/g' |     # Use UTC timestamp instead of UTC date
    perl -p -e 's/\d{6}$//'                            |     # Convert microseconds to seconds
    TZ=US/Central  xargs -I'{}' date -d "@{}" +%Y%m%d    |     # Convert to US/Central time

    # Sort, Group and Count
    sort -n |
    uniq -c |

    # Print SQL
    # awk  '{ print "REPLACE INTO active_user_table ( date, active_user_count ) VALUES( "$2", "$1" );" }' |
    awk  '{
        print "INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( "$2", 0 );";
        print "UPDATE                active_user_table SET active_user_count = active_user_count + "$1" WHERE date = "$2";";
    }' |
    cat                                                      # Useless use of a cat award! (for commenting out)

# Outputs:
# INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( 20191230, 0 );
# UPDATE                active_user_table SET active_user_count = active_user_count + 37 WHERE date = 20191230;
# INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( 20191231, 0 );
# UPDATE                active_user_table SET active_user_count = active_user_count + 38 WHERE date = 20191231;

