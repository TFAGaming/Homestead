# Introduction to the API

To access the Homestead API in your plugin, download the Homestead **.jar** file from [here](../Installation/Installation.md) and upload it as a third-party JAR to your project.

[Click here](https://stackoverflow.com/questions/4955635/how-can-i-add-local-jar-files-to-a-maven-project?page=1) or [here](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html) for the guide to installing the dependency.

!!! warning

    This section explains the API from version **4.0.2**.
    
    Try to make a condition in your plugin to verify that the installed Homestead version is 4.0.2 or higher. Example version parser and checker:

    ```java
    String version = Homestead.getVersion();

    int versionInteger = Integer.parseInt(version.replace(".", ""));

    if (versionInteger >= 402) {
        // Supported
    }
    ```

## Example Usage

First of all, you must install Homestead plugin on your project by downloading the **.jar** file, moving it on your resources folder, and then load it in your **pom.xml** file (for Maven).

The sample code below will give the instance of Homestead running on the server.

```java
Homestead instance = Homestead.getInstance();
```

This is used to check if Homestead is installed on the server or not; here is another sample code.

```java
try {
    Homestead instance = Homestead.getInstance();

    System.out.println("Homestead plugin was found!");
} catch (NoClassDefFoundError e) {
    System.err.println("Unable to find Homestead plugin!");
}
```

To get access to the API, use the sample code below:

```java
HomesteadAPI api = new API();
```

Now, your plugin's code should look like this:

```java
import org.bukkit.plugin.java.JavaPlugin;

import tfagaming.projects.minecraft.homestead.*;
import tfagaming.projects.minecraft.homestead.api.*;

public class Plugin extends JavaPlugin {
    public HomesteadAPI api;

	public void onEnable() {
		System.out.println("Plugin has been enabled.");
		
		try {
			Homestead instance = Homestead.getInstance();

			api = new API();

			System.out.println("Homestead plugin was found!");
		} catch (NoClassDefFoundError e) {
			System.err.println("Unable to find Homestead plugin!");
		}
	}

	public void onDisable() {
		System.out.println("Plugin has been disabled.");
	}
}
```
