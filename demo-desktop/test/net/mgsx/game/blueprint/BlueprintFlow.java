package net.mgsx.game.blueprint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.mgsx.game.blueprint.annotations.Inlet;
import net.mgsx.game.blueprint.annotations.Node;
import net.mgsx.game.blueprint.annotations.Outlet;
import net.mgsx.game.blueprint.model.Graph;
import net.mgsx.game.blueprint.model.Graph.CopyStrategy;
import net.mgsx.game.blueprint.ui.GraphView;
import net.mgsx.game.blueprint.ui.GraphView.GraphViewConfig;
import net.mgsx.game.core.GameApplication;
import net.mgsx.game.core.annotations.Editable;
import net.mgsx.game.core.helpers.AssetHelper;
import net.mgsx.game.core.screen.StageScreen;

public class BlueprintFlow extends GameApplication {

	public static void main(String[] args) 
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new BlueprintFlow(), config);
	}
	private Graph graph;

	@Override
	public void create() 
	{
		super.create();
		
		Skin skin = AssetHelper.loadAssetNow(assets, "sgxui/sgx-ui.json", Skin.class);
		StageScreen screen;
		setScreen(screen = new StageScreen(skin));
		
		graph = new Graph(CopyStrategy.FROM_DST);
		
		graph.addNode(new Initial(), 10, 50);
		graph.addNode(new Shoot(), 300, 200);
		
		
		GraphViewConfig config = new GraphViewConfig();
		config.setTypeColor(Color.ORANGE, FlowEvent.class);
		config.setTypeColor(Color.CYAN, float.class);
		
		GraphView view = new GraphView(graph, skin, config);
		view.addNodeType(Initial.class, Shoot.class, Flee.class, GameScreen.class, ColorNode.class, ParamNode.class);
		
		screen.getStage().addActor(view);
		view.setFillParent(true);
	}
	
	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}
	
	public static class FlowEvent {
		
		
		
	}
	
	@Node("Param")
	public static class ParamNode {
		
		@Editable
		@Outlet
		public float value;
		
		@Outlet
		public void debug(){
			System.out.println(value);
		}
	}
	
	@Node("Init")
	public static class Initial {
		
		@Outlet
		public FlowEvent exit;
		
		@Outlet
		public void debug(){
			System.out.println(exit);
		}
	}
	
	@Inlet("enter")
	@Node("ColorAction")
	public static class ColorNode extends FlowEvent {
		
		@Outlet
		public FlowEvent exit;
		
		@Editable
		@Inlet
		public ColorAction action;
		
		public ColorNode() {
			action = new ColorAction();
			action.setColor(new Color()); // avoid NPE
		}
		
	}
	
	@Inlet
	@Node("shoot")
	public static class Shoot extends FlowEvent {
		
		@Outlet
		public FlowEvent onDead, onComplete;
		
		
	}
	
	
	@Inlet
	@Node("flee")
	public static class Flee extends FlowEvent {
		
		@Editable
		@Inlet
		public float distance = 1;
		
		@Outlet
		public FlowEvent onComplete;
		
	}
	
	@Inlet
	@Node("GameScreen")
	public static class GameScreen extends FlowEvent {
		
		@Outlet
		public FlowEvent onGameOver, onAbort, onSuccess;
		
	}

	
}
