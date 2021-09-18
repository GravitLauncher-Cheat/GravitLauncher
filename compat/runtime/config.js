// ====== LAUNCHER CONFIG ====== //
var config = {
    dir: "Dreamfinity", // Launcher directory
    title: "GravitLauncher Cheat", // Window title
    icons: [ "favicon.png" ], // Window icon paths

    // Auth config
    linkText: "dima_dencep", // Text for link under "Auth" button
    linkURL: new java.net.URL("http://dima-dencep.ml"), // URL for link under "Auth" button
	
    // Menu config
    discord_url: new java.net.URL("https://discord.gg/b9QG4ygY75"),

    // Settings defaults
    settingsMagic: 0xC0DE5, // Ancient magic, don't touch
    autoEnterDefault: false, // Should autoEnter be enabled by default?
    fullScreenDefault: false, // Should fullScreen be enabled by default?
    ramDefault: 4096, // Default RAM amount (0 for auto)
};

// ====== DON'T TOUCH! ====== //

DirBridge.dir = DirBridge.getLauncherDir(config.dir);
if (!IOHelper.isDir(DirBridge.dir)) {
    java.nio.file.Files.createDirectory(DirBridge.dir);
}
DirBridge.defaultUpdatesDir = DirBridge.dir.resolve("updates");
if (!IOHelper.isDir(DirBridge.defaultUpdatesDir)) {
    java.nio.file.Files.createDirectory(DirBridge.defaultUpdatesDir);
}
