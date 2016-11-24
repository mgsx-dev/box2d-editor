package net.mgsx.game.core.storage;

import java.io.IOException;
import java.io.Writer;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.game.core.GameRegistry;
import net.mgsx.game.core.GameScreen;
import net.mgsx.game.core.components.Initializable;
import net.mgsx.game.core.components.Movable;
import net.mgsx.game.core.components.Repository;
import net.mgsx.game.core.helpers.EntityHelper;
import net.mgsx.game.plugins.core.components.ProxyComponent;
import net.mgsx.game.plugins.core.components.Transform2DComponent;

/**
 * Storage service.
 * 
 * A component could implements Serializable interface in that way :
 * <pre>
 * 
 * 
	public void write(Json json) 
	{
		json.writeField(this, "life");
		json.writeField(this, "points");
	}

	@Override
	public void read(Json json, JsonValue jsonData) 
	{
		json.readField(this, "life", jsonData);
		json.readField(this, "points", jsonData);
		time = 0; // init not persisted data
	}
	</pre>
 * 
 * or you can register a serializer :
 * 
 * {@link GameScreen#addSerializer(Class, Serializer)} :
 * <pre>
 * GameEngine.addSerializer(MyType.class, new MySerializer());
 * </pre>
 * or any builtin serializers :
 * <pre>
 * GameEngine.addSerializer(MyType.class, new IgnoreSerializer<MyType>());
 * GameEngine.addSerializer(MyType.class, new AnnotationSerializer<MyType>()); TODO ?
 * </pre>
 * @author mgsx
 *
 */
public class EntityGroupStorage 
{
	/**
	 * load entity group for editing, all entities in group will be Repository, any other
	 * referenced entities (from proxy ...) won't be repository.
	 * Entities are automatically added to the engine.
	 * @param assets
	 * @param registry
	 * @param fileName
	 * @return
	 */
	public static Array<Entity> loadForEditing(AssetManager assets, GameRegistry registry, Engine engine, String fileName)
	{
		// first clone entity group and add repository flag.
		// then create transients (referenced from proxies.
		EntityGroup group = loadNow(assets, registry, fileName);
		Array<Entity> entities = new Array<Entity>();
		EntityGroup clones = new EntityGroup();
		for(Entity template : group.entities)
		{
			// clone template and add repository flag
			Entity entity = EntityHelper.clone(engine, template, group, clones, true);
			Repository repository = new Repository(); // no pool here (not required ?)
			entity.add(repository);
			
			// in case of proxy, create referenced entities recursively as transient (non repository)
			ProxyComponent proxy = ProxyComponent.components.get(entity);
			if(proxy != null){
				EntityGroup proxyGroup = assets.get(proxy.ref, EntityGroup.class);
				proxy.template = proxyGroup;
				proxy.clones = create(entities, assets, engine, proxyGroup, entity);
			}
			
			entities.add(entity);
		}
		return entities;
	}

	/**
	 * Load entities as transient, that is without persistence information. These entities
	 * won't be saved in patch files.
	 * 
	 * @param assets
	 * @param registry
	 * @param engine
	 * @param fileName
	 * @return
	 */
	public static Array<Entity> loadTransient(AssetManager assets, GameRegistry registry, Engine engine, String fileName)
	{
		EntityGroup group = loadNow(assets, registry, fileName);
		Array<Entity> entities = new Array<Entity>();
		create(entities, assets, engine, group, null);
		return entities;
	}

	/**
	 * load entity group as proxy : all entities are loaded but not persisted.
	 * an additional proxy entity is created and persisted
	 * @param assets
	 * @param registry
	 * @param engine
	 * @param fileName
	 * @return all entities added to the engine, at least the proxy entity which is the
	 * only entity having proxy component, this is always the last component.
	 */
	public static Array<Entity> loadAsProxy(AssetManager assets, GameRegistry registry, Engine engine, String fileName, Vector2 position) 
	{
		// case of proxy : we load entities as non persited but we create a proxy entity which will be stored.
		// create the special proxy entity
		Entity proxyEntity = engine.createEntity();
		ProxyComponent proxy = engine.createComponent(ProxyComponent.class);
		proxy.ref = fileName; // TODO could use AssetSerializer ?
		proxyEntity.add(proxy);
		proxyEntity.add(engine.createComponent(Repository.class));
		Transform2DComponent transform = engine.createComponent(Transform2DComponent.class);
		transform.position.set(position);
		proxyEntity.add(transform);
		
		// first instanciate group and remove any proxy components (de reference)
		EntityGroup group = loadNow(assets, registry, fileName);
		proxy.template = group;
		
		Array<Entity> entities = new Array<Entity>();
		proxy.clones = create(entities, assets, engine, group, proxyEntity);
		for(Entity entity : entities){
			entity.remove(ProxyComponent.class);
		}
		
		entities.add(proxyEntity);
		engine.addEntity(proxyEntity);
		
		return entities;
	}
	
	/**
	 * load an entity group ASAP forcing assets to be loaded now.
	 * @param assets
	 * @param registry
	 * @param fileName
	 * @return
	 */
	public static EntityGroup loadNow(AssetManager assets, GameRegistry registry, String fileName)
	{
		load(assets, registry, fileName);
		assets.finishLoadingAsset(fileName);
		return assets.get(fileName, EntityGroup.class);
	}
	
	/**
	 * load entity group in background (normal in game loading).
	 * entities can be instanciated by calling {@link #get(AssetManager, Engine, String)} method once asset manager
	 * finished loading this asset.
	 * @param assets
	 * @param registry
	 * @param engine
	 * @param fileName
	 * @return
	 */
	public static void load(AssetManager assets, GameRegistry registry, String fileName) 
	{
		if(!assets.isLoaded(fileName)){
			EntityGroupLoaderParameters parameters = new EntityGroupLoaderParameters(registry);
			AssetDescriptor<EntityGroup> descriptor = new AssetDescriptor<EntityGroup>(fileName, EntityGroup.class, parameters);
			assets.load(descriptor);
		}
	}
	
	/**
	 * instanciate pre loaded entity group and add to engine.
	 * You can call {@link #load(AssetManager, GameRegistry, String)} method before and check asset manager status
	 * before calling this method.
	 * @param assets
	 * @param engine
	 * @param fileName
	 * @return
	 */
	public static Array<Entity> get(AssetManager assets, Engine engine, String fileName) 
	{
		EntityGroup group = assets.get(fileName, EntityGroup.class);
		Array<Entity> entities = new Array<Entity>();
		create(entities, assets, engine, group, null);
		return entities;
	}
	
	
	public static EntityGroup create(Array<Entity> entities, AssetManager assets, Engine engine, EntityGroup group, Entity parent)
	{
		final Transform2DComponent parentTransform = parent == null ? null : Transform2DComponent.components.get(parent);
		EntityGroup clones = new EntityGroup();
		for(Entity template : group.entities)
		{
			Entity entity = EntityHelper.clone(engine, template, group, clones, false);
			if(parentTransform != null){
				Transform2DComponent transform = Transform2DComponent.components.get(entity);
				if(transform != null){
					transform.position.add(parentTransform.position);
				}else{
					Movable movable = entity.getComponent(Movable.class);
					if(movable != null){
						movable.move(entity, new Vector3(parentTransform.position, 0)); // sprite plan
					}
				}
			}
			
			entities.add(entity);
			
			engine.addEntity(entity);
			
			// XXX legacy code only : initializable concept will be removed
			// initialize components
			for(Component componentClone : entity.getComponents()){
				if(componentClone instanceof Initializable){
					((Initializable) componentClone).initialize(engine, entity);
				}
			}
			
			ProxyComponent proxy = ProxyComponent.components.get(entity);
			if(proxy != null){
				EntityGroup proxyGroup = assets.get(proxy.ref, EntityGroup.class);
				proxy.template = proxyGroup;
				proxy.clones = create(entities, assets, engine, proxyGroup, entity);
			}
		}
		return clones;
	}
	
	public static void save(AssetManager assets, Engine engine, GameRegistry registry, FileHandle file, boolean pretty) 
	{
		Writer writer = file.writer(false);
		save(assets, engine, registry, writer, pretty);
		try {
			writer.close();
		} catch (IOException e) {
			throw new Error(e);
		};
	}
	public static void save(AssetManager assets, Engine engine, GameRegistry registry, Writer writer, boolean pretty) 
	{
		EntityGroup group = new EntityGroup();
		for(Entity entity : engine.getEntitiesFor(Family.all(Repository.class).get())){
			group.add(entity);
		}
		
		Json json = EntityGroupStorage.setup(assets, registry, group);
		if(pretty){
			try {
				writer.append(json.prettyPrint(group));
			} catch (IOException e) {
				throw new Error(e);
			}
		}else
			json.toJson(group, writer);
	}

	static Json setup(AssetManager assets, GameRegistry registry, EntityGroup group)
	{
		Json json = new Json();
	
		// set the root serializer
		EntityGroupSerializer entityGroupSerializer = new EntityGroupSerializer(assets, registry);
		json.setSerializer(EntityGroup.class, entityGroupSerializer);
		
		for(Entries<Class, Serializer> entries = registry.serializers.iterator() ; entries.hasNext() ; )
		{
			Entry<Class, Serializer> entry = entries.next();
			Class type = entry.key;
			Serializer serializer = entry.value;
			
			if(serializer instanceof AssetSerializer){
				AssetSerializer assetSerializer = (AssetSerializer)serializer;
				assetSerializer.assets = assets;
				assetSerializer.parent = entityGroupSerializer;
			}
			else if(serializer instanceof ContextualSerializer){
				ContextualSerializer contextualSerializer = (ContextualSerializer)serializer;
				contextualSerializer.context = group;
			}
			
			json.setSerializer(type, serializer);
		}
		
		return json;
	}
	
	
}