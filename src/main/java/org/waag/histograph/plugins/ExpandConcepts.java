package org.waag.histograph.plugins;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;

import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

@javax.ws.rs.Path( "/expand" )
public class ExpandConcepts {

  private enum Rels implements RelationshipType {
    hg_sameHgConcept, hg_isUsedFor, hg_liesIn
  }

  private GraphDatabaseService graphDb;
  private final ObjectMapper objectMapper;

  private TraversalDescription conceptTraversalDescription;
  private TraversalDescription hairsTraversalDescription;

  private boolean isPit(Node node) {
    for (Label label: node.getLabels()) {
      if (label.name().equals("_Rel")) {
        return false;
      }
    }
    return true;
  }

  public ExpandConcepts(@Context GraphDatabaseService graphDb) {
    this.graphDb = graphDb;
    this.objectMapper = new ObjectMapper();

    this.conceptTraversalDescription = graphDb.traversalDescription()
        .breadthFirst()
        .relationships(Rels.hg_isUsedFor, Direction.BOTH)
        .relationships(Rels.hg_sameHgConcept, Direction.BOTH)
        .uniqueness(Uniqueness.NODE_RECENT);

    this.hairsTraversalDescription = graphDb.traversalDescription()
      .depthFirst()
      .relationships(Rels.hg_liesIn, Direction.OUTGOING)
      .evaluator(Evaluators.fromDepth(2))
      .evaluator(Evaluators.toDepth(2));
      //.uniqueness(Uniqueness.NODE_PATH);
    //.evaluator(Evaluators.excludeStartPosition());
  }

  @GET
  @javax.ws.rs.Path("/")
  public Response expand() {
    StreamingOutput stream = new StreamingOutput() {

      @Override
      public void write( OutputStream os ) throws IOException, WebApplicationException {

        // TODO: convert function to POST, accepting JSON of the following form
        // {
        //   "ids"
        //   "geometry"
        //   "traversalRelations
        //   "hairRelations"
        //   "hairy"
        // }

        JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator( os, JsonEncoding.UTF8 );
        jg.writeStartArray();
//
//        String[] ids = {
//            "urn:hg:geonames:2759794",
//            "urn:hg:geonames:2753637",
//            "urn:hg:geonames:2753639",
//            "urn:hg:geonames:2753638",
//            "urn:hg:geonames:2753640",
//            "urn:hg:tgn:7264696",
//            "urn:hg:tgn:term:1001511217",
//            "urn:hg:geonames:2753636",
//            "urn:hg:tgn:7264697",
//            "urn:hg:tgn:7264700",
//            "urn:hg:tgn:term:1001493884"
//        };

        String[] ids = {
            "urn:hg:geonames:2753638"
        };

        ArrayList<String> visited = new ArrayList<String>();

        try (Transaction tx = graphDb.beginTx()) {
          for (String id : ids) {
            if (!visited.contains(id)) {
              Concept concept = new Concept();

              Node node = graphDb.findNode(DynamicLabel.label("_"), "id", id);
              concept.addPit(node);
              visited.add(id);

              if (node == null) {
                continue;
              }

              Traverser conceptTraverser = conceptTraversalDescription.traverse(node);

              // Get all nodes found in each path, add them to concept if they weren't added before
              for (Path path : conceptTraverser) {
                Node startNode = path.startNode();
                String startNodeId = startNode.getProperty("id").toString();

                Node endNode = path.endNode();
                String endNodeId = endNode.getProperty("id").toString();

                if (!visited.contains(endNodeId)) {

                  boolean endNodeIsPit = isPit(endNode);
                  if (endNodeIsPit) {
                    concept.addPit(endNode);
                  }

                  visited.add(endNodeId);
                }
              }

              for (Path path : conceptTraverser) {
                // Identity relation paths (always length 2) denote single identify relation
                // between two PITs in concept. Walk paths, and add them to concept!
                for (Node pathNode: path.nodes()) {
                  if (!isPit(pathNode)) {
                    Iterable<Relationship> outgoingRelations = pathNode.getRelationships(Direction.OUTGOING);
                    Iterable<Relationship> incomingRelations = pathNode.getRelationships(Direction.INCOMING);

                    Relationship outgoingRelation = outgoingRelations.iterator().next();
                    Relationship incomingRelation = incomingRelations.iterator().next();

                    String incomingStartNodeId = incomingRelation.getStartNode().getProperty("id").toString();

                    concept.addRelation(incomingStartNodeId, outgoingRelation);
                  }
                }
              }

              Traverser hairsTraverser = hairsTraversalDescription.traverse(concept.getNodes());

              // Each path is an incoming or outgoing hair: (p)-[r]-[q]
              // p belongs to the concept, q does not
              for (Path path : hairsTraverser) {
                String startNodeId = path.startNode().getProperty("id").toString();
                Node endNode = path.endNode();
                Relationship relation = path.lastRelationship();
                concept.addHair(startNodeId, relation, endNode);
              }

              concept.toJson(jg);
            }
          }
        }

        jg.writeEndArray();
        jg.flush();
        jg.close();
      }
    };

    return Response.ok().entity( stream ).type( MediaType.APPLICATION_JSON ).build();
  }
}
