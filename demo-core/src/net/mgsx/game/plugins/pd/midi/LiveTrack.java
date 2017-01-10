package net.mgsx.game.plugins.pd.midi;

import com.badlogic.gdx.utils.Array;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.ProgramChange;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TrackName;
import com.leff.midi.util.MidiEventListener;

public class LiveTrack extends MidiLooper
{
	public long loopStart, loopEnd, trackEnd;
	public int nextLoopStart, nextLoopEnd, modulus;
	public boolean loop = false;
	public boolean nextLoop = false;
	public int index, loopStartIndex;
	public int resolution;
	private long offset;
	private long virtualPosition;
	
	final private Array<MidiEvent> events;
	
	private int [] notes = new int[127 * 16];
	private int [] notesOnIndices = new int[127 * 16];
	private int notesOnCount = 0;
	
	private ResetNote off = new ResetNote();
	private final LiveSequencer master;
	
	public LiveTrack(LiveSequencer master, MidiFile file, MidiTrack track, MidiEventListener listener) {
		super(listener);
		this.master = master;
		events = new Array<MidiEvent>();
		for(MidiEvent e : track.getEvents()){
			events.add(e);
			if(e instanceof TrackName){
				name = ((TrackName) e).getTrackName();
			}
		}
		loopStart = 0;
		trackEnd = loopEnd = track.getLengthInTicks();
		index = loopStartIndex = 0;
		resolution = file.getResolution();
		prePos = 0;
	}
	
	@Override
	public void replace(MidiLoop loop) 
	{
		// setLoop(loop.start, loop.end);
		super.replace(loop);
	}
	
	private void setLoop(long startTick, long endTick){
		loopStart = startTick;
		loopEnd = endTick;
		// find index
		for(int i=0 ; i<events.size ; i++){
			MidiEvent nextEvent = events.get(i);
			if(nextEvent.getTick() >= loopStart){
				loopStartIndex = i;
				break;
			}
		}
		index = loopStartIndex;
		prePos = 0;
		sendNotesOff();
		loop = true;
		offset = 0;
	}
	public void setLoop(int startBeat, int endBeat, int modulus){
		nextLoopStart = startBeat;
		nextLoopEnd = endBeat;
		nextLoop = true;
		this.modulus = modulus;
	}
	
	long prePos;
	boolean active = true;
	private boolean running = false;
	public String name;
	
	private void sendProgramChange(long position) {
		for(int i=0 ; i<events.size ; i++){
			MidiEvent nextEvent = events.get(i);
			if(nextEvent instanceof ProgramChange && nextEvent.getTick() <= position){
				listener.onEvent(nextEvent, 0);
			}
		}
	}

	public void mute(boolean on) {
		active  = on;
		if(!active) sendNotesOff();
	}

	// TODO sync ?
	public void unloop() {
		if(loop){
			long loopLen = loopEnd - loopStart;
			long localPos = ((virtualPosition % loopLen) + loopLen) % loopLen;
			offset = loopStart + localPos - virtualPosition;
			loop = false;
		}
	}

	public int endBeat() {
		return (int)(events.get(events.size-1).getTick() / resolution);
	}

	public void sendNotesOff() {
		
		for(int i=0 ; i<notesOnCount ; i++){
			int index = notesOnIndices[i];
			int note = index & 0x7F;
			int channel = index >> 7;
			if(notes[index] != 0){
				listener.onEvent(off.set(channel, note), 0);
				notes[index] = 0;
			}
		}
		notesOnCount = 0;
	}

	public Array<MidiEvent> getEvents() {
		return events;
	}

	public void reset() 
	{
		running  = false;
	}

	@Override
	protected void loopChanged(MidiLoop loop) {
		sendNotesOff();
		if(loop.start == null){
			index = 0;
		}else{
			// find index : TODO binary search ?
			for(index = 0 ; index < events.size ; index++){
				MidiEvent nextEvent = events.get(index);
				if(nextEvent.getTick() >= loop.start){
					break;
				}
			}
		}
		System.out.println(index);
	}

	@Override
	protected void process(long tickMin, long tickMax) 
	{
		while(index < events.size){
			MidiEvent nextEvent = events.get(index);
			if(nextEvent.getTick() >= tickMin && nextEvent.getTick() < tickMax){
				listener.onEvent(nextEvent, 0);
				if(nextEvent instanceof NoteOn){
					NoteOn e = ((NoteOn) nextEvent);
					int index = (e.getChannel() << 7) | e.getNoteValue();
					int value = e.getVelocity();
					if(notes[index] == 0 && value > 0){
						notes[index] = value;
						notesOnIndices[notesOnCount++] = index;
					}
				}else if(nextEvent instanceof Tempo){
					master.bpm = ((Tempo) nextEvent).getBpm();
				}
				// System.out.println(nextEvent);
				index++;
			}else{
				break;
			}
		}
		
	}
}