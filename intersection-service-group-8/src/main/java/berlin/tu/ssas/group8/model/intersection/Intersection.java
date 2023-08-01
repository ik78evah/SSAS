package berlin.tu.ssas.group8.model.intersection;

import berlin.tu.ssas.group8.model.rules.ForbiddenTransitionRule;
import berlin.tu.ssas.group8.model.rules.TrafficLightRule;
import berlin.tu.ssas.group8.model.trafficLight.*;
import berlin.tu.ssas.group8.model.utilities.IntersectionDataProvider;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class Intersection {

    final int greenPhaseDuration = 30;
    final int transitionPhaseDuration = 10;

    HashMap<UUID, TrafficLight> trafficLightsRegister;
    HashMap<UUID, StatesContainer> greenSafeStates; // safe states for GreenLightRequest
    ArrayList<UUID> lightsIds; //just saving names, so we haven't to access HashMap every time

    @Getter
    LocalDateTime blockedUntil;//null if intersection is not blocked
    String blockedBy;
    LinkedList<ScheduledStatesContainer> scheduledContainers;//queue
    StatesContainer savedState;//null or state, which is "paused" (add by alex: one green, rest red)

    ArrayList<TrafficLightRule> stateRules;
    ArrayList<ForbiddenTransitionRule> transitionRules;

    @Getter
    @ConfigProperty(name = "intersectionID")
    int intersectionId;

    @Getter
    @ConfigProperty(name = "longitude")
    double longitude;

    @Getter
    @ConfigProperty(name = "latitude")
    double latitude;

    @ConfigProperty(name = "info")
    String info;

    private Logger log = Logger.getLogger("Intersection");

    public Intersection() {
    }

    //Maybe we could use it for testing
    public Intersection(IntersectionDataProvider provider) {
        this.trafficLightsRegister = provider.getTrafficLightsRegister();
        this.greenSafeStates = provider.getGreenSafeStates();
        this.stateRules = provider.getStateRules();
        this.transitionRules = provider.getTransitionRules();

        this.scheduledContainers = new LinkedList<>();
        this.lightsIds = new ArrayList<>(trafficLightsRegister.keySet());
    }

    public void initializeIntersection() {
        log = Logger.getLogger("Intersection" + this.intersectionId);
        IntersectionDataProvider provider = new IntersectionDataProvider(longitude, latitude);
        this.trafficLightsRegister = provider.getTrafficLightsRegister();
        this.greenSafeStates = provider.getGreenSafeStates();
        this.stateRules = provider.getStateRules();
        this.transitionRules = provider.getTransitionRules();

        this.scheduledContainers = new LinkedList<>();
        this.lightsIds = new ArrayList<>(trafficLightsRegister.keySet());
    }

    /**
     * get the trafficLight of the specified UUID
     *
     * @param id UUID of the trafficLight that should be retrieved
     * @return the respective trafficLight of the specified UUID
     * @throws NoSuchElementException if the intersection does not contain the submitted UUID
     */

    public TrafficLight getTrafficLight(@NonNull UUID id) {
        if (this.trafficLightsRegister.containsKey(id)) {
            return this.trafficLightsRegister.get(id);
        } else {
            throw new NoSuchElementException("Intersection does not have any traffic light with id: " + id);
        }
    }

    /**
     * gets all the trafficLights of an intersection
     *
     * @return ArrayList containing all the trafficLight of an intersection
     */
    public List<TrafficLight> getAllTrafficLights() {
        return new ArrayList<>(trafficLightsRegister.values());
    }

    /**
     * gets the current trafficLightState constellation of an intersection
     *
     * @return returns a StatesContainer containing all the trafficLightStates with their respective UUID
     */
    public StatesContainer getCurrentState() {  //wrapping current state in a container, so we can easily manipulate/save it

        StatesContainer container = new StatesContainer();
        for (UUID id : this.lightsIds)
            container.addState(id, this.trafficLightsRegister.get(id).getState());

        return container;
    }

    /**
     * Changes the TrafficLightStates constellation of an intersection to the specified constellation
     *
     * @param sc StatesContainer containing the TrafficLightStates constellation that should be acquired
     */
    private void executeState(StatesContainer sc) { // Check container safety before.
        for (UUID id : this.lightsIds)
            this.trafficLightsRegister.get(id).setState(sc.getState(id));
    }

    /**
     * adds the specified ScheduledStatesContainer to the queue of scheduledContainers
     *
     * @param container contains the ScheduledStatesContainer that should be appended
     */
    private void scheduleState(ScheduledStatesContainer container) {//adds container in a queue
        this.scheduledContainers.addLast(container);
    }

    /**
     * loops through scheduledContainers changing the trafficLightState constellation of the intersection
     */
    @Scheduled(every = "5s")
    void scheduleLoop() {
        if (this.scheduledContainers.isEmpty())//no scheduled states
            return;

        if (!this.scheduledContainers.getFirst().isStarted()) {//there are states, but queue not started
            log.info("New scheduled sequence started!");
            log.info("Executing state <" + scheduledContainers.getFirst().getName() + "> for " + scheduledContainers.getFirst().getDuration() + " seconds");
            this.executeState(scheduledContainers.getFirst().getContainer());
            this.scheduledContainers.getFirst().start();
            return;
        }

        if (this.scheduledContainers.getFirst().isExpired()) {//current state done
            log.info("State <" + scheduledContainers.getFirst().getName() + "> expired!");
            this.scheduledContainers.removeFirst();
            if (this.scheduledContainers.isEmpty()) {//no more scheduled states
                log.info("Schedule is empty");
                blockedUntil = null;
                if (this.savedState != null) {//we have saved state and coming back to it
                    log.info("Executing saved state");
                    this.executeState(this.savedState);
                    this.savedState = null;
                } else//we don't have a saved state, so we will stay by the last one
                    log.info("Last executed state remains");
            } else {//executing next scheduled state
                log.info("Executing state " + scheduledContainers.getFirst().getName() + " for " + scheduledContainers.getFirst().getDuration() + " seconds");
                this.executeState(this.scheduledContainers.getFirst().getContainer());
                this.scheduledContainers.getFirst().start();
            }
        }
    }

    /**
     * Produces a new container with the states from a given container, where traffic lights
     * with emergency state are taken. This will ensure, that emergency state will not
     * be overwritten accidentally.
     *
     * @param sc - state container with a new state for the intersection
     * @return copy of sc or copy of sc combined with emergency states
     */
    private StatesContainer adjustEmergency(StatesContainer sc) {
        StatesContainer result = new StatesContainer();
        for (UUID id : lightsIds) {
            if (trafficLightsRegister.get(id).getState() == TrafficLightState.EMERGENCY)
                result.addState(id, TrafficLightState.EMERGENCY);
            else
                result.addState(id, sc.getState(id));
        }
        return result;
    }

    /**
     * Changes the TrafficLightState of the respective UUID to green and all others to red.
     * UUID has to belong to a vehicle trafficLight
     *
     * @param id contains the UUID of the TrafficLight which state should be changed to green
     * @return returns a LocalDateTime indicating how long the intersection is blocked for new greenLightRequests
     * @throws IllegalArgumentException if the intersection does not contain the specified UUID
     * @throws IllegalArgumentException if the specified UUID belongs to a pedestrianTrafficLight
     * @throws IllegalStateException    if the TrafficLight is in emergency mode
     * @throws IllegalStateException    if the intersection is blocked due to another emergency request
     */
    //TrafficLight will be processed, also if it is green (because all others should be red)
    public LocalDateTime setGreenLight(@NonNull UUID id, String issuer) {
        log.info("Got a green light request for " + id + " from " + issuer);
        if (!issuer.equals("mayor") && !issuer.equals("emergency-vehicle")) {
            throw new IllegalArgumentException("Issuer: " + issuer + " not known");
        }

        if (!this.trafficLightsRegister.containsKey(id)) {
            throw new IllegalArgumentException("Traffic light " + id + " is not attached to this intersection");
        }

        if (this.trafficLightsRegister.get(id) instanceof PedestrianTrafficLight) {
            throw new IllegalArgumentException("Traffic light " + id + " is a pedestrian traffic light. Change state of pedestrian traffic lights is not supported.");
        }

        if (this.trafficLightsRegister.get(id).getState() == TrafficLightState.EMERGENCY) {
            throw new IllegalArgumentException("Traffic light " + id + "in emergency mode!");
        }

        if (blockedUntil != null && blockedUntil.isAfter(LocalDateTime.now())) {

            if ((blockedBy.equals("mayor") || blockedBy.equals("tmc")) && issuer.equals("emergency-vehicle")) { //emergency has priority over mayor
                log.info("The intersection is blocked by mayor, but it will be canceled due to prioritisation.");
            } else {
                throw new IllegalStateException("Intersection is blocked due another emergency request until " + blockedUntil.toString());
            }

        }

        lookingForStateToReturnTo();

        transitionTheState(id);

        blockIntersection(issuer);


        return blockedUntil;
    }

    /**
     * Checks to which trafficLightState constellation the intersection should return after the Green light request is over
     */
    public void lookingForStateToReturnTo() {
        if (this.savedState == null) {
            if (!scheduledContainers.isEmpty()) {
                log.debug("We don't have saved state. But we tried to achieve another state. Let’s save it.");
                this.savedState = scheduledContainers.getLast().getContainer();
            } else {
                log.debug("Saving current state of intersection, so we can come back.");
                this.savedState = this.getCurrentState(); // save only if intersection is not blocked
            }
        } else {
            log.debug("We already have saved state of intersection and will keep it.");
        }
    }

    /**
     * Manages the transition from the trafficLightState start constellation to the end constellation
     * I.e. a constellation with a yellow phase
     *
     * @param id of the trafficLight for which the greenLightRequest was issued
     */
    public void transitionTheState(@NonNull UUID id) {
        StatesContainer goalState = this.adjustEmergency(this.greenSafeStates.get(id));
        StatesContainer transition;
        StatesContainer transitionBack;
        try {
            transition = this.getTransition(this.getCurrentState(), goalState);
            transitionBack = this.getTransition(goalState, this.savedState);
        } catch (Exception e) {
            log.debug("Transition to the the goal state impossible due following error: " + e.getMessage());
            throw e;
        }

        if (scheduledContainers.size() > 0) {
            log.debug("There are some scheduled states at the intersection, which will be deleted.");
            while (scheduledContainers.size() > 1) {
                scheduledContainers.removeLast();
            }
            if (!scheduledContainers.getFirst().isStarted()) {
                scheduledContainers.removeFirst();
                log.debug("All scheduled states are deleted now.");
            } else {
                log.debug("One state is executing right now, can't delete it. We have wait until it is finished.");
            }
        }

        if (transition == null) { //transition not needed - we can execute immediately
            this.scheduleState(new ScheduledStatesContainer(goalState, "Emergency-green", this.greenPhaseDuration));
        } else { //transition and back-transition needed, lets execute it
            log.debug("Need a transition to execute the goal state");
            this.scheduleState(new ScheduledStatesContainer(transition, "Transition from normal to emergency green", this.transitionPhaseDuration));
            this.scheduleState(new ScheduledStatesContainer(goalState, "Emergency-green ", this.greenPhaseDuration));
        }

        if (transitionBack != null) {
            log.debug("Need a transition to come back to saved state");
            this.scheduleState(new ScheduledStatesContainer(transitionBack, "Transition to a saved state", this.transitionPhaseDuration));
        }
    }

    /**
     * Calculates the time an intersection has to be blocked for further requests.
     * This time is derived from the time the green light phase is active after the GreenLightRequest
     *
     * @param issuer Entity form which the GreenLightRequest was issued
     */
    public void blockIntersection(String issuer) {
        LocalDateTime total = scheduledContainers.getFirst().expiresAt();
        if (total == null)
            total = LocalDateTime.now();
        for (ScheduledStatesContainer s : scheduledContainers) {
            if (!s.isStarted()) {
                total = total.plusSeconds(s.getDuration());
            }
        }
        log.info("The intersection suppose to be blocked until: " + total);

        this.blockedUntil = total;
        this.blockedBy = issuer;
    }


    /**
     * adds a new TrafficLightState constellation to the queue of TrafficLightState constellations. After executing, changing to a new state is
     * not possible for 10 sec.
     *
     * @param req TrafficLightStateChangeRequest containing the TrafficLightState constellation that should be achieved
     * @return return boolean indicating that the stateChangeRequest was successful
     * @throws IllegalStateException    if the intersection blocked due to an emergency request
     * @throws IllegalArgumentException if the size of the request is wrong
     * @throws IllegalArgumentException if the state for the trafficLight is null
     * @throws IllegalArgumentException if the constellation does not compatible with the TrafficLightRules of this intersection
     */
    //If TMC sends new states, they will be processed here
    public LocalDateTime setTrafficLightState(@NonNull TrafficLightStateChangeRequest req) {

        if (blockedUntil != null) { //Intersection is currently blocked
            throw new IllegalStateException("Intersection is blocked due emergency request until" + blockedUntil);
        }

        try {//Validating the states in the request
            if (req.getStates().size() != lightsIds.size())
                throw new IllegalArgumentException("Size of the request is wrong");

            for (UUID id : this.lightsIds) {
                try {
                    if (req.getState(id) == null)
                        throw new IllegalArgumentException("State for the traffic light " + id + " is null");
                } catch (NoSuchElementException e) {
                    throw new IllegalArgumentException("Traffic light " + id + " is required but not found");
                }
            }

            for (TrafficLightRule rule : this.stateRules) {
                if (!rule.isValid(req))
                    throw new IllegalArgumentException("Rule: " + rule.getShortDescription() + " is invalid");
            }

            for (ForbiddenTransitionRule rule : this.transitionRules) {
                if (!rule.isValid(req, trafficLightsRegister))
                    throw new IllegalArgumentException("Rule: " + rule.getShortDescription() + " is invalid");
            }

        } catch (Exception e) { //Handling exceptions while validating of the dataset
            log.error("Error occurred while processing state change request \n" +
                    "   issuer: " + req.getIssuer() + " timestamp: " + req.getTimestamp() +
                    "\n error: " + e.getMessage());
            throw e;
        }

        //at this point the set is successfully validated
        //check if transition is needed
        StatesContainer goalState = this.adjustEmergency(req);
        StatesContainer transition = this.getTransition(this.getCurrentState(), goalState);

        if (transition != null) { //transition needed, lets execute it and then the state
            log.info("State will be executed with transition");
            this.scheduleState(new ScheduledStatesContainer(transition, "Transition to new intersection state", this.transitionPhaseDuration));
            this.blockedUntil = LocalDateTime.now().plusSeconds(this.transitionPhaseDuration);
        } else {
            log.info("State will be executed without transition");
        }
        this.scheduleState(new ScheduledStatesContainer(goalState, "New intersection state",req.getMinDuration()));
        this.blockedUntil = this.blockedUntil.plusSeconds(req.getMinDuration());
        this.savedState = goalState;//We will save the wanted state in case of interruption
        this.blockedBy = "tmc"; //We have only UUID in the request, but it's not helpful for roles prioritisation

        return this.blockedUntil;
    }

    /**
     * checks if a transitional TrafficLightState constellation is needed to achieve the desired TrafficLightState constellation
     * from a former TrafficLightState constellation (i.e. taking into account that a vehicle trafficLight might need a transitional
     * yellow phase)
     * And if so creates the transitional TrafficLightState constellation
     *
     * @param from StatesContainer containing a TrafficLightState constellation which should change into the desired TrafficLightState constellation
     * @param to   StatesContainer containing the desired TrafficLightState constellation
     * @return transitional TrafficLightState constellation or null if non is needed
     */

    //Looking if a state can be executed directly or looking for possible transition to execute it
    private StatesContainer getTransition(StatesContainer from, StatesContainer to) {
        StatesContainer res = new StatesContainer();
        boolean changed = false;
        TrafficLightState state;

        for (UUID id : this.lightsIds) {
            //For each traffic light getting transition from its current state to goal-state
            //TrafficLightState.getTransition throws exceptions, if arguments are bad
            if (this.trafficLightsRegister.get(id) instanceof VehicleTrafficLight) {
                try {
                    state = TrafficLightState.getTransition(from.getState(id), to.getState(id));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Illegal state for traffic light " + id + " " + e.getMessage());
                }
            } else //if we have a pedestrian traffic light, we can switch directly
                state = to.getState(id);
            res.addState(id, state);

            //Checking, if transition state is the same with goal-state
            if (!res.getState(id).equals(to.getState(id)))
                changed = true;
        }

        if (changed) //we found transition, which we have to execute before goal-state
            return res;
        else  //no transition needed, goals-states can be executed immediately
            return null;
    }

    /**
     * retrieves information about the intersection
     */
    public JsonObject getInfo() {
        return new JsonObject()
                .put("Info", this.info)
                .put("Latitude", latitude)
                .put("Longitude", longitude)
                .put("ID", this.intersectionId)
                .put("trafficLightsConnected", trafficLightsRegister.size())
                .put("blockedUntil", this.blockedUntil);
    }

}
