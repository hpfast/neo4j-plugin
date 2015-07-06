package org.waag.histograph;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.server.plugins.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;

import java.util.ArrayList;
import java.util.List;

public class ExpandConcepts extends ServerPlugin {

  private enum Rels implements RelationshipType {
    hg_sameHgConcept, hg_isUsedFor
  }

  @Name("expandConcepts")
  @Description("Maak klonten")
  @PluginTarget(GraphDatabaseService.class)
  public Iterable<Path>  expandConcepts( @Source GraphDatabaseService graphDb,
                  @Description("Startnodes")
                    @Parameter(name = "ids", optional = false) String[] ids,
                  @Description("Klontvormende relaties")
                    @Parameter(name = "types", optional = true) String[] types,
                  @Description("Cavia's?")
                    @Parameter(name = "hairy", optional = true ) Boolean hairy
                  )
  {
//    Node startNode = GraphMethods.getNodeByHgid(db, hgid);
//    if (startNode == null) {
//      response.append("hgids_not_found", hgid);
//      continue;
//    }
//
//    // Create a map with <Node, nRels> pairs to sort nodes by the number of relationships later on
//    Map<String, Integer> nRelsMap = new HashMap<String, Integer>();
//    Set<Relationship> relSet = new HashSet<Relationship>();
//
//    nRelsMap.put(hgid, startNode.getDegree());
//    hgidsDone.add(hgid);
//
//    // We only traverse the SameHgConcept relationship
//    TraversalDescription td = db.traversalDescription()
//        .breadthFirst()
//        .relationships(ReasoningDefinitions.RelationType.SAMEHGCONCEPT, Direction.BOTH)
//        .evaluator(Evaluators.excludeStartPosition());
//
//    Traverser pitTraverser =  td.traverse(startNode);
//
//    // Get all nodes found in each path, add them to the list if they weren't added before
//    for (Path path : pitTraverser) {
//      Node endNode = path.endNode();
//      String hgidFound = endNode.getProperty(HistographTokens.General.HGID).toString();
//
//      if (!hgidsDone.contains(hgidFound)) {
//        nRelsMap.put(hgidFound, endNode.getDegree());
//        hgidsDone.add(hgidFound);
//      }
//    }
//
//    // Add relationships to a Set to filter out duplicates
//    for (Relationship r : pitTraverser.relationships()) {
//      relSet.add(r);
//    }
//
//    // Sort nodes by #relationships
//    ValueComparator comparator = new ValueComparator(nRelsMap);
//    TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer>(comparator);
//    sortedMap.putAll(nRelsMap);
//
//    int pitIndex = 0;
//    int geometryIndex = 0;
//    boolean pitsPresentWithGeometry = false;
//
//    // For each node in the Set (= hgConcept), create JSON output
//    for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
//      JSONObject pit = new JSONObject();
//      Node node = GraphMethods.getNodeByHgid(db, entry.getKey());
//      boolean hasGeometry = false;
//
//      for (String key : node.getPropertyKeys()) {
//        if (key.equals(HistographTokens.PITTokens.GEOMETRY)) {
//          pit.put("geometryIndex", geometryIndex);
//          geometryArr.put(geometryIndex, new JSONObject(node.getProperty(key).toString()));
//          hasGeometry = true;
//          pitsPresentWithGeometry = true;
//          geometryIndex++;
//        } else if (key.equals(HistographTokens.PITTokens.TYPE)) {
//          pit.put(HistographTokens.PITTokens.TYPE, node.getProperty(key));
//          properties.put(HistographTokens.PITTokens.TYPE, node.getProperty(key));
//        } else if (key.equals(HistographTokens.PITTokens.DATA)) {
//          pit.put(HistographTokens.PITTokens.DATA, new JSONObject(node.getProperty(key).toString()));
//        } else {
//          pit.put(key, node.getProperty(key));
//        }
//      }
//
//      if (!hasGeometry) {
//        pit.put("geometryIndex", -1);
//      }
//
//      JSONObject pitRelations = new JSONObject();
//
//      // Add all outgoing relationships to each PIT
//      for (Relationship r : relSet) {
//        if (r.getStartNode().equals(node)) {
//          String type = RelationType.fromRelationshipType(r.getType()).toString();
//          if (pitRelations.has(type)) {
//            pitRelations.getJSONArray(type).put(r.getEndNode().getProperty(HistographTokens.General.HGID));
//          } else {
//            JSONArray relArray = new JSONArray();
//            relArray.put(r.getEndNode().getProperty(HistographTokens.General.HGID));
//            pitRelations.put(type, relArray);
//          }
//        }
//      }
//
//      if (pitRelations.length() > 0) {
//        pit.put("relations", pitRelations);
//      }
//
//      pits.put(pitIndex, pit);
//      pitIndex ++;
//    }
//
//    // Only add the HgConcept to the JSON output if at least one geometry is present
//    if (pitsPresentWithGeometry) {
//
//      geometryObj.put("type", "GeometryCollection");
//      geometryObj.put("geometries", geometryArr);
//
//      properties.put("pits", pits);
//      feature.put("properties", properties);
//      feature.put("geometry", geometryObj);
//      features.put(feature);
//    }

    ArrayList<Path> results = new ArrayList<Path>();

    try (Transaction tx = graphDb.beginTx()) {
      for (String id : ids) {

        Node node = graphDb.findNode(DynamicLabel.label("_"), "id", id);

        if (node == null) {
          continue;
        }

        TraversalDescription td = graphDb.traversalDescription()
            .breadthFirst()
            .relationships(Rels.hg_isUsedFor, Direction.BOTH)
            .relationships(Rels.hg_sameHgConcept, Direction.BOTH)
            .uniqueness( Uniqueness.RELATIONSHIP_GLOBAL );

        for ( Path path : td.traverse(node) )
        {
          results.add(path);
        }


      }
    }


    return results;
  }

}
