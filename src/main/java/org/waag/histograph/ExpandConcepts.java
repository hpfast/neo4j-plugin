package org.waag.histograph;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.server.plugins.*;

import java.util.ArrayList;
import java.util.List;

public class ExpandConcepts extends ServerPlugin {

  @Name("expandConcepts")
  @Description("Maak klonten")
  @PluginTarget(GraphDatabaseService.class)
  public Iterable<String>  expandConcepts( @Source GraphDatabaseService graphDb,
                  @Description("Startnodes")
                    @Parameter(name = "ids", optional = false) String[] ids,
                  @Description("Klontvormende relaties")
                    @Parameter(name = "types", optional = true) String[] types,
                  @Description("Cavia's?")
                    @Parameter(name = "hairy", optional = true ) Boolean hairy
                  )
  {
    ArrayList<String> results = new ArrayList<String>();

    for (String id : ids) {
      results.add("Maak een klont voor " + id);
    }

    return results;
  }

}
