package net.mgsx.game.examples.platformer.core;

import com.badlogic.ashley.core.Entity;

import net.mgsx.game.core.GameEngine;
import net.mgsx.game.core.helpers.EmptySerializer;
import net.mgsx.game.core.helpers.EntityHelper;
import net.mgsx.game.core.plugins.Plugin;
import net.mgsx.game.core.storage.Storage;

/**
 * 
 * Game core plugin
 * 
 * @author mgsx
 *
 */
public class PlatformerPlugin implements Plugin
{
	public GameEngine engine;
	
	@Override
	public void initialize(GameEngine engine) 
	{
		this.engine = engine;
		
		Storage.register(PlayerComponent.class, "example.platformer.player");
		engine.addSerializer(PlayerComponent.class, new EmptySerializer<PlayerComponent>());
		
		Storage.register(BonusComponent.class, "example.platformer.bonus");
		engine.addSerializer(BonusComponent.class, new EmptySerializer<BonusComponent>());
		
		// add a processor for player
		// TODO could be automated with a generic component and abstract behavior attached to it ?
		engine.entityEngine.addSystem(new EntityHelper.SingleComponentIteratingSystem<PlayerComponent>(PlayerComponent.class) {
			@Override
			protected void processEntity(Entity entity, PlayerComponent component, float deltaTime) {
				component.update(deltaTime);
			}
		});
		
	}

}