package net.mgsx.kit.demo;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import android.os.Bundle;
import de.golfgl.gdxgamesvcs.GpgsClient;
import net.mgsx.game.core.EditorApplication;
import net.mgsx.game.core.EditorConfiguration;
import net.mgsx.game.core.meta.ClassRegistry;
import net.mgsx.game.core.meta.StaticClassRegistry;
import net.mgsx.game.examples.openworld.OpenWorldEditorPlugin;
import net.mgsx.game.examples.openworld.model.OpenWorldRuntimeSettings;
import net.mgsx.game.examples.openworld.systems.OpenWorldCameraSystem;
import net.mgsx.kit.KitClass;
import net.mgsx.pd.Pd;
import net.mgsx.pd.PdConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		
		GpgsClient gsc = new GpgsClient();
		OpenWorldRuntimeSettings.gsc = gsc;
		gsc.initialize(this, false); // XXX disable drive API
		
		ClassRegistry.instance = new StaticClassRegistry(KitClass.class);
		
		EditorConfiguration editConfig = new EditorConfiguration();
		editConfig.plugins.add(new OpenWorldEditorPlugin());
		editConfig.path = "openworld/openworld-scene-minimal.json";
		
		
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new EditorApplication(editConfig){
			@Override
			public void create() {
				Pd.audio.create(new PdConfiguration());
				OpenWorldCameraSystem.cameraMatrixProvider = new CameraMatrixProviderAndroid(AndroidLauncher.this);
				super.create();
			}
		}, config);
	}
}
