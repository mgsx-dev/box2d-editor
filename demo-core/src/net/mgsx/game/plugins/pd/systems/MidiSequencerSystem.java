package net.mgsx.game.plugins.pd.systems;

import java.io.IOException;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.meta.Marker;

import net.mgsx.game.core.GamePipeline;
import net.mgsx.game.core.GameScreen;
import net.mgsx.game.core.annotations.Editable;
import net.mgsx.game.core.annotations.EditableSystem;
import net.mgsx.game.plugins.pd.midi.LiveSequencer;
import net.mgsx.game.plugins.pd.midi.LiveTrack;
import net.mgsx.game.plugins.pd.midi.MidiLoop;
import net.mgsx.game.plugins.pd.midi.PdMidiSynth;
import net.mgsx.game.plugins.pd.midi.QTractorSequenceReader;
import net.mgsx.game.plugins.pd.midi.SequenceDesc;
import net.mgsx.game.plugins.pd.midi.SequenceMarker;
import net.mgsx.pd.patch.PatchLoader;
import net.mgsx.pd.patch.PdPatch;

@EditableSystem
public class MidiSequencerSystem extends EntitySystem
{
	public LiveSequencer sequencer;
	public SequenceDesc sequenceDesc;
	private GameScreen game;
	
	
	
	@Editable
	public void play(){
		
		MidiLoop loop = new MidiLoop();
		loop.start = 0L;
		loop.end = (long)(96 * 4 * 4);
		for(LiveTrack track : sequencer.getTracks()){
			track.replace(loop);
		}
		
		sequencer.play();
	}
	@Editable
	public void stop(){
		sequencer.stop();
	}
	
	public MidiSequencerSystem(GameScreen game) {
		super(GamePipeline.AFTER_LOGIC);
		this.game = game;
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		sequencer = new LiveSequencer(new PdMidiSynth());
		
		try {
			
			game.assets.setLoader(PdPatch.class, "pd", new PatchLoader(game.assets.getFileHandleResolver()));
			
			AssetDescriptor<PdPatch> patchAsset = new AssetDescriptor<PdPatch>("pdmidi/midiplayer.pd", PdPatch.class);
			
			game.assets.load(patchAsset);
			game.assets.finishLoading();
			
			
			sequenceDesc = new QTractorSequenceReader().read(Gdx.files.internal("MuteCity.qtr"));
			MidiFile midiFile = new MidiFile(Gdx.files.internal("MuteCity.mid").file());
			sequencer.load(midiFile);
			
			for(MidiTrack track : midiFile.getTracks()){
				for(MidiEvent e : track.getEvents()){
					if(e instanceof Marker){
						SequenceMarker marker = new SequenceMarker();
						marker.name = ((Marker) e).getMarkerName();
						marker.tick = e.getTick();
						sequenceDesc.markers.put(marker.name, marker);
					}
				}
			}
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	public void update(float deltaTime) 
	{
		// TODO ...
	}
}