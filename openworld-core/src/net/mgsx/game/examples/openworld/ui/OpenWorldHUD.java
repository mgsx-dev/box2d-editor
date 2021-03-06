package net.mgsx.game.examples.openworld.ui;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.game.core.Kit;
import net.mgsx.game.examples.openworld.components.ObjectMeshComponent;
import net.mgsx.game.examples.openworld.components.SpawnAnimalComponent;
import net.mgsx.game.examples.openworld.components.TreesComponent;
import net.mgsx.game.examples.openworld.model.Compound;
import net.mgsx.game.examples.openworld.model.GameAction;
import net.mgsx.game.examples.openworld.model.OpenWorldElement;
import net.mgsx.game.examples.openworld.model.OpenWorldGameEventListener;
import net.mgsx.game.examples.openworld.model.OpenWorldModel;
import net.mgsx.game.examples.openworld.systems.OpenWorldCameraSystem;
import net.mgsx.game.examples.openworld.systems.OpenWorldEnvSystem;
import net.mgsx.game.examples.openworld.systems.OpenWorldGameSystem;
import net.mgsx.game.examples.openworld.systems.UserObjectSystem;
import net.mgsx.game.examples.openworld.systems.UserObjectSystem.UserObject;
import net.mgsx.game.plugins.bullet.system.BulletWorldSystem;

public class OpenWorldHUD extends Table
{
	private static enum GameMenu{
		STATE, QUESTS, BUILDS, STATS
	}
	private GameAction action;
	private Label infoLabel;
	
	QuestStatusPopup questStatusPopup;
	
	OpenWorldGameSystem gameSystem;
	
	// TODO something better like : Backpack selection and World selection
	private OpenWorldElement backpackSelection;
	
	static class WorldSelection {
		public String elementName = null;
		public UserObject uo = null;
		public Vector3 position = new Vector3();
		public Vector3 normal = new Vector3();
		public Object object;
		public OpenWorldElement element;
	}
	
	private WorldSelection worldSelection;
	
	private Array<OpenWorldElement> craftSelection;
	
	
	Table backpack;
	ObjectMap<String, TextButton> backpackItemButtons = new ObjectMap<String, TextButton>();
	// TODO should be saved as well and reloaded !
	ObjectMap<String, Array<OpenWorldElement>> backpackContent = new ObjectMap<String, Array<OpenWorldElement>>();
	private Engine engine;
	
	public OpenWorldHUD(Skin skin, final Engine engine) {
		super(skin);
		this.engine = engine;
		gameSystem = engine.getSystem(OpenWorldGameSystem.class);
		
		gameSystem.addGameEventListener(new OpenWorldGameEventListener() {
			@Override
			public void onQuestUnlocked(String qid) {
				questStatusPopup.pushQuestStatus(qid, true);
			}
			@Override
			public void onQuestRevealed(String qid) {
				questStatusPopup.pushQuestStatus(qid, false);
			}
			@Override
			public void onSecretUnlocked(String itemId) {
				questStatusPopup.pushSecretStatus(itemId);
			}
			@Override
			public void onPlayerAction(GameAction action, String type) {
			}
		});
		
		build();
		
		Kit.inputs.addProcessor(new InputAdapter(){
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if(button != Input.Buttons.LEFT) return false;
				Camera camera = engine.getSystem(OpenWorldCameraSystem.class).getCamera();
				
				BulletWorldSystem bulletWorld = engine.getSystem(BulletWorldSystem.class);
				
				Ray ray = camera.getPickRay(screenX, screenY);
				ray.direction.scl(camera.far);
				Ray rayResult = new Ray();
				Object o = bulletWorld.rayCastObject(ray, rayResult);
				
				worldSelection = new WorldSelection();
				worldSelection.position.set(rayResult.origin);
				worldSelection.normal.set(rayResult.direction);
				worldSelection.object = o;
				if(o != null){
					// find which object as been picked :
					if(o instanceof Entity){
						Entity e = (Entity)o;
						ObjectMeshComponent omc = ObjectMeshComponent.components.get(e);
						if(omc != null){
							if(omc.userObject != null){
								worldSelection.uo = omc.userObject;
								worldSelection.elementName = omc.userObject.element.type;
								worldSelection.element = omc.userObject.element;
							}
						}
						SpawnAnimalComponent animal = SpawnAnimalComponent.components.get(e);
						if(animal != null){
							worldSelection.elementName = animal.element.type;
							worldSelection.element = animal.element;
						}
					}
					else if(o instanceof TreesComponent){
						worldSelection.elementName = "tree";
					}
				}
				return resolveInteraction();
			}
		});
	}
	
	public void resetState() {
		action = null;
		actionButtonGroup = null;
		backpackSelection = null;
		craftSelection = null;
		worldSelection = null;
		backpackItemButtons.clear();;
		backpackContent.clear();
		
		if(popin != null) popin.remove();
		popin = null;
		craftingView = null;
		
		clearChildren();
		clearActions();
		
		build();
	}

	public void addItemToBackpack(final OpenWorldElement item){
		TextButton bt = backpackItemButtons.get(item.type);
		if(bt == null){
			backpackItemButtons.put(item.type, bt = new TextButton("", getSkin()));
			backpack.add(bt);
			backpackContent.put(item.type, new Array<OpenWorldElement>());
			bt.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					backpackSelection = backpackContent.get(item.type).peek();
					resolveInteraction();
					//removeFromBackpack(item.type);
				}
			});
		}
		backpackContent.get(item.type).add(item);
		bt.setText(OpenWorldModel.name(item.type) + " : " + backpackContent.get(item.type).size);
	}
	
	private void dropFromBackpack(String type) {
		OpenWorldElement item = backpackContent.get(type).peek();
		
		removeFromBackpack(item);
		
		// append just in front of player ! TODO ray cast for ground !
		Camera camera = engine.getSystem(OpenWorldCameraSystem.class).getCamera();
		
		// materialize item
		OpenWorldModel.generateElement(item);
		
		// TODO project on bullet world
		
		// XXX restore dynamic property but it should be 2 dynamic fields : 
		// intrinsic and runtime (used by spawning ...)
		item.dynamic = true;
		item.position.set(camera.position).mulAdd(camera.direction, 2); // 2m
		item.rotation.idt();
		
		engine.getSystem(UserObjectSystem.class).appendObject(item);
			
	}
	
	private void removeFromBackpack(OpenWorldElement item) {
		backpackContent.get(item.type).removeValue(item, true);
		if(backpackContent.get(item.type).size > 0){
			backpackItemButtons.get(item.type).setText(OpenWorldModel.name(item.type) + " : " + backpackContent.get(item.type).size);
		}else{
			backpackContent.remove(item.type);
			backpackItemButtons.get(item.type).remove();
			backpackItemButtons.remove(item.type);
		}
		gameSystem.backpack.removeValue(item, true);
	}

	private ButtonGroup<TextButton> actionButtonGroup;
	
	/**
	 * contextual actions depends on surrounding items.
	 * eg. near a caban, you can sleep, neer a hover you can cook.
	 */
	private Table contextualActionsTable;
	
	private void build() {
		
		// TODO player status
		
		// TODO backpack
		backpack = new Table(getSkin());
		
		backpack.add("Backpack: ");
		
		
		
		// actions like Point'n'click
		actionButtonGroup = new ButtonGroup<TextButton>();
		actionButtonGroup.setMaxCheckCount(1);
		actionButtonGroup.setMinCheckCount(0);
		
		Table actionsTable = new Table(getSkin());
		
		actionsTable.add("Actions: ");
		
		actionsTable.add(createActionButton("Pick Up", GameAction.GRAB));
		actionsTable.add(createActionButton("Look At", GameAction.LOOK));
		actionsTable.add(createActionButton("Eat/Drink", GameAction.EAT));
		actionsTable.add(createActionButton("Use", GameAction.USE));
		actionsTable.add(createActionButton("Drop", GameAction.DROP));
		actionsTable.add(createActionButton("Build", GameAction.CRAFT));
		actionsTable.add(createActionButton("Sleep", GameAction.SLEEP));
		actionsTable.add(createActionButton("Destroy", GameAction.DESTROY));
		
		contextualActionsTable = new Table(getSkin());
		actionsTable.add(contextualActionsTable);
		
		infoLabel = new Label("", getSkin());
		
		Table menuTable = new Table(getSkin());
		menuTable.add("Menu: ");
		menuTable.add(createMenuButton("Load/Save", GameMenu.STATE));
		menuTable.add(createMenuButton("Quests", GameMenu.QUESTS));
		menuTable.add(createMenuButton("Secrets", GameMenu.BUILDS));
		menuTable.add(createMenuButton("Stats", GameMenu.STATS));
		
		defaults().padRight(30);
		
		questStatusPopup = new QuestStatusPopup(getSkin());
		
		add(questStatusPopup).colspan(2).expand().center().top().row();
		
		add(infoLabel).colspan(2).expandX().center().row();
		add(new StatusView(getSkin(), engine)).colspan(2).expandX().center().row();
		add(actionsTable).expandX().right();
		add(menuTable).expandX().left();
		row();
		
		add(backpack).colspan(2).expandX().center();
		
		
		
		for(OpenWorldElement e : gameSystem.backpack){
			addItemToBackpack(e);
		}
	}

	private TextButton createActionButton(String label, final GameAction newAction) {
		final TextButton bt = new TextButton(label, getSkin(), "toggle");
		actionButtonGroup.add(bt);
		bt.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				action = bt.isChecked() ? newAction : null;
				resolveInteraction();
			}
		});
		return bt;
	}

	private TextButton createMenuButton(String label, final GameMenu menu) {
		final TextButton bt = new TextButton(label, getSkin());
		bt.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				openMenu(menu);
			}
		});
		return bt;
	}

	private void openMenu(GameMenu menu) 
	{
		Actor dialog;
		switch(menu){
		case QUESTS:
			dialog = new QuestsView(getSkin(), engine);
			break;
		case BUILDS:
			dialog = new SecretsView(getSkin(), engine);
			break;
		case STATS:
			dialog = new StatisticsView(getSkin(), engine);
			break;
		default:
		case STATE:
			dialog = new SavedGameView(getSkin(), engine);
			break;
		}
		
		openPopin(dialog);
	}

	protected boolean resolveInteraction() {
		
		// TODO put texts in model with i18n
		
		OpenWorldEnvSystem envSystem = engine.getSystem(OpenWorldEnvSystem.class);
		UserObjectSystem objectSystem = engine.getSystem(UserObjectSystem.class);
		
		String actionPerformed = null;
		boolean actionCanceled = false;
		
		UserObjectSystem userObject = engine.getSystem(UserObjectSystem.class);
		
		// eat, grab, look
		if(action == GameAction.GRAB){
			// try to grab element
			if(worldSelection != null){
				// remove it TODO but keep its properties somewhere in order to regenerates !
				if(worldSelection.uo != null){
					
					Integer space = OpenWorldModel.space(worldSelection.uo.element.type);
					if(space != null){
						int spaceAvailable = gameSystem.getAvailableSpaceInBackpack();
						if(space <= spaceAvailable){
							userObject.removeElement(worldSelection.uo);
							// and add it to the player backpack ! if meet conditions (size, ...etc).
							// animate model : lerp to player and inc GUI
							addItemToBackpack(worldSelection.uo.element);
							gameSystem.backpack.add(worldSelection.uo.element);
							// TODO update backpack fill rate
							infoLabel.setText(OpenWorldModel.name(worldSelection.uo.element.type) + " added to your backpack.");
							actionPerformed = worldSelection.uo.element.type;
						}else{
							infoLabel.setText("You don't have enough space in your backpack.");
						}
					}else{
						infoLabel.setText("You can't pick up this... try something else.");
					}
				}else{
					infoLabel.setText("You can't pick up this... try something else.");
				}
			}else{
				infoLabel.setText("Pick anything you want...");
			}
		}
		else if(action == GameAction.USE){
			if(backpackSelection != null && worldSelection != null && worldSelection.elementName != null){
				Array<OpenWorldElement> created = new Array<OpenWorldElement>();
				if(OpenWorldModel.useTool(created, backpackSelection.type, worldSelection.elementName)){
					// TODO multi element support should be better
					for(OpenWorldElement element : created){
						element.position.set(worldSelection.position);
						userObject.appendObject(element);
						infoLabel.setText(OpenWorldModel.name(element.type) + " just spawned!");
					}
					actionPerformed = backpackSelection.type;
				}
				int damage = OpenWorldModel.useWeapon(backpackSelection.type, worldSelection.elementName);
				if(damage > 0 && worldSelection.element != null){
					worldSelection.element.life -= damage;
					infoLabel.setText(OpenWorldModel.name(worldSelection.elementName) + " was hurt!");
					actionPerformed = backpackSelection.type;
				}
				
				if(actionPerformed == null){
					infoLabel.setText("Nothing happens...");
					actionCanceled = true;
				}
			}
			else if(backpackSelection == null && (worldSelection == null || worldSelection.elementName == null)){
				infoLabel.setText("Use something from your backpack...");
			}
			// use in the world ?
			else if(backpackSelection != null){
				infoLabel.setText("Use your " + OpenWorldModel.name(backpackSelection.type) + " on something...");
			}
		}
		else if(action == GameAction.DROP){
			// TODO and if raycasted toward the world ...
			if(backpackSelection != null){
				dropFromBackpack(backpackSelection.type);
				actionPerformed = backpackSelection.type;
				infoLabel.setText(OpenWorldModel.name(backpackSelection.type) + " was dropped from your backpack");
			}else{
				infoLabel.setText("Drop something from your backpack");
			}
		}
		else if(action == GameAction.EAT){
			if(worldSelection != null && worldSelection.elementName != null){
				// TODO allow eat something not too far
				infoLabel.setText("You have to pick it up first.");
				actionCanceled = true;
			}
			else if(backpackSelection != null){
				Integer energyGiven = OpenWorldModel.energy(backpackSelection.type);
				if(energyGiven != null){
					if(energyGiven > 0){
						gameSystem.player.energy = Math.min(gameSystem.player.energy + energyGiven, gameSystem.player.energyMax);
						infoLabel.setText(OpenWorldModel.name(backpackSelection.type) + " gives you some energie!");
					}else if(energyGiven < 0){
						gameSystem.player.energy = Math.max(gameSystem.player.energy + energyGiven, 0);
						infoLabel.setText(OpenWorldModel.name(backpackSelection.type) + " poisonned you, you lost energie!");
					}else{
						infoLabel.setText(OpenWorldModel.name(backpackSelection.type) + " gives you no energie at all...");
					}
					removeFromBackpack(backpackSelection);
					actionPerformed = backpackSelection.type;
				}else{
					infoLabel.setText(OpenWorldModel.name(backpackSelection.type) + " cannot be eat or drink...");
				}
			}else{
				infoLabel.setText("Get something to eat...");
			}
		}
		else if(action == GameAction.CRAFT){
			if(craftSelection != null && worldSelection != null){
				// fusion
				Compound compound = new Compound();
				for(OpenWorldElement item : craftSelection){
					compound.add(item.type);
					removeFromBackpack(item);
				}
				String newType = OpenWorldModel.findFusion(compound);
				
				OpenWorldElement e;
				if(newType != null){
					// create the new object !
					e = OpenWorldModel.generateNewElement(newType);
					infoLabel.setText("Congrats! you get " + OpenWorldModel.name(e.type));
				}
				else
				{
					// create some basic objects (fail !)
					e = OpenWorldModel.generateNewGarbageElement(compound);
					infoLabel.setText("Ooops ... you get nothing valuable, check your note book.");
				}
				
				e.position.set(worldSelection.position);
				e.rotation.idt(); // TODO normal ?
				
				engine.getSystem(UserObjectSystem.class).appendObject(e);
				
				actionPerformed = e.type;
			}
			else if(craftSelection != null && worldSelection == null){
				infoLabel.setText("Choose where to build your ... thing.");
			}
			else if(craftingView == null){
				openCrafting();
			}
			else{
				actionCanceled = true;
				infoLabel.setText("");
			}
		}
		else if(action == GameAction.SLEEP){
			
			// player have to select a place to sleep. For now only sleepable area is allowed
			if(worldSelection != null && worldSelection.elementName != null){
				
				Integer restValue = OpenWorldModel.sleepableEnergy(worldSelection.elementName);
				if(restValue != null){
					gameSystem.player.energy = Math.min(gameSystem.player.energy + restValue, gameSystem.player.energyMax);

					// TODO should have a cinematic, time elpased, ...etc.
					// we simply offet time a little (8 hours)
					envSystem.timeOffset += 8;
					
					infoLabel.setText("This nap gave you some energy, you're ready to go now.");
					actionPerformed = worldSelection.elementName;
				}else{
					infoLabel.setText("This place is not safe, try another place...");
				}
				
			}else{
				infoLabel.setText("Touch a safe place where you could sleep...");
			}
			
		}
		else if(action == GameAction.DESTROY){
			if(worldSelection != null && worldSelection.elementName != null && worldSelection.uo != null){
				
				Array<OpenWorldElement> result = OpenWorldModel.destroy(worldSelection.elementName);
				if(result != null){
					
					// first remove element
					objectSystem.removeElement(worldSelection.uo);
					
					// then create some new object here
					for(OpenWorldElement item : result){
						
						item.position.set(worldSelection.position);
						
						objectSystem.appendObject(item);
					}
					
					actionPerformed = worldSelection.elementName;
				}else{
					infoLabel.setText("This can't be destroyed, try another thing...");
				}
				
			}else{
				infoLabel.setText("Touch something to destroy it...");
			}
		}
		// default look
		else
		{
			action = GameAction.LOOK;
			if(worldSelection != null && worldSelection.elementName != null){
				String description = OpenWorldModel.description(worldSelection.elementName);
				infoLabel.setText(description);
				actionPerformed = worldSelection.elementName;
			}
			else if(backpackSelection != null){
				String description = OpenWorldModel.description(backpackSelection.type);
				infoLabel.setText(description);
				actionPerformed = backpackSelection.type;
			}else{
				infoLabel.setText("Touch something to examin it...");
			}
		}
		
		if(actionPerformed != null){
			gameSystem.actionReport(action, actionPerformed);
		}
		
		// TODO not always clear if action not resolved
		// clear all but action
		if(actionPerformed != null || actionCanceled){
			craftSelection = null;
			if(action != GameAction.USE)
				backpackSelection = null; // TODO depends on context : use will reuse
			if(action == GameAction.CRAFT){
				actionButtonGroup.uncheckAll();
				action = null;
			}
		}
		// always reset world selection because this is the last action in the workflow
		worldSelection = null;
		
		// XXX
		if(action != GameAction.CRAFT){
			if(popin != null) popin.remove();
			popin = null;
			craftingView = null;
		}
		
		
		
		
		return actionPerformed != null;
	}

	private Table popin;
	private CraftingView craftingView;
	
	private void openCrafting() {
		craftingView = new CraftingView(getSkin(), engine, new CraftingView.Callback() {
			@Override
			public void onComplete(Array<OpenWorldElement> selection) {
				craftSelection = selection;
				resolveInteraction();
				craftingView = null;
				closePopin();
			}
		});
		
		openPopin(craftingView);
	}

	private void openPopin(Actor dialog)
	{
		if(popin != null) popin.remove();
		popin = new Table();
		popin.setFillParent(true);
		popin.setTouchable(Touchable.enabled);
		popin.add(dialog).expand().center();
		getStage().addActor(popin);
		popin.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(event.getTarget() == popin){
					closePopin();
				}
			}
		});
	}
	
	private void closePopin(){
		if(popin != null) popin.remove();
		popin = null;
	}
	
}
