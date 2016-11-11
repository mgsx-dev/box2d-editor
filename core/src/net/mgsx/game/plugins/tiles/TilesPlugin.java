package net.mgsx.game.plugins.tiles;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.game.core.Editor;
import net.mgsx.game.core.helpers.NativeService;
import net.mgsx.game.core.helpers.NativeService.DialogCallback;
import net.mgsx.game.core.plugins.EditorPlugin;
import net.mgsx.game.core.storage.Storage;
import net.mgsx.game.core.tools.Tool;
import net.mgsx.game.plugins.boundary.components.BoundaryComponent;

public class TilesPlugin extends EditorPlugin
{

	@Override
	public void initialize(Editor editor) {
		
		editor.addTool(new Tool("Import Tiles", editor){
			@Override
			protected void activate() {
				NativeService.instance.openLoadDialog(new DialogCallback() {
					@Override
					public void selected(FileHandle file) {
						TiledMap map = new TmxMapLoader(new AbsoluteFileHandleResolver()).load(file.path());
						importMap(map, file, editor);
						end();
					}
					@Override
					public void cancel() {
					}
				});
			}
		});
		
	}
	
	private void importMap(TiledMap map, FileHandle mapFile, Editor editor){
		float res = 2;
		for(MapLayer layer : map.getLayers()){
			if(layer instanceof TiledMapTileLayer){
				TiledMapTileLayer tileLayer = ((TiledMapTileLayer) layer);
				for(int y=0 ; y<tileLayer.getHeight() ; y++){
					for(int x=0 ; x<tileLayer.getWidth() ; x++){
						Cell cell = tileLayer.getCell(x, y);
						if(cell != null){
							TiledMapTile tile = cell.getTile();
							String name = String.valueOf(tile.getProperties().get("name"));
							FileHandle file = mapFile.parent().parent().child("library").child(name + ".json");
							for(Entity entity : Storage.load(editor.entityEngine, file, editor.assets, editor.serializers))
							{
								editor.getMovable(entity).move(entity, new Vector3(x * res, y * res, 0));
								BoundaryComponent b = entity.getComponent(BoundaryComponent.class);
								if(b != null){
									b.box.mul(new Matrix4().setTranslation(new Vector3(x * res, y * res, 0)));
								}
							}
						}
					}

				}
			}
		}
	}
}