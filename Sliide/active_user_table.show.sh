#!/usr/bin/env bash

# cd to script directory - https://stackoverflow.com/a/51651602/748503
cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# Create SQL table if required
echo "SELECT * from active_user_table" | tee /dev/stderr | sqlite3 active_user_table.db -column -header
