package net.mgsx.game.plugins.btree;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ai.btree.AnnotationBasedTaskCloner;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.utils.Pool.Poolable;

import net.mgsx.game.core.annotations.EditableComponent;
import net.mgsx.game.core.annotations.Storable;
import net.mgsx.game.core.components.Duplicable;

@Storable("btree")
@EditableComponent(autoTool=false)
public class BTreeModel implements Component, Duplicable, Poolable
{
	
	public final static ComponentMapper<BTreeModel> components = ComponentMapper.getFor(BTreeModel.class);
	
	public String libraryName;
	public BehaviorTree<EntityBlackboard> tree;
	
	public boolean enabled = false; // XXX should be enabled by default ? or use a command in normal game mode ?
	
	public boolean remove = false;
	
	@Override
	public Component duplicate(Engine engine) {
		BTreeModel clone = engine.createComponent(BTreeModel.class);
		clone.tree = engine.getSystem(BTreeSystem.class).createBehaviorTree(libraryName); 
		clone.libraryName = libraryName;
		clone.remove = remove;
		clone.enabled = enabled;
		return clone;
	}
	
	@Override
	public void reset() {
		AnnotationBasedTaskCloner.free(tree);
		tree = null;
		enabled = false;
	}

}
