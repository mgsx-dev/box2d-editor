package net.mgsx.plugins.box2d.tools;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import net.mgsx.core.Editor;
import net.mgsx.core.tools.RectangleTool;
import net.mgsx.plugins.box2d.commands.Box2DCommands;
import net.mgsx.plugins.box2d.model.BodyItem;
import net.mgsx.plugins.box2d.model.WorldItem;

public class CreateRectangleTool extends RectangleTool {

	private WorldItem worldItem;
	
	public CreateRectangleTool(Editor editor, WorldItem worldItem) {
		super("Rectangle", editor);
		this.worldItem = worldItem;
	}

	@Override
	protected void create(Vector2 startPoint, Vector2 endPoint) 
	{
		float x = (startPoint.x + endPoint.x) / 2;
		float y = (startPoint.y + endPoint.y) / 2;
		float w = Math.abs(startPoint.x - endPoint.x);
		float h = Math.abs(startPoint.y - endPoint.y);

		BodyItem bodyItem = worldItem.currentBody("Rectangle", x, y);
		
		PolygonShape pshape = new PolygonShape();
		pshape.setAsBox(w/2, h/2, new Vector2(x, y).sub(bodyItem.body.getPosition()), 0); 
		
		FixtureDef fix = worldItem.settings.fixture();
		fix.shape = pshape;
		
		worldItem.performCommand(Box2DCommands.addShape(worldItem, bodyItem, fix));
	}
	
	
}
