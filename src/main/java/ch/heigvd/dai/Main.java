package ch.heigvd.dai;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Main {

        public static void main(String[] args) throws IOException {
            Javalin app = Javalin.create().start(5000); // Créer et lancer le serveur
            app.get("/", ctx -> {
                ctx.html("<h1>Bienvenue sur le projet pratique 3 de DAI !</h1>" +
                        "<p>Cliquez <a href='/note'>ici</a> pour voir les notes.</p>");
            });

            // Lecture du fichier JSON depuis les ressources
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = Main.class.getResourceAsStream("/data.json");
            JsonNode jsonNode = mapper.readTree(inputStream);

            // Regrouper les notes par branche (branch)
            Map<String, List<String>> notesParBranche = new HashMap<>();

            jsonNode.fields().forEachRemaining(entry -> {
                JsonNode noteData = entry.getValue();
                String branch = noteData.get("branch").asText();
                String nom = noteData.get("nom").asText();
                double noteValue = noteData.get("note").asDouble();

                // Ajouter la note à la bonne branche
                String formattedNote = nom + " : " + noteValue;
                notesParBranche.computeIfAbsent(branch, k -> new ArrayList<>()).add(formattedNote);
            });

            // Endpoint qui affiche les notes groupées par branche
            app.get("/note", ctx -> {
                StringBuilder result = new StringBuilder();
                notesParBranche.forEach((branch, notes) -> {
                    result.append("Branche : ").append(branch).append("\n");
                    notes.forEach(note -> result.append(" - ").append(note).append("\n"));
                });
                ctx.result(result.toString());
            });
        }

}