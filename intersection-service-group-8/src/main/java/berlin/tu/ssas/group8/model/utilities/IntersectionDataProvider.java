package berlin.tu.ssas.group8.model.utilities;

import berlin.tu.ssas.group8.model.intersection.StatesContainer;
import berlin.tu.ssas.group8.model.rules.ExcludingRule;
import berlin.tu.ssas.group8.model.rules.ForbiddenStateRule;
import berlin.tu.ssas.group8.model.rules.ForbiddenTransitionRule;
import berlin.tu.ssas.group8.model.rules.TrafficLightRule;
import berlin.tu.ssas.group8.model.trafficLight.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class IntersectionDataProvider {
    HashMap<UUID, TrafficLight> trafficLightsRegister;
    HashMap <UUID, StatesContainer> greenSafeStates;
    ArrayList<TrafficLightRule> stateRules;
    ArrayList<ForbiddenTransitionRule> transitionRules;

    double lo, la;

    public IntersectionDataProvider(double longitude, double latitude){
        this.lo = longitude;
        this.la = latitude;

        trafficLightsRegister = new HashMap<>();
        greenSafeStates = new HashMap<>();

        stateRules = new ArrayList<>();
        transitionRules = new ArrayList<>();

        this.produceData();
    }

    /*  We assume following schema
     *
     *
     *                  |  |
     *               ptl1  ptl2
     *                  |  |
     *           vtl1   |  |
     *                  |  |    vtl3
         ptl3––––––––––––––––––––––––––––––––ptl5
         ptl4––––––––––––––––––––––––––––––––ptl6
     *          vtl2    |  |
     *                  |  | vtl4
     *                  |  |
     *               ptl7  ptl8
     *                  |  |
     *
     * vtl1, vtl4 - Vehicle traffic lights responsible for vertical street movement
     * vtl2, vtl3 - Vehicle traffic lights responsible for horizontal street movement
     * ptl1, ptpl2, ptl7, ptl8 - Pedestrian traffic lights responsible for crossing vertical street
     *  ptl3, ptpl4, ptl5, ptl6 - Pedestrian traffic lights responsible for crossing horizontal street

     */

    private void produceData(){
        //Creating traffic lights with by adding different offsets to a given location.
        VehicleTrafficLight vtl1 = new VehicleTrafficLight(new Location(la+0.00001, lo+0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.NORTH);
        VehicleTrafficLight vtl2 = new VehicleTrafficLight(new Location(la+0.00001, lo-0.00001), TrafficLightState.RED, TrafficLightPositionTag.WEST);
        VehicleTrafficLight vtl3 = new VehicleTrafficLight(new Location(la-0.00001, lo+0.00001), TrafficLightState.RED, TrafficLightPositionTag.EAST);
        VehicleTrafficLight vtl4 = new VehicleTrafficLight(new Location(la-0.00001, lo-0.00001), TrafficLightState.GREEN, TrafficLightPositionTag.SOUTH);
        PedestrianTrafficLight ptl1 = new PedestrianTrafficLight(new Location(la+0.000013, lo+0.000015), TrafficLightState.RED, TrafficLightPositionTag.NORTH);
        PedestrianTrafficLight ptl2 = new PedestrianTrafficLight(new Location(la+0.000013, lo-0.000015), TrafficLightState.RED, TrafficLightPositionTag.NORTH);
        PedestrianTrafficLight ptl3 = new PedestrianTrafficLight(new Location(la-0.000013, lo+0.000015), TrafficLightState.GREEN, TrafficLightPositionTag.WEST);
        PedestrianTrafficLight ptl4 = new PedestrianTrafficLight(new Location(la-0.000013, lo-0.000015), TrafficLightState.GREEN, TrafficLightPositionTag.WEST);
        PedestrianTrafficLight ptl5 = new PedestrianTrafficLight(new Location(la+0.000015, lo+0.000014), TrafficLightState.GREEN, TrafficLightPositionTag.EAST);
        PedestrianTrafficLight ptl6 = new PedestrianTrafficLight(new Location(la+0.000015, lo-0.000014), TrafficLightState.GREEN, TrafficLightPositionTag.EAST);
        PedestrianTrafficLight ptl7 = new PedestrianTrafficLight(new Location(la-0.000015, lo+0.000014), TrafficLightState.RED, TrafficLightPositionTag.SOUTH);
        PedestrianTrafficLight ptl8 = new PedestrianTrafficLight(new Location(la-0.000015, lo-0.000014), TrafficLightState.RED, TrafficLightPositionTag.SOUTH);

        ArrayList<TrafficLight> lights = new ArrayList<>();
        lights.add(vtl1);
        lights.add(vtl2);
        lights.add(vtl3);
        lights.add(vtl4);
        lights.add(ptl1);
        lights.add(ptl2);
        lights.add(ptl3);
        lights.add(ptl4);
        lights.add(ptl5);
        lights.add(ptl6);
        lights.add(ptl7);
        lights.add(ptl8);

        //add traffic lights
        for (TrafficLight tl : lights){
            this.trafficLightsRegister.put(tl.getId().getUuid(), tl);
        }


        //computing safe states by setting each vehicle traffic light to green and all others to red
        StatesContainer sc;
        for(TrafficLight light : lights){
            if(light instanceof  VehicleTrafficLight){
                sc = new StatesContainer();
                for(TrafficLight l : lights){
                    if(l == light){
                        sc.addState(l.getId().getUuid(), TrafficLightState.GREEN);
                    }
                    else{
                        sc.addState(l.getId().getUuid(), TrafficLightState.RED);
                    }
                }
                this.greenSafeStates.put(light.getId().getUuid(), sc);
            }
        }

        for(TrafficLight tl : lights){
            if(tl instanceof PedestrianTrafficLight){
                //Forbidden states for Pedestrian traffic lights
                stateRules.add(new ForbiddenStateRule(tl.getId().getUuid(), TrafficLightState.YELLOW));
                stateRules.add(new ForbiddenStateRule(tl.getId().getUuid(), TrafficLightState.RED_YELLOW));
                stateRules.add(new ForbiddenStateRule(tl.getId().getUuid(), TrafficLightState.EMERGENCY));
            }
            else{
                transitionRules.add(new ForbiddenTransitionRule(tl.getId().getUuid(), TrafficLightState.EMERGENCY, TrafficLightState.GREEN));
                transitionRules.add(new ForbiddenTransitionRule(tl.getId().getUuid(), TrafficLightState.EMERGENCY, TrafficLightState.RED));
                transitionRules.add(new ForbiddenTransitionRule(tl.getId().getUuid(), TrafficLightState.EMERGENCY, TrafficLightState.YELLOW));
                transitionRules.add(new ForbiddenTransitionRule(tl.getId().getUuid(), TrafficLightState.EMERGENCY, TrafficLightState.RED_YELLOW));
                transitionRules.add(new ForbiddenTransitionRule(tl.getId().getUuid(), TrafficLightState.GREEN, TrafficLightState.RED_YELLOW));
                transitionRules.add(new ForbiddenTransitionRule(tl.getId().getUuid(), TrafficLightState.YELLOW, TrafficLightState.GREEN));
                transitionRules.add(new ForbiddenTransitionRule(tl.getId().getUuid(), TrafficLightState.YELLOW, TrafficLightState.RED_YELLOW));
                transitionRules.add(new ForbiddenTransitionRule(tl.getId().getUuid(), TrafficLightState.RED, TrafficLightState.YELLOW));
                transitionRules.add(new ForbiddenTransitionRule(tl.getId().getUuid(), TrafficLightState.RED_YELLOW, TrafficLightState.YELLOW));
            }
        }

        //Vehicle traffic lights excluding each other
        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),vtl2.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),vtl2.getId().getUuid(), TrafficLightState.YELLOW));
        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),vtl2.getId().getUuid(), TrafficLightState.RED_YELLOW));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),vtl2.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),vtl2.getId().getUuid(), TrafficLightState.YELLOW));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),vtl2.getId().getUuid(), TrafficLightState.RED_YELLOW));

        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),vtl3.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),vtl3.getId().getUuid(), TrafficLightState.YELLOW));
        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),vtl3.getId().getUuid(), TrafficLightState.RED_YELLOW));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),vtl3.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),vtl3.getId().getUuid(), TrafficLightState.YELLOW));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),vtl3.getId().getUuid(), TrafficLightState.RED_YELLOW));


        //vehicle traffic lights excluding pedestrian lights
        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),ptl1.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),ptl2.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),ptl7.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl1.getId().getUuid(),ptl8.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),ptl1.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),ptl2.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),ptl7.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl4.getId().getUuid(),ptl8.getId().getUuid(), TrafficLightState.GREEN));

        stateRules.add(new ExcludingRule(vtl2.getId().getUuid(),ptl3.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl2.getId().getUuid(),ptl4.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl2.getId().getUuid(),ptl5.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl2.getId().getUuid(),ptl6.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl3.getId().getUuid(),ptl3.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl3.getId().getUuid(),ptl4.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl3.getId().getUuid(),ptl5.getId().getUuid(), TrafficLightState.GREEN));
        stateRules.add(new ExcludingRule(vtl3.getId().getUuid(),ptl6.getId().getUuid(), TrafficLightState.GREEN));
    }

    public HashMap<UUID, TrafficLight> getTrafficLightsRegister() {
        return trafficLightsRegister;
    }

    public HashMap<UUID, StatesContainer> getGreenSafeStates() {
        return greenSafeStates;
    }

    public ArrayList<TrafficLightRule> getStateRules() {
        return stateRules;
    }

    public ArrayList<ForbiddenTransitionRule> getTransitionRules() {
        return transitionRules;
    }


}
