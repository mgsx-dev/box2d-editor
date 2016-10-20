package net.mgsx.game.plugins.box2dold.tools;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.PulleyJointDef;

import net.mgsx.game.core.Editor;
import net.mgsx.game.plugins.box2d.model.Box2DBodyModel;
import net.mgsx.game.plugins.box2dold.model.WorldItem;

public class JointPulleyTool extends JointTool<PulleyJointDef> {
	public JointPulleyTool(Editor editor, WorldItem worldItem) 
	{
		super("Pulley", editor, worldItem, 4);
	}
	@Override
	protected PulleyJointDef createJoint(Box2DBodyModel bodyA, Box2DBodyModel bodyB) {
		
		PulleyJointDef def = new PulleyJointDef();
		def.bodyA = bodyA.body;
		def.bodyB = bodyB.body;
		def.groundAnchorA.set(dots.get(1));
		def.groundAnchorB.set(dots.get(2));
		def.localAnchorA.set(new Vector2(dots.get(0)).sub(bodyA.body.getPosition()));
		def.localAnchorB.set(new Vector2(dots.get(3)).sub(bodyB.body.getPosition()));
		def.lengthA = dots.get(1).dst(dots.get(0));
		def.lengthB = dots.get(2).dst(dots.get(3));
		
		return def;
	}
}