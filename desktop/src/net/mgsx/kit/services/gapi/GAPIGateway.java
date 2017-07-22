package net.mgsx.kit.services.gapi;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.games.Games;
import com.google.api.services.games.GamesScopes;

class GAPIGateway {
	
	private static String applicationName;
	private static FileHandle clientSecretsFile;
	
	/**
	 * Global instance of the {@link DataStoreFactory}. The best practice is to
	 * make it a single globally shared instance across your application.
	 */
	private static FileDataStoreFactory dataStoreFactory;

	/** Global instance of the HTTP transport. */
	private static HttpTransport httpTransport;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	public static Games games;

	public static Drive drive;

	/**
	 * Be sure to specify the name of your application. If the application name
	 * is {@code null} or blank, the application will log a warning. Suggested
	 * format is "MyCompany-ProductName/1.0".
	 */
	public static void init(String applicationName, FileHandle clientSecretsFile) throws GeneralSecurityException, IOException  
	{
		GAPIGateway.applicationName = applicationName;
		GAPIGateway.clientSecretsFile = clientSecretsFile;
		
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		
		File dataStoresDirectory = new File(System.getProperty("user.home"), ".store");
		File dataStoreDirectory = new File(dataStoresDirectory, applicationName.toLowerCase());
		dataStoreFactory = new FileDataStoreFactory(dataStoreDirectory);
	}
	
	/** Authorizes the installed application to access user's protected data. */
	public static void authorize(String userID) throws Exception {
		// load client secrets
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(clientSecretsFile.read()));
		// set up authorization code flow
		Collection<String> scopes = new ArrayList<String>();
		scopes.add(GamesScopes.GAMES);
		scopes.add(DriveScopes.DRIVE_APPDATA);

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
				clientSecrets, scopes).setDataStoreFactory(dataStoreFactory).build();
		// authorize
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()) {
			// Override open browser not working well on Linux and maybe other
			// OS.
			protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws java.io.IOException {
				Gdx.net.openURI(authorizationUrl.build());
			}
		}.authorize(userID);
		
		games = new Games.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(applicationName).build();
		drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(applicationName).build();

	}

}
