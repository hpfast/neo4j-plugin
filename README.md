# Histograph Neo4j plugin

[Unmanaged Extension](http://neo4j.com/docs/stable/server-unmanaged-extensions.html] for Neo4j v2.2.2. Uses Neo4j's [Traversal Framework Java API](http://neo4j.com/docs/stable/tutorial-traversal-java-api.html) to compute Histograph concepts, and their outgoing relations.

## Installation

First, add the following line to `neo4j-server.properties`:

    #Comma separated list of JAXRS packages containing JAXRS Resource, one package name for each mountpoint.
    org.neo4j.server.thirdparty_jaxrs_classes=org.waag.histograph.plugins=/histograph

Afterwards, run `mvn package` and copy JAR file to Neo4j's plugin directory, or run `install.sh` (currently only works under MacOS with a Neo4j installed by Neo4j).

## Usage

    curl -s http://localhost:7474/histograph/expand
