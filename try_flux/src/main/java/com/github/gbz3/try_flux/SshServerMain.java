package com.github.gbz3.try_flux;

import java.io.IOException;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;
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
		sshd.setShellFactory( new ProcessShellFactory( new String[] { "bash.exe", "-i", "-l" } ) );
//		sshd.setShellFactory( new ProcessShellFactory( new String[] { "/bin/sh", "-i", "-l" } ) );
		
		sshd.start();
		
		Thread.sleep( 60L * 1000L );
	}

}
