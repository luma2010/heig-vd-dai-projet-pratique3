package ch.heigvd.dai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Javalin app = Javalin.create().start(5000);

        // Page d'accueil
        app.get("/", ctx -> {
            ctx.html("<h1>Bienvenue sur le projet pratique 3 de DAI !</h1>" +
                    "<p>Cliquez <a href='/notes'>ici</a> pour voir les notes.</p>");
        });

        // Page de connexion GET
        app.get("/login", ctx -> {
            ctx.html("<h1>Page de Connexion</h1>" +
                    "<form action='/login' method='POST'>" +
                    "<label for='username'>Nom d'utilisateur :</label><br>" +
                    "<input type='text' id='username' name='username'><br><br>" +
                    "<input type='submit' value='Se connecter'>" +
                    "</form>");
        });

        // Page de connexion POST
        app.post("/login", ctx -> {
            String username = ctx.formParam("username");

            if (username != null && !username.isEmpty()) {
                // Création du cookie
                ctx.cookie("username", username);

                // Redirection sur la page de note
                ctx.redirect("/notes");
            } else {
                ctx.result("Le nom d'utilisateur ne peut pas être vide.");
            }
        });

        // Page d'affichage de note
        app.get("/notes", ctx -> {
            // Récupération du cookie
            String username = ctx.cookie("username");

            // Si aucun cookie -> redirection sur le login
            if (username == null) {
                ctx.redirect("/login");
                return;
            }

            // Création d'un ObjectMapper pour lire le JSON (Jackson)
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("src/main/resources/data.json");

            if (!file.exists()) {
                ctx.result("Le fichier data.json est introuvable !");
                return;
            }
            InputStream inputStream = new FileInputStream(file);

            // Création d'un JsonNode (Jackson) -> vue en arbre du JSON
            JsonNode rootNode = mapper.readTree(inputStream);

            // Récupération des notes d'un user
            JsonNode userNode = rootNode.get(username);

            if (userNode != null) {
                // Récupération des notes
                JsonNode notesNode = userNode.get("notes");

                // Utilisation d'une Map pour regrouper les notes par branches (code venant de la documentation Jackson : https://jenkov.com/tutorials/java-json/jackson-jsonnode.html#iterate-jsonnode-fields)
                Map<String, StringBuilder> notesParBranche = new HashMap<>();

                Iterator<Map.Entry<String, JsonNode>> fields = notesNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> noteEntry = fields.next();
                    JsonNode noteNode = noteEntry.getValue();
                    String branch = noteNode.get("branch").asText();
                    String name = noteNode.get("nom").asText();
                    double note = noteNode.get("note").asDouble();

                    // Rajout d'une branche si elle n'existe pas encore
                    notesParBranche.putIfAbsent(branch, new StringBuilder());
                    // Utilisation d'un StringBuilder pour rajouter sans devoir crée une nouvelle String
                    notesParBranche.get(branch).append(name).append(" : ").append(note).append("\n");
                }

                // Construction de l'affichage
                StringBuilder result = new StringBuilder();
                for (Map.Entry<String, StringBuilder> entry : notesParBranche.entrySet()) {
                    result.append("Branche: ").append(entry.getKey()).append("\n");
                    result.append(entry.getValue().toString()).append("\n");
                }

                // Affichage du résultat
                ctx.result(result.toString());
            } else {
                // Si l'utilisateur n'existe pas, afficher un message
                ctx.html("<h1>Utilisateur non trouvé.</h1><p><a href='/login'>Retour à la page de connexion</a></p><p><a href='/'>Retour à la page home</a></p>");
            }
        });

        // Page pour logout
        app.get("/logout", ctx -> {
            // Suppression du cookie actuel (maxAge à 0 permet de supprimer le cookie)
            ctx.cookie("username", "", 0);
            ctx.html("<h1>Vous êtes maintenant déconnecté.</h1><p><a href='/login'>Retour à la page de connexion</a></p>" +
                    "<p><a href='/'>Retour à la page home</a></p>");
        });

        // Page pour ajouter des notes
        app.post("/add-note", ctx -> {
            // Récupération des paramêtre envoyer en POST
            String username = ctx.formParam("username");
            String branch = ctx.formParam("branch");
            String nom = ctx.formParam("nom");
            double note = Double.parseDouble(ctx.formParam("note"));

            // Permet de sauvegarder le cookie username
            ctx.cookie("username", username);

            // Lecture du fichier JSON
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("src/main/resources/data.json");

            if (!file.exists()) {
                ctx.result("Le fichier data.json est introuvable !");
                return;
            }
            InputStream inputStream = new FileInputStream(file);
            JsonNode rootNode = mapper.readTree(inputStream);
            JsonNode userNode = rootNode.get(username);

            // Vérifie si l'utilisateur a pu être trouver
            if (userNode == null) {
                // Si l'utilisateur n'existe pas -> ajout de l'utilisateur dans le JSON (utilisation de cast en ObjectNode)
                ((ObjectNode) rootNode).set(username, mapper.createObjectNode().put("notes", mapper.createObjectNode()));
                userNode = rootNode.get(username);
            }

            // Auto incrémentation de la clef "note"
            JsonNode notesNode = userNode.get("notes");
            String noteKey = "note" + (notesNode.size() + 1);

            // Création de la note à ajouter
            ObjectNode newNote = mapper.createObjectNode();
            newNote.put("branch", branch);
            newNote.put("nom", nom);
            newNote.put("id", notesNode.size()); // ID de la note (taille de la liste actuelle)
            newNote.put("note", note);

            // Ajout de la nouvelle note
            ((ObjectNode) notesNode).set(noteKey, newNote);

            // Sauvegarde des nouvelles modifications
            FileOutputStream fos = new FileOutputStream("src/main/resources/data.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, rootNode);

            ctx.result("Note ajoutée avec succès !");
        });
    }
}
