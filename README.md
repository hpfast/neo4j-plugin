# Histograph Neo4j plugin

[Unmanaged Extension](http://neo4j.com/docs/stable/server-unmanaged-extensions.html) for Neo4j v2.2.2. Uses Neo4j's [Traversal Framework Java API](http://neo4j.com/docs/stable/tutorial-traversal-java-api.html) to compute Histograph concepts, and their outgoing relations.

## Installation

First, add the following line to `neo4j-server.properties`:

    #Comma separated list of JAXRS packages containing JAXRS Resource, one package name for each mountpoint.
    org.neo4j.server.thirdparty_jaxrs_classes=org.waag.histograph.plugins=/histograph

Afterwards, clone this repository:

    git clone https://github.com/histograph/neo4j-plugin.git

Go to directory:

    cd neo4j-plugin

And build the plugin using Maven:

    mvn package

Then, copy JAR file to Neo4j's plugin directory!

(Or just run `install.sh` (currently only works under MacOS with a Neo4j installed by Neo4j).)

## Usage

    curl -s -X POST http://localhost:7474/histograph/expand \
      -H "Content-Type: application/json" \
      -d '{"ids": ["urn:hg:geonames:2759794", "urn:hg:geonames:2753637", "urn:hg:geonames:2753639", "urn:hg:geonames:2753638", "urn:hg:geonames:2753640", "urn:hg:tgn:7264696", "urn:hg:tgn:term:1001511217", "urn:hg:geonames:2753636", "urn:hg:tgn:7264697", "urn:hg:tgn:7264700", "urn:hg:tgn:term:1001493884"]}' \
    | python -mjson.tool | pygmentize -l js

The plugin exposes a single endpoint - `/histograph/expand` - and expects a POST request with a JSON body of the following form:

```json
{
  "ids": [
    "urn:hg:geonames:2759794",
    "urn:hg:geonames:2753637",
    "..."
  ]
}
```
