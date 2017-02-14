package net.mgsx.game.core;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.Array;

import net.mgsx.game.core.storage.EngineStorage;
import net.mgsx.game.core.storage.EntityGroup;
import net.mgsx.game.core.storage.EntityGroupLoader;
import net.mgsx.game.core.storage.EntityGroupStorage;
import net.mgsx.game.core.storage.LoadConfiguration;

/**
 * Base screen for managed game scene.
 * Has its own Entity Engine.
 * 
 * @author mgsx
 *
 */
public class GameScreen extends ScreenAdapter
{
	final public AssetManager assets;
	
	final private Array<String> pendingGroups = new Array<String>();
	
	final public Engine entityEngine;
	public GameRegistry registry;
	
	public Camera camera;
	
	/**
	 * create game screen with default engine.
	 * @param assets
	 * @param registry
	 */
	public GameScreen(AssetManager assets, GameRegistry registry) {
		this(assets, registry, new PooledEngine());
	}
	
	public GameScreen(AssetManager assets, GameRegistry registry, Engine engine) {
		super();
		this.assets = assets;
		this.registry = registry;
		this.entityEngine = engine;
		
		// TODO default camera with magic numbers ... how to configure ?
		Camera gameCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gameCamera.position.set(0, 0, 10);
		gameCamera.up.set(0,1,0);
		gameCamera.lookAt(0,0,0);
		gameCamera.near = 1f;
		gameCamera.far = 3000f;
		gameCamera.update(true);
		
		camera = gameCamera;
		
		registry.init(this);
	}
	
	@Override
	public void render(float delta) 
	{
		// load pending entity groups in engine.
		while(pendingGroups.size > 0){
			EntityGroupStorage.get(assets, entityEngine, pendingGroups.removeIndex(0));
		}
		
		// run the engine
		entityEngine.update(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void resize(int width, int height) 
	{
		super.resize(width, height);
		
		camera.viewportWidth = Gdx.graphics.getWidth();
		camera.viewportHeight = Gdx.graphics.getHeight();
		camera.update(true);
	}

	public void loadSettings(FileHandle file) 
	{
		LoadConfiguration config = new LoadConfiguration();
		config.assets = assets;
		config.engine = entityEngine;
		config.failSafe = false;
		config.registry = registry;
		
		// TODO load in background
		EngineStorage.load(file, config);
	}
	
	public void load(FileHandle file) 
	{
		if(file != null){
			assets.setLoader(EntityGroup.class, new EntityGroupLoader(assets.getFileHandleResolver(), registry));
			
			String name = file.path();
			pendingGroups.add(name);
			
			LoadConfiguration config = new LoadConfiguration();
			config.assets = assets;
			config.registry = registry;
			config.engine = entityEngine;
			
			// TODO load settings as well !
			
			EntityGroupStorage.load(name, config);
		}
	}

}
