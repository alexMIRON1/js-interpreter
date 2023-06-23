# JS-interpreter

## Setting up MongoDB via Docker
```
docker run --name local-mongo-js-interpreter -d -p 2701:27017  mongo:6.0
```
### Additional settings for MongoDB(via CLI)
```
docker exec -it local-mongo-js-interpreter /bin/bash
mongosh
use js_interpreter
show dbs
```
After that you will see list of all databases in mongo and one of them must be js_interpreter