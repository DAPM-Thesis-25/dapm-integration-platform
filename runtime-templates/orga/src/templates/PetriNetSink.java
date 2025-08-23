package templates;

import communication.message.Message;
import communication.message.impl.petrinet.PetriNet;
import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;
import communication.message.impl.petrinet.arc.Arc;

import communication.message.impl.petrinet.arc.PlaceToTransitionArc;
import communication.message.impl.petrinet.arc.TransitionToPlaceArc;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import utils.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.engine.Format.SVG;
import static guru.nidi.graphviz.engine.Graphviz.fromGraph;
import static guru.nidi.graphviz.model.Factory.*;

public class PetriNetSink extends Sink {
    public PetriNetSink(Configuration configuration) {
        super(configuration);
    }

@Override
public void observe(Pair<Message, Integer> messageAndPortNumber) {
    PetriNet petriNet = (PetriNet) messageAndPortNumber.first();
    System.out.println("PetriNet has " + petriNet.getPlaces().size() + " places and " + petriNet.getTransitions().size() + " transitions.");
    MutableGraph dotGraph = constructDotGraph(petriNet);
    try {
        // Use env var set in docker-compose; fallback to /tmp/petrinet
        String outDir = System.getenv().getOrDefault("PETRINET_OUTPUT_DIR", "/tmp/petrinet");
        File dir = new File(outDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Could not create output directory: " + outDir);
        }

        File outFile = new File(dir, "petriNet.svg");
        fromGraph(dotGraph)
            .width(1024)  
            .render(SVG)
            .toFile(outFile);

        System.out.println("PetriNet SVG written to: " + outFile.getAbsolutePath());
    } catch (Exception e) {
        throw new RuntimeException("Failed to render PetriNet", e);
    }
}



    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(PetriNet.class, 1);
        return map;
    }

    private MutableGraph constructDotGraph(PetriNet petriNet) {
        MutableGraph dotGraph = mutGraph("petriNet").setDirected(true);

        Set<Place> places = petriNet.getPlaces();
        Set<Transition> transitions = petriNet.getTransitions();
        Set<Arc> arcs = petriNet.getFlowRelation();

        Set<PlaceToTransitionArc> placeToTransitionArcs = arcs.stream()
                .filter(arc -> arc instanceof PlaceToTransitionArc)
                .map(arc -> (PlaceToTransitionArc) arc)
                .collect(Collectors.toSet());

        Set<TransitionToPlaceArc> transitionToPlaceArcs = arcs.stream()
                .filter(arc -> arc instanceof TransitionToPlaceArc)
                .map(arc -> (TransitionToPlaceArc) arc)
                .collect(Collectors.toSet());


        Map<String, MutableNode> nodeMap = new HashMap<>();

        for (Place p : places) {
            MutableNode placeNode = mutNode(p.getID()).add(Shape.CIRCLE, Color.GREEN);
            dotGraph.add(placeNode);
            nodeMap.put(p.getID(), placeNode);
        }

        for (Transition t : transitions) {
            MutableNode transNode = mutNode(t.getID()).add(Shape.BOX, Color.BLUE);
            dotGraph.add(transNode);
            nodeMap.put(t.getID(), transNode);
        }

        for (PlaceToTransitionArc a : placeToTransitionArcs) {
            String src = a.getSource().getID();
            String tgt = a.getTarget().getID();
            if(nodeMap.containsKey(src) && nodeMap.containsKey(tgt)) {
                nodeMap.get(src).addLink(nodeMap.get(tgt));
            }
        }

        for (TransitionToPlaceArc a : transitionToPlaceArcs) {
            String src = a.getSource().getID();
            String tgt = a.getTarget().getID();
            if(nodeMap.containsKey(src) && nodeMap.containsKey(tgt)) {
                nodeMap.get(src).addLink(nodeMap.get(tgt));
            }
        }
        return dotGraph;
    }
}
