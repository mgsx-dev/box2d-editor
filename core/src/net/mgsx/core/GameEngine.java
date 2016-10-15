package net.mgsx.core;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import net.mgsx.core.plugins.Plugin;
import net.mgsx.core.plugins.StorablePlugin;

// TODO
// shoulb be a screen (because of multi screen features or multi viewport ...
// it only contains high performence / no editor stuff runtime!
public class GameEngine extends ApplicationAdapter
{
	public AssetManager assets;
	protected ShapeRenderer shapeRenderer;
	protected SpriteBatch batch;
	protected Array<Plugin> plugins = new Array<Plugin>();
	public PooledEngine entityEngine;
	public OrthographicCamera orthographicCamera;
	public PerspectiveCamera perspectiveCamera;
	
	public void registerPlugin(Plugin plugin) {
		plugins.add(plugin);
	}

	private Json json;

	public <T> void registerPlugin(Class<T> type, StorablePlugin<T> plugin) {
		json.setSerializer(type, plugin);
	}
	
	@Override
	public void create() 
	{
		super.create();
		assets = new AssetManager(); // TODO resolver maybe different for game and editor ?
		Texture.setAssetManager(assets);
		entityEngine = new PooledEngine();
		orthographicCamera = new OrthographicCamera();
		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		
		perspectiveCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		perspectiveCamera.position.set(0, 0, 10);
		perspectiveCamera.up.set(0,1,0);
		perspectiveCamera.lookAt(0,0,0);
		perspectiveCamera.near = 1f;
		perspectiveCamera.far = 3000f;
		perspectiveCamera.update();
	}
	
	@Override
	public void render() {
		
		perspectiveCamera.position.set(orthographicCamera.position.x, orthographicCamera.position.y, 3); // TODO ortho factor ?
//		perspectiveCamera.lookAt(0,0,0);
		perspectiveCamera.update();
		super.render();
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		perspectiveCamera.viewportWidth = Gdx.graphics.getWidth();
		perspectiveCamera.viewportHeight = Gdx.graphics.getHeight();
		
		perspectiveCamera.update();
	}
	

}
