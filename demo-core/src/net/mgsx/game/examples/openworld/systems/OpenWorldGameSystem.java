package net.mgsx.game.examples.openworld.systems;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import net.mgsx.game.core.GamePipeline;
import net.mgsx.game.core.PostInitializationListener;
import net.mgsx.game.core.annotations.Editable;
import net.mgsx.game.core.annotations.EditableSystem;
import net.mgsx.game.core.annotations.Inject;
import net.mgsx.game.examples.openworld.model.OpenWorldElement;
import net.mgsx.game.examples.openworld.model.OpenWorldGame;
import net.mgsx.game.examples.openworld.model.OpenWorldModel;
import net.mgsx.game.examples.openworld.model.OpenWorldPlayer;
import net.mgsx.game.services.gapi.GAPI;
import net.mgsx.game.services.gapi.SavedGame;

/**
 * Saving is done synchronously while loading is done on this system update in order to
 * properly load other systems and avoid glitches.
 * 
 * TODO add threading support and callback.
 * 
 * @author mgsx
 *
 */
@EditableSystem
public class OpenWorldGameSystem extends EntitySystem implements PostInitializationListener
{
	@Inject OpenWorldCameraSystem cameraSystem;
	@Inject OpenWorldGeneratorSystem generator;
	@Inject OpenWorldManagerSystem manager;
	@Inject UserObjectSystem userObjectSystem;
	@Inject OpenWorldHUDSystem hudSystem;
	@Inject OpenWorldEnvSystem env;
	
	@Editable(realtime=true, readonly=true)
	public transient boolean diving, walking, flying, swimming;
	
	public Array<OpenWorldElement> backpack = new Array<OpenWorldElement>();
	
	private SavedGame gameToLoad;
	
	// TODO move player logic to dedicated system ? only keep game loading/saving logic here.
	// things need environement system to be computed first (specially at loading time)
	// , then player logic can apply, then HUD can display correct values.
	public OpenWorldPlayer player;
	
	public OpenWorldGameSystem() {
		super(GamePipeline.FIRST);
	}
	
	@Override
	public void onPostInitialization() {
		// XXX init workaround
		if(backpack.size == 0){
			backpack.add(OpenWorldModel.generateNewElement("machete"));
		}
		player = new OpenWorldPlayer();
		
		computePlayerStats();
		
		// compute inital values
		player.energy = player.energyMax;
		player.life = player.lifeMax;
		player.oxygen = player.oxygenMax;
		
		// TODO some values should be updated in realtime
	}
	
	/**
	 * compute level based on XP (see {@link #experience(int)}
	 * @param experience
	 * @return
	 */
	public static int level(long experience){
		return (int)Math.sqrt(experience/100 + 0.5) + 1;
	}
	/**
	 * compute XP based on level.
	 * Common gaming is to use square to have linear delta between level.
	 * Values are :
	 * LV1 :    0 XP
	 * LV2 :  100 XP (+100)
	 * LV3 :  400 XP (+300) (++200)
	 * LV4 :  900 XP (+500) (++200)
	 * LV5 : 1600 XP (+700) (++200)
	 * LV6 : 2500 XP (+900) (++200)
	 * ...
	 * @param level
	 * @return
	 */
	public static long experience(int level){
		long base = (long)level - 1;
		return 100L * base * base;
	}
	
	private void computePlayerStats() 
	{
		// first compute level based on experience
		player.level = level(player.experience);
		
		// TODO maybe adjust base to have something realistic ? (oxygen ...)
		// derived max values from level
		player.energyMax = player.level + 3;
		player.lifeMax = player.level + 3;
		player.oxygenMax = player.level + 3;
		
		// init basics values
		// these values are based not based on scientific stuff and limits depends
		// on exposure time, air/water ...etc. But we could consider that
		// player loose 1 life point every hour :
		// * per degree below 10°
		// * per degree above 50°
		player.temperatureMax = 50;
		player.temperatureMin = 10;
		
		// TODO modify basic values based on equipment (iterate from backpack).
		
		// XXX reset stats
		player.life = player.lifeMax;
		player.energy = player.energyMax;
		player.oxygen = player.oxygenMax;
		player.temperature = 37.2;
	}

	private void load(SavedGame game)
	{
		InputStream stream = GAPI.service.loadGame(game);
		
		Json json = new Json();
		
		OpenWorldGame gameData = json.fromJson(OpenWorldGame.class, stream);
		
		cameraSystem.getCamera().position.set(gameData.position);
		
		generator.seed = gameData.seed;
		generator.reset();
		
		manager.clear();
		
		userObjectSystem.removeAllElements();
		if(gameData.objects != null){
			for(OpenWorldElement element : gameData.objects){
				OpenWorldModel.generateElement(element);
				userObjectSystem.appendObject(element);
			}
		}
		
		backpack.clear();
		if(gameData.backpack != null){
			for(OpenWorldElement element : gameData.backpack){
				OpenWorldModel.generateElement(element);
				backpack.add(element);
			}
		}
		
		player = gameData.player;
		if(player == null){
			player = new OpenWorldPlayer();
		}
		computePlayerStats();
		
		// finally invalidate GUI
		hudSystem.hudMain.resetState();
	}
	
	public void save(SavedGame game){
		
		OpenWorldGame gameData = new OpenWorldGame();
		
		// store camera position and seed
		gameData.position.set(cameraSystem.getCamera().position);
		gameData.seed = generator.seed;
		
		// store objects
		gameData.objects = new OpenWorldElement[userObjectSystem.allUserObjects.size];
		for(int i=0 ; i<gameData.objects.length ; i++){
			gameData.objects[i] = userObjectSystem.allUserObjects.get(i).element;
		}
		
		gameData.backpack = new OpenWorldElement[backpack.size];
		for(int i=0 ; i<gameData.backpack.length ; i++){
			gameData.backpack[i] = backpack.get(i);
		}
		
		gameData.player = player;
		
		// serialize
		Json json = new Json();
		StringWriter writer = new StringWriter();
		json.toJson(gameData, writer);
		
		InputStream data = new ByteArrayInputStream(writer.toString().getBytes());
		
		GAPI.service.saveGame(game, data);
	}

	public void loadRequest(SavedGame game) {
		gameToLoad = game;
	}
	
	@Override
	public void update(float deltaTime) 
	{
		if(gameToLoad != null){
			load(gameToLoad);
			gameToLoad = null;
		}else{
			
			Camera camera = cameraSystem.getCamera();
			if(camera == null) return;
			
			final float playerSize = cameraSystem.offset;
			
			float altitude = generator.getAltitude(camera.position.x, camera.position.z);
			
			boolean isAquatic = altitude < env.waterLevel;
			diving = isAquatic && camera.position.y < env.waterLevel;
			if(cameraSystem.flyingMode){
				flying = !diving;
				walking = false;
				swimming = false;
			}
			else{
				flying = false;
				swimming = !diving && isAquatic && camera.position.y - playerSize > altitude;
				walking = !swimming && !diving && cameraSystem.currentMove != 0;
			}
			
			// update player logic
			float currentMove = cameraSystem.currentMove;
			
			// TODO use scentific values : nb km per hour, ...Etc.
			
			// in water then update oxygen
			if(diving){
				player.oxygen -= deltaTime * 0.5;
				if(player.oxygen < 0){
					player.oxygen = 0;
					player.life -= deltaTime * 1;
				}
			}else{
				player.oxygen += deltaTime * 2;
				if(player.oxygen > player.oxygenMax){
					player.oxygen = player.oxygenMax;
				}
			}
			
			// update temperature from environement and time ...
			player.temperature += (env.temperature - player.temperature) * deltaTime * 0.05;
			
			if(player.temperature < player.temperatureMin){
				player.energy -= deltaTime * 0.1;
			}
			if(player.temperature > player.temperatureMax){
				player.energy -= deltaTime * 0.1;
			}
			
			// lose more energy by moving
			if(walking){
				player.energy -= currentMove * 0.001;
			}else if(swimming || diving){
				player.energy -= currentMove * 1;
			}else if(flying){
				player.energy -= currentMove * .5;
			}else{
				// loose energy in all cases
				player.energy -= deltaTime * 0.0000001;
			}
			
			if(player.energy < 0){
				player.energy = 0;
				player.life -= deltaTime * 0.01;
			}else if(player.energy >= player.energyMax){
				player.life += deltaTime * 0.01;
				if(player.life > player.lifeMax) player.life = player.lifeMax;
			}
			
			if(player.life < 0){
				player.life = 0;
				// TODO death sequence ...
			}
		}
	}

	public int getAvailableSpaceInBackpack() 
	{
		return getTotalSpaceInBackpack() - getUsedSpaceInBackpack();
	}

	public int getUsedSpaceInBackpack() {
		int sum = 0;
		for(OpenWorldElement item : backpack){
			sum += OpenWorldModel.space(item.type);
		}
		return sum;
	}

	public int getTotalSpaceInBackpack() {
		// TODO this value could be upgraded in some way ...
		return 16;
	}

	
}
