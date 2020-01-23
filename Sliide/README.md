# Sliide - Data Engineer Challenge

We have multiple apps emitting different kinds of events, such as when a push notification has been 
received, when a certain screen was viewed, or the app has been updated. We also get an event 
called “user_engagement” for each session of use, with the time the user was active in the app.

We would like to be able to plot the number of active users per day. Our analytics database is 
an SQL database, so the data from the event should be loaded in the following table:


#### active_user_table

| Field name        | Type    | Mode     |
| ----------------- | ------- | -------- |
| date              | DATE    | REQUIRED | 
| active_user_count | INTEGER | REQUIRED |


Notes (unimplemented):
1. event_date is UTC. Our users are based in the US.
2. We say a user is active if the engagement time is at least 3 seconds and any valuable
events occurred at least once.
                    


## Questions:
 
**1. Using the data in the attached [data/bq-results-sample-data.jsons](data/bq-results-sample-data.jsons) file, 
create a script to load the data from the events into the table above. You can use python, or any language you 
feel familiar with. Please provide instructions on how to run the script.**


Unix As A Programming Language: Bash
```
# rm -vf Sliide/active_user_table.db  # reset database - if required
time Sliide/active_user_table.import.sh
```

OUTPUT

On Insert:
```bash
+ cat ./active_user_table.schema.sql
+ tee /dev/stderr
+ sqlite3 active_user_table.db
CREATE TABLE IF NOT EXISTS active_user_table (
    date              DATE    PRIMARY KEY,
    active_user_count INTEGER NOT NULL
) WITHOUT ROWID;+ echo

+ ./count_user_engagements_by_date.sh
+ tee /dev/stderr
+ sqlite3 active_user_table.db

++ cat ./data/bq-results-sample-data.jsons
++ grep user_engagement
++ perl -p -e 's/.*"event_date":.*?(\d+).*/$1/g'
++ sort -n
++ uniq -c
++ awk '{
        print "INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( "$2", 0 );";
        print "UPDATE                active_user_table SET active_user_count = active_user_count + "$1" WHERE date = "$2";";
    }'
INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( 20191230, 0 );
UPDATE                active_user_table SET active_user_count = active_user_count + 37 WHERE date = 20191230;
INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( 20191231, 0 );
UPDATE                active_user_table SET active_user_count = active_user_count + 38 WHERE date = 20191231;
+ echo

+ echo 'SELECT * from active_user_table'
+ tee /dev/stderr
+ sqlite3 active_user_table.db -column -header

SELECT * from active_user_table
date        active_user_count
----------  -----------------
20191230    37               
20191231    38               

real    0m0.118s
user    0m0.091s
sys     0m0.061s                                           
```

On Update
```
+ echo 'SELECT * from active_user_table'
+ tee /dev/stderr
+ sqlite3 active_user_table.db -column -header
SELECT * from active_user_table
date        active_user_count
----------  -----------------
20191230    74               
20191231    76               

real    0m0.112s
user    0m0.089s
sys     0m0.057s
```

NOTES: 

- Assumes .jsons format (one json per newline)

- UPDATE syntax allows for incremental updates
  - Assumes duplicate data will not be ingested 

- (OLD) REPLACE SQL syntax assumes:
  - dataset will only contain data for entire days (not partial days)
  - script can be run at midday then at midnight to update TODAY   
  - script can be rerun multiple times on stale data



**2. How would you design the ETL process for it to automatically update daily?**

Crontab will execute the script nightly 
```
0 0 * * * Sliide/active_user_table.import.sh >/dev/null 2>&1
```

**3. How would you scale this process if we got tens or hundreds of millions of events per day?**

- Rather than batch mode, the script could read a unix pipe with the raw stream of events

- Performance bottleneck here is probably the disk IO and the SSD drive. 

- Keeping all the database and json datastream in memory may help performance optimize this

- For this micro usecase of counting a single datafield, sqlite might actually be able to handle it!


**Bash Performance** 

- Performance using INSERT OR UPDATE syntax was 0.12s per 1000 lines.

- 100,000,000 / 1440 minutes = 70k lines per file * 0.12s = 8.4 seconds (2011 Macbook Pro)

- Back of the envelope calculation suggests bash may scale to 7 billion events per day 
  - untested assumptions about linearity of scaling are unreasonable here 



 
**3. Suggest any target architecture to cater for this growth.**

- The rest of your tech stack might require a proper database cluster (MS SQL, MySQL, Oracle, OrientDB) or Cloud SQL 

- Alternatively you could ingest all your event data into an ElasticSearch cluster then run HyperLogLog to do your counting for you in realtime

- Was the correct answer to reference the Lambda Architecture Book
  - https://www.amazon.co.uk/Big-Data-Principles-practices-scalable/dp/1617290343
  


**We expect the result as a github repository. Please leave the commit history in.**

https://github.com/JamesMcGuigan/recruitment-technical-tests/tree/master/Sliide

