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


Notes :
1. DONE: event_date is UTC. Our users are based in the US.  (Assumed Central)
2. DONE: We say a user is active if the engagement time is at least 3 seconds and any valuable
events occurred at least once
                    


## Questions:
 
**1. Using the data in the attached [data/bq-results-sample-data.jsons](data/bq-results-sample-data.jsons) file, 
create a script to load the data from the events into the table above. You can use python, or any language you 
feel familiar with. Please provide instructions on how to run the script.**


Unix As A Programming Language: Bash
```bash
# rm -vf Sliide/active_user_table.db  # reset database - if required
time Sliide/active_user_table.import.sh
```

OUTPUT

On Insert:
```
+ cat ./active_user_table.schema.sql
+ tee /dev/stderr
+ sqlite3 active_user_table.db
CREATE TABLE IF NOT EXISTS active_user_table (
    date              DATE    PRIMARY KEY,
    active_user_count INTEGER NOT NULL
) WITHOUT ROWID;

+ ./count_user_engagements_by_date.sh
+ tee /dev/stderr
+ sqlite3 active_user_table.db


+ grep user_engagement
+ grep -P '"engagement_time_msec"\D+([3-9]|\d\d+)\d{3}'
+ grep -P '_event",'
+ perl -p -e 's/.*"event_timestamp":.*?(\d+).*/$1/g'
+ perl -p -e 's/\d{6}$//'
+ TZ=US/Central
+ xargs '-I{}' date -d '@{}' +%Y%m%d
+ sort -n
+ uniq -c
+ awk '{
        print "INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( "$2", 0 );";
        print "UPDATE                active_user_table SET active_user_count = active_user_count + "$1" WHERE date = "$2";";
    }'
+ cat
INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( 20191229, 0 );
UPDATE                active_user_table SET active_user_count = active_user_count + 11 WHERE date = 20191229;
INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( 20191230, 0 );
UPDATE                active_user_table SET active_user_count = active_user_count + 31 WHERE date = 20191230;
INSERT OR IGNORE INTO active_user_table     ( date, active_user_count ) VALUES ( 20191231, 0 );
UPDATE                active_user_table SET active_user_count = active_user_count + 22 WHERE date = 20191231;


+ echo 'SELECT * from active_user_table'
+ tee /dev/stderr
+ sqlite3 active_user_table.db -column -header
SELECT * from active_user_table
date        active_user_count
----------  -----------------
20191229    11               
20191230    31               
20191231    22               

real    0m0.517s
user    0m0.202s
sys     0m0.281s                                       
```

On Update
```
+ echo 'SELECT * from active_user_table'
+ tee /dev/stderr
+ sqlite3 active_user_table.db -column -header
SELECT * from active_user_table
date        active_user_count
----------  -----------------
20191229    22               
20191230    62               
20191231    44               

real    0m0.482s
user    0m0.192s
sys     0m0.273s
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
- For this micro usecase of counting a single datafield, sqlite might actually be able to handle it!

Depends on where the performance bottleneck is

- CPU     - multiprocess using `xargs -P` or `gnu parallel`
- RAM     - split SQL commands into smaller batches (or use smaller files)
- Disk IO - Keep all data in memory, reimplement using Redis 


**Bash Performance** 

- Performance using INSERT OR UPDATE syntax was 0.12s per 1000 lines.
    - Reduced to ~0.5s when timestamp parsing logic is implemented  

- 100,000,000 / 1440 minutes = 70k lines per file * 0.12s = 8.4 seconds (2011 Macbook Pro)
  - 1 minute / 8.4 seconds * 100,000,000 = 700 million events per day 
- 100,000,000 / 1440 minutes = 70k lines per file * 0.5s  = 35  seconds (with timestamp parsing)
  - 1 minute / 35 seconds * 100,000,000 = 170 million events per day

- Back of the envelope calculation suggests bash may scale to hundreds of millions events per day 
  - untested assumptions about linearity of loading and scaling are unreasonable here 
  - lots of variation when disk IO doesn't have the source file in memory

 
**3. Suggest any target architecture to cater for this growth.**

- The rest of your tech stack might require a proper database cluster (MS SQL, MySQL, Oracle, OrientDB) or Cloud SQL 

- Alternatively you could ingest all your event data into an ElasticSearch cluster then run HyperLogLog to do your counting for you in realtime

- Was the correct answer to reference the Lambda Architecture Book
  - https://www.amazon.co.uk/Big-Data-Principles-practices-scalable/dp/1617290343
  


**We expect the result as a github repository. Please leave the commit history in.**

https://github.com/JamesMcGuigan/recruitment-technical-tests/tree/master/Sliide

