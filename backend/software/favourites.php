<?php
header("Content-Type: application/json");

$host = "localhost";
$db_name = "smartcoders";
$username = "smartcoders";
$password = "y39T>(";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db_name", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    echo json_encode(["error" => "Database connection failed"]);
    exit();
}

$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        if (isset($_GET['user_id'])) {
            $user_id = intval($_GET['user_id']);
            $stmt = $pdo->prepare(
                "SELECT r.* FROM recipes r 
                INNER JOIN favourites f ON r.id = f.id_recipe 
                WHERE f.id_user = :user_id"
            );
            $stmt->execute([':user_id' => $user_id]);
            $recipes = $stmt->fetchAll(PDO::FETCH_ASSOC);

            echo json_encode([
                'user_id' => $user_id,
                'count' => count($recipes),
                'results' => $recipes
            ]);
        } else {
            echo json_encode(["error" => "User ID is required"]);
        }
        break;

    case 'POST':
        $data = json_decode(file_get_contents("php://input"), true);
        if (isset($data['user_id']) && isset($data['recipe_id'])) {
            $user_id = intval($data['user_id']);
            $recipe_id = intval($data['recipe_id']);

            // Provjera postoji li već u favoritima
            $checkStmt = $pdo->prepare("SELECT COUNT(*) FROM favourites WHERE id_user = :user_id AND id_recipe = :recipe_id");
            $checkStmt->execute([':user_id' => $user_id, ':recipe_id' => $recipe_id]);
            $exists = $checkStmt->fetchColumn();

            if ($exists) {
                echo json_encode(["error" => "Recept je već u favoritima"]);
            } else {
                $stmt = $pdo->prepare("INSERT INTO favourites (id_user, id_recipe) VALUES (:user_id, :recipe_id)");
                $stmt->execute([':user_id' => $user_id, ':recipe_id' => $recipe_id]);
                echo json_encode(["message" => "Recept dodan u favorite"]);
            }
        } else {
            echo json_encode(["error" => "User ID i Recipe ID su obavezni"]);
        }
        break;

    case 'DELETE':
        parse_str(file_get_contents("php://input"), $data);
        if (isset($data['user_id']) && isset($data['recipe_id'])) {
            $stmt = $pdo->prepare("DELETE FROM favourites WHERE id_user = :user_id AND id_recipe = :recipe_id");
            $stmt->execute([':user_id' => $data['user_id'], ':recipe_id' => $data['recipe_id']]);
            echo json_encode(["message" => "Recept uklonjen iz favorita"]);
        } else {
            echo json_encode(["error" => "User ID i Recipe ID su obavezni"]);
        }
        break;

    default:
        echo json_encode(["error" => "Method not allowed"]);
        break;
}

$pdo = null;
?>
