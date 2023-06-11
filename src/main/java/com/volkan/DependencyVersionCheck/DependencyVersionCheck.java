package com.volkan.DependencyVersionCheck;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Mojo(name = "dependency-check", defaultPhase = LifecyclePhase.COMPILE)
public class DependencyVersionCheck extends AbstractMojo {

	public void execute() throws MojoExecutionException {
		try {
			Process process = Runtime.getRuntime().exec("mvn versions:display-property-updates versions:display-parent-updates -DgenerateBackupPoms=false");
			int exitCode = process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder dependencyUpdates = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.contains("->")) {
					String[] parts = line.split("->");
					String currentVersion = parts[0].trim();
					String availableVersion = parts[1].trim();
					if (!currentVersion.equals(availableVersion)) {
						dependencyUpdates.append(line).append("\n");
					}
				}
			}

			if (dependencyUpdates.length() > 0) {
				getLog().warn("The following dependencies or microservices have updates available:\n" + dependencyUpdates);
				getLog().warn("Please update the dependencies and microservice versions before committing...");
			} else {
				getLog().info("No dependency updates are available.");
			}

			if (exitCode != 0) {
				throw new MojoExecutionException("An error occurred while checking for dependency updates.");
			}
		} catch (IOException | InterruptedException e) {
			throw new MojoExecutionException("An error occurred while executing Maven command.", e);
		}
	}
}
