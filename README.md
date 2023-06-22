# JS-interpreter

## Setting up MongoDB via Docker
```
docker run --name local-mongo-js-interpreter -d -p 2701:27017 -e MONGO_INITDB_ROOT_USERNAME=mongo -e MONGO_INITDB_ROOT_PASSWORD=mongo mongo:6.0
```
### Additional settings for MongoDB(via CLI)
```
docker exec -it local-mongo-js-interpreter /bin/bash
mongosh
use admin
db.auth("mongo", "mongo")
show dbs
use js_interpreter
db.temp.insertOne({"created":1})
show dbs
```
After that you will see list of all databases in mongo and one of them must be js_interpreter