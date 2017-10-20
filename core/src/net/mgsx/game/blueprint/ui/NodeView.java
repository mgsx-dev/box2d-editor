package net.mgsx.game.blueprint.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import net.mgsx.game.blueprint.Graph;
import net.mgsx.game.blueprint.GraphNode;
import net.mgsx.game.blueprint.Portlet;
import net.mgsx.game.blueprint.annotations.Node;
import net.mgsx.game.core.annotations.Editable;
import net.mgsx.game.core.ui.EntityEditor;

public class NodeView extends Table
{
	public Array<Portlet> inlets = new Array<Portlet>();
	public Array<Portlet> outlets = new Array<Portlet>();

	private Table inletList;
	private Table outletList;
	private HorizontalGroup header;
	public GraphNode node;
	private Graph graph;

	
	public NodeView(Graph graph, GraphNode node, Skin skin) {
		super(skin);
		this.graph = graph;
		this.node = node;
		
		Node meta = node.object.getClass().getAnnotation(Node.class);
		String name = meta != null ? meta.value() : "";
		name = name.isEmpty() ? node.object.getClass().getName() : name;
		
		setBackground("default-round");
		
//		debug();
		
		header = new HorizontalGroup();
		add(header).colspan(2).expandX().center();
		header.addActor(new Label(getTypeName(node.object.getClass()), skin));
		row();
		inletList = new Table(skin);
		outletList = new Table(skin);
		add(inletList).expand().fill();
		add(outletList).expand().fill();
		row();
		
		setTouchable(Touchable.enabled);
		
		for(Portlet inlet : node.inlets){
			addInlet(inlet);
		}
		
		for(Portlet outlet : node.outlets){
			addOutlet(outlet);
		}
		
	}
	
	public void addInlet(final Portlet portlet){
		
		TextButton bt = new TextButton("", getSkin());
		bt.setUserObject(portlet);
		portlet.actor = bt;
		inlets.add(portlet);
		
		Table table = inletList;
		table.add(bt);
		table.add(portlet.getName()).expandX().fill();
		
		if(portlet.accessor.config(Editable.class) != null)
		{
			Table tmpTable = new Table(getSkin());
			EntityEditor.createControl(tmpTable, portlet.node.object, portlet.accessor);
			table.add(tmpTable.getCells().first().getActor());
		}
		
		inletList.row();
		
		bt.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				graph.removeLinksTo(portlet);
			}
		});

		
	}
	public void addOutlet(final Portlet portlet){
		
		TextButton bt = new TextButton("", getSkin());
		bt.setUserObject(portlet);
		portlet.actor = bt;
		outlets.add(portlet);
		
		Table table = outletList;
		table.add(portlet.getName()).expandX().fill();
		
		if(portlet.accessor.config(Editable.class) != null)
		{
			Table tmpTable = new Table(getSkin());
			EntityEditor.createControl(tmpTable, portlet.node.object, portlet.accessor);
			table.add(tmpTable.getCells().first().getActor());
		}
		table.add(bt);
		
		outletList.row();
		
		bt.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				graph.removeLinksFrom(portlet);
			}
		});
		
	}

	public void addPortlet(final Portlet portlet){
		
		
		
		
	}
	
	public static String getTypeName(Class<?> type) {
		Node meta = type.getAnnotation(Node.class);
		String name = meta != null ? meta.value() : "";
		name = name.isEmpty() ? type.getName() : name;
		return name;
	}
	
}