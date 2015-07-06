curl -X POST http://localhost:7474/db/data/ext/ExpandConcepts/node/326885/shortestPath \
  -H "Content-Type: application/json" \
  -d '{"target":"http://localhost:7474/db/data/node/321724", "depth":"10"}'
