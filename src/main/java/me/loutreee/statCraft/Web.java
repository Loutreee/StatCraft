package me.loutreee.statCraft;

import io.javalin.Javalin;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import java.io.File;
import java.util.Arrays;

import static org.apache.logging.log4j.LogManager.getLogger;

public class Web {
    private Javalin app;

    // Méthode pour démarrer le serveur Javalin
    public void start() {
        app = Javalin.create(config -> {
            // Vous pouvez ajouter des configurations ici si nécessaire
        });
        // Nouvelle route pour afficher les scores des joueurs en texte
        app.get("/", ctx -> {
            StringBuilder resultBuilder = new StringBuilder();
            File dataFolder = new File("player_statistics");
            // getLogger().info("Chemin absolu : {}", dataFolder.getAbsolutePath());
            if (!dataFolder.exists() || !dataFolder.isDirectory()) {
                ctx.result("Aucune donnée trouvée dans 'data/player_statistics'");
                return;
            }
            File[] sessionDirs = dataFolder.listFiles(File::isDirectory);
            if (sessionDirs == null || sessionDirs.length == 0) {
                ctx.result("Aucune session trouvée dans 'data/player_statistics'");
                return;
            }
            // Trier les sessions par nom pour une lecture cohérente
            Arrays.sort(sessionDirs, (a, b) -> a.getName().compareTo(b.getName()));
            for (File sessionDir : sessionDirs) {
                resultBuilder.append("Score total des joueurs").append("\n");
                File[] playerDirs = sessionDir.listFiles(File::isDirectory);
                if (playerDirs == null || playerDirs.length == 0) {
                    resultBuilder.append("  Aucun joueur trouvé\n");
                } else {
                    Arrays.sort(playerDirs, (a, b) -> a.getName().compareTo(b.getName()));
                    for (File playerDir : playerDirs) {
                        File[] xmlFiles = playerDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
                        if (xmlFiles == null || xmlFiles.length == 0) {
                            resultBuilder.append("  ").append(playerDir.getName()).append(" : Aucune donnée XML trouvée\n");
                        } else {
                            // Trier les fichiers XML par nom et prendre le dernier (le plus récent)
                            Arrays.sort(xmlFiles, (a, b) -> a.getName().compareTo(b.getName()));
                            File lastXml = xmlFiles[xmlFiles.length - 1];
                            try {
                                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                                DocumentBuilder db = dbf.newDocumentBuilder();
                                Document doc = db.parse(lastXml);
                                doc.getDocumentElement().normalize();
                                String score = doc.getElementsByTagName("scoreTotal").item(0).getTextContent();
                                resultBuilder.append("  ").append(playerDir.getName())
                                        .append(" : ").append(score).append("\n");
                            } catch (Exception e) {
                                resultBuilder.append("  ").append(playerDir.getName())
                                        .append(" : Erreur lors du parsing XML\n");
                            }
                        }
                    }
                }
                resultBuilder.append("\n");
            }
            ctx.result(resultBuilder.toString());
        });

        // Démarrer le serveur sur le port 7070
        app.start(27800);
        getLogger().info("[Web] Serveur Javalin démarré sur le port 27800");
    }

    // Méthode pour arrêter proprement le serveur Javalin
    public void stop() {
        if (app != null) {
            app.stop();
            getLogger().info("[Web] Serveur Javalin arrêté");
        }
    }
}
