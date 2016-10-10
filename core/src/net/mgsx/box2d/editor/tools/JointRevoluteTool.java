package net.mgsx.box2d.editor.tools;

import net.mgsx.box2d.editor.BodyItem;
import net.mgsx.box2d.editor.WorldItem;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;

public class JointRevoluteTool extends JointTool<RevoluteJointDef> {
	public JointRevoluteTool(Camera camera, WorldItem worldItem) 
	{
		super("Revolute", camera, worldItem, 2);
	}
	@Override
	protected RevoluteJointDef createJoint(BodyItem bodyA, BodyItem bodyB) {
		
		RevoluteJointDef def = new RevoluteJointDef();
		def.bodyA = bodyA.body;
		def.bodyB = bodyB.body;
		def.collideConnected = true;
		def.localAnchorA.set(new Vector2(dots.get(0)).sub(bodyA.body.getPosition()));
		def.localAnchorB.set(new Vector2(dots.get(1)).sub(bodyB.body.getPosition()));
		
		return def;
	}
}
