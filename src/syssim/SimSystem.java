package syssim;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the base class behind system
 * @author srinath
 *
 */
public class SimSystem {
	AtomicInteger participantIndexCounter = new AtomicInteger();
	private static ConcurrentHashMap<Integer, Participant> participantMap = new ConcurrentHashMap<Integer, Participant>();
	private static ConcurrentHashMap<Integer, EventClient> eventClientMap = new ConcurrentHashMap<Integer, EventClient>();
	
    private ExecutorService pool = Executors.newFixedThreadPool(50);


	
	public Participant createParticipant(Participant.EventListener listener) throws SysSimException{
		int participantIndex = participantIndexCounter.incrementAndGet();
		Participant participant = new Participant(listener, this, 4444 + participantIndex); 
		pool.submit(participant);
		participantMap.put(participant.getID(), participant);
		eventClientMap.put(participant.getID(), new EventClient("127.0.0.1", participant.getPort()));
		return participant;
	}
	
	
	public void bootUp(){
		Iterator<Participant> iterator = participantMap.values().iterator();
		while(iterator.hasNext()){
			iterator.next().getListener().participantStarted(this);
		}

	}
	
	/**
	 * Send a message to participant having given Pid. Message is array of strings
	 * @param pid
	 * @param message
	 */

	public void sendMessage(int pid, String[] message){
		eventClientMap.get(pid).sendMessage(message);
	}
	
	/**
	 * Broadcast message to all participants in the system
	 * @param message
	 */
	
	public void broadcastMessage(String[] message){
		Iterator<EventClient> iterator = eventClientMap.values().iterator();
		while(iterator.hasNext()){
			EventClient participant = iterator.next();
			participant.sendMessage(message);
		}
	}
}
