package net.mgsx.game.core.helpers;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public interface ComponentFactory 
{
	public Component create(Entity entity);
}
