package net.mgsx.core.plugins;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.mgsx.core.Editor;

/**
 * Base for a global (all entities) editor view
 */
public interface GlobalEditorPlugin {

	public Actor createEditor(Editor editor, Skin skin);
}