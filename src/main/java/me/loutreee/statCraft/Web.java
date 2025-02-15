package me.loutreee.statCraft;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class Web {
    private Javalin app;

    public void start(int port) {
        app = Javalin.create(config -> {
            // Sert les fichiers statiques depuis /web
            config.staticFiles.add("/web", Location.CLASSPATH);
        });

        // Route scoreboard (affichage texte)
        app.get("/scores", ctx -> {
            StringBuilder resultBuilder = new StringBuilder();
            File dataFolder = new File("player_statistics");
            // System.out.println("Chemin absolu : " + dataFolder.getAbsolutePath());

            if (!dataFolder.exists() || !dataFolder.isDirectory()) {
                ctx.result("Aucune donnée trouvée dans 'player_statistics'");
                return;
            }
            File[] sessionDirs = dataFolder.listFiles(File::isDirectory);
            if (sessionDirs == null || sessionDirs.length == 0) {
                ctx.result("Aucune session trouvée dans 'player_statistics'");
                return;
            }

            Arrays.sort(sessionDirs, Comparator.comparing(File::getName));
            for (File sessionDir : sessionDirs) {
                resultBuilder.append("Score total des joueurs (Session : ")
                        .append(sessionDir.getName()).append(")\n");
                File[] playerDirs = sessionDir.listFiles(File::isDirectory);
                if (playerDirs == null || playerDirs.length == 0) {
                    resultBuilder.append("  Aucun joueur trouvé\n");
                } else {
                    Arrays.sort(playerDirs, Comparator.comparing(File::getName));
                    for (File playerDir : playerDirs) {
                        File[] xmlFiles = playerDir.listFiles(
                                (dir, name) -> name.toLowerCase().endsWith(".xml")
                        );
                        if (xmlFiles == null || xmlFiles.length == 0) {
                            resultBuilder.append("  ")
                                    .append(playerDir.getName())
                                    .append(" : Aucune donnée XML trouvée\n");
                        } else {
                            Arrays.sort(xmlFiles, Comparator.comparing(File::getName));
                            File lastXml = xmlFiles[xmlFiles.length - 1];
                            try {
                                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                                DocumentBuilder db = dbf.newDocumentBuilder();
                                Document doc = db.parse(lastXml);
                                doc.getDocumentElement().normalize();
                                String score = doc.getElementsByTagName("scoreTotal")
                                        .item(0)
                                        .getTextContent();
                                resultBuilder.append("  ")
                                        .append(playerDir.getName())
                                        .append(" : ")
                                        .append(score)
                                        .append("\n");
                            } catch (Exception e) {
                                resultBuilder.append("  ")
                                        .append(playerDir.getName())
                                        .append(" : Erreur lors du parsing XML\n");
                            }
                        }
                    }
                }
                resultBuilder.append("\n");
            }
            ctx.result(resultBuilder.toString());
        });

        app.start(port);
        System.out.println("[Web] Serveur Javalin démarré sur le port : " + port);
    }

    public void stop() {
        if (app != null) {
            app.stop();
            System.out.println("[Web] Serveur Javalin arrêté");
        }
    }
}
