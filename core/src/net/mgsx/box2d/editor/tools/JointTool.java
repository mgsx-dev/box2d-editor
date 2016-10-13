package net.mgsx.box2d.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;

import net.mgsx.box2d.editor.commands.Box2DCommands;
import net.mgsx.box2d.editor.model.BodyItem;
import net.mgsx.box2d.editor.model.JointItem;
import net.mgsx.box2d.editor.model.WorldItem;
import net.mgsx.fwk.editor.Command;
import net.mgsx.fwk.editor.tools.MultiClickTool;

abstract public class JointTool<T extends JointDef> extends MultiClickTool 
{
	protected WorldItem worldItem;
	
	public JointTool(String name, Camera camera, WorldItem worldItem, int maxPoints) 
	{
		super("Create " + name, camera, maxPoints);
		this.worldItem = worldItem;
	}
	
	protected abstract T createJoint(BodyItem bodyA, BodyItem bodyB);
	
	@Override
	protected void complete() 
	{
		if(worldItem.selection.bodies.size < 2){
			return;
		}
		BodyItem bodyA = worldItem.selection.bodies.get(worldItem.selection.bodies.size-2);
		BodyItem bodyB = worldItem.selection.bodies.get(worldItem.selection.bodies.size-1);
		
		final T def = createJoint(bodyA, bodyB);
		
		def.bodyA = bodyA.body;
		def.bodyB = bodyB.body;
		
		worldItem.performCommand(Box2DCommands.addJoint(worldItem, name, def));
	}
}
