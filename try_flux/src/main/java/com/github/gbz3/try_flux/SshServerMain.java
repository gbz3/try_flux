package com.github.gbz3.try_flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.common.channel.RequestHandler;
import org.apache.sshd.common.session.ConnectionService;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshServerMain {
	private static Logger logger = LoggerFactory.getLogger( SshServerMain.class );

	public static void main(String[] args) throws IOException, InterruptedException {
		final SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort( 22 );
		sshd.setKeyPairProvider( new SimpleGeneratorHostKeyProvider() );
		sshd.setPublickeyAuthenticator( AcceptAllPublickeyAuthenticator.INSTANCE );
//		sshd.setShellFactory( new ProcessShellFactory( new String[] { "cmd.exe" } ) );
//		sshd.setShellFactory( new ProcessShellFactory( new String[] { "bash.exe", "-i", "-l" } ) );
//		sshd.setShellFactory( new ProcessShellFactory( new String[] { "/bin/sh", "-i", "-l" } ) );
		final InteractiveProcessShellFactory ipsf = InteractiveProcessShellFactory.INSTANCE;
		//ipsf.setCommand( Arrays.asList( "dir.exe" ) );
		sshd.setShellFactory( ipsf );
		//sshd.setCommandFactory( new UnknownCommandFactory() );
//		sshd.setCommandFactory(new ScpCommandFactory.Builder()
//	            .withDelegate(ProcessShellCommandFactory.INSTANCE)
//	            .build());
		//sshd.setSubsystemFactories(null);
		
		final List<RequestHandler<ConnectionService>> oldGlobals = sshd.getGlobalRequestHandlers();
		final List<RequestHandler<ConnectionService>> newGlobals = new ArrayList<>();
		if( GenericUtils.size( oldGlobals ) > 0) {
			newGlobals.addAll( oldGlobals );
		}
		newGlobals.add( new RequestHandler<ConnectionService>() {
			@Override public Result process( ConnectionService t, String request, boolean wantReply, Buffer buffer )	throws Exception {
				logger.info( "t={} request={} wantReply={} buffer={}", t, request, wantReply, buffer );
				return null;
			}
		});
		logger.info( "requestHandler={}", newGlobals );
		
		sshd.start();
		
		Thread.sleep( 60L * 1000L );
	}
	
	

}
