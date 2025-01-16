package ch.heigvd.dai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static ConcurrentHashMap<String,Boolean> session = new ConcurrentHashMap<>();

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

            if (username == null || username.isEmpty()) {
                ctx.result("Le nom d'utilisateur ne peut pas être vide.");
                return;
            }else if(!session.isEmpty() && session.get(username)){
                ctx.result("Vous êtes déja connecter depuis un autre apareil");
                return;
            }
            session.put(username,true);

            // Gestion des cookies
            ctx.cookie("username", username);
            ctx.redirect("/notes");

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
                ctx.html("<pre>" + result.toString() + "</pre>" +
                        "<p><a href='/logout'>Logout</a></p>");

            } else {
                // Si l'utilisateur n'existe pas, afficher un message
                ctx.html("<h1>Utilisateur non trouvé.</h1><p><a href='/login'>Retour à la page de connexion</a></p><p><a href='/'>Retour à la page home</a></p>");
            }
        });

        // Page pour logout
        app.get("/logout", ctx -> {
            String username = ctx.cookie("username");

            if (username == null) {
                ctx.result("Vous n'êtes pas connecté.");
                return;
            }

            // Supprimer le token de l'utilisateur si présent
            if (session.get(username)) {
                session.put(username,false);
                // Suppression des cookies côté client
                ctx.cookie("username", "", 0);

                ctx.html("<h1>Vous êtes maintenant déconnecté.</h1>" +
                        "<p><a href='/login'>Retour à la page de connexion</a></p>" +
                        "<p><a href='/'>Retour à la page home</a></p>");
            } else {
                ctx.result("Aucune session trouvée pour cet utilisateur.");
            }
        });


        // Page pour ajouter des notes
        app.post("/add-note", ctx -> {
                String username = ctx.cookie("username");

                ObjectMapper mapper = new ObjectMapper();
                if(!session.get(username)){
                    ctx.result("Veuillez vous connecter avant d'ajouter une note");
                    return;
                }

                // Lecture et ajout de la note dans data.json (inchangé)
                File file = new File("src/main/resources/data.json");
                if (!file.exists()) {
                    ctx.result("Le fichier data.json est introuvable !");
                    return;
                }

                InputStream inputStream = new FileInputStream(file);
                JsonNode rootNode = mapper.readTree(inputStream);
                JsonNode userNode = rootNode.get(username);

                if (userNode == null) {
                    ((ObjectNode) rootNode).set(username, mapper.createObjectNode().put("notes", mapper.createObjectNode()));
                    userNode = rootNode.get(username);
                }

                JsonNode notesNode = userNode.get("notes");
                String noteKey = "note_" + UUID.randomUUID().toString();
                ObjectNode newNote = mapper.createObjectNode();
                newNote.put("branch", ctx.formParam("branch"));
                newNote.put("nom", ctx.formParam("nom"));
                newNote.put("note", Double.parseDouble(ctx.formParam("note")));
                ((ObjectNode) notesNode).set(noteKey, newNote);

                FileOutputStream fos = new FileOutputStream("src/main/resources/data.json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(fos, rootNode);

                ctx.result("Note ajoutée avec succès !");

        });

        app.post("/delete-note", ctx->{
            String username = ctx.cookie("username");

            ObjectMapper mapper = new ObjectMapper();
            if(username == null || !session.get(username)){
                ctx.result("Veuillez vous connecter avant de supprimer une note.");
                return;
            }

            String branch = ctx.formParam("branch");
            String noteName = ctx.formParam("nom");

            File file = new File("src/main/resources/data.json");
            if (!file.exists()) {
                ctx.result("Le fichier data.json est introuvable !");
                return;
            }
            InputStream inputStream = new FileInputStream(file);
            JsonNode rootNode = mapper.readTree(inputStream);
            JsonNode userNode = rootNode.get(username);

            if (userNode == null) {
                ctx.result("Utilisateur non trouvé.");
                return;
            }
            JsonNode notesNode = userNode.get("notes");

            if (notesNode == null) {
                ctx.result("Aucune note trouvée pour cet utilisateur.");
                return;
            }
            Iterator<Map.Entry<String, JsonNode>> fields = notesNode.fields();
            String noteKeyToRemove = null;
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> noteEntry = fields.next();
                JsonNode noteNode = noteEntry.getValue();
                String noteBranch = noteNode.get("branch").asText();
                String noteNom = noteNode.get("nom").asText();

                if (noteBranch.equals(branch) && noteNom.equals(noteName)) {
                    noteKeyToRemove = noteEntry.getKey();
                    break;
                }
            }

            if (noteKeyToRemove == null) {
                ctx.result("Note avec la branche \"" + branch + "\" et le nom \"" + noteName + "\" non trouvée.");
                return;
            }

            // Suppression de la note trouvée
            ((ObjectNode) notesNode).remove(noteKeyToRemove);

            // Sauvegarder les changements dans le fichier data.json
            FileOutputStream fos = new FileOutputStream("src/main/resources/data.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, rootNode);

            ctx.result("Note supprimée avec succès !");
        });

        // Page pour mettre à jour une note
        app.post("/update-note", ctx -> {
            String username = ctx.cookie("username");

            ObjectMapper mapper = new ObjectMapper();
            if (username == null || !session.get(username)) {
                ctx.result("Veuillez vous connecter avant de modifier une note.");
                return;
            }

            String branch = ctx.formParam("branch");
            String noteName = ctx.formParam("nom");
            double newNoteValue;

            try {
                newNoteValue = Double.parseDouble(ctx.formParam("note"));
            } catch (NumberFormatException e) {
                ctx.result("La note doit être un nombre valide.");
                return;
            }

            File file = new File("src/main/resources/data.json");
            if (!file.exists()) {
                ctx.result("Le fichier data.json est introuvable !");
                return;
            }

            InputStream inputStream = new FileInputStream(file);
            JsonNode rootNode = mapper.readTree(inputStream);
            JsonNode userNode = rootNode.get(username);

            if (userNode == null) {
                ctx.result("Utilisateur non trouvé.");
                return;
            }

            JsonNode notesNode = userNode.get("notes");
            if (notesNode == null) {
                ctx.result("Aucune note trouvée pour cet utilisateur.");
                return;
            }

            Iterator<Map.Entry<String, JsonNode>> fields = notesNode.fields();
            String noteKeyToUpdate = null;
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> noteEntry = fields.next();
                JsonNode noteNode = noteEntry.getValue();
                String noteBranch = noteNode.get("branch").asText();
                String noteNom = noteNode.get("nom").asText();

                if (noteBranch.equals(branch) && noteNom.equals(noteName)) {
                    noteKeyToUpdate = noteEntry.getKey();
                    break;
                }
            }

            if (noteKeyToUpdate == null) {
                ctx.result("Note avec la branche \"" + branch + "\" et le nom \"" + noteName + "\" non trouvée.");
                return;
            }

            // Mise à jour de la note uniquement
            ((ObjectNode) notesNode.get(noteKeyToUpdate)).put("note", newNoteValue);

            // Sauvegarde des modifications dans le fichier data.json
            FileOutputStream fos = new FileOutputStream(file);
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, rootNode);

            ctx.result("Note mise à jour avec succès !");
        });

    }

}
