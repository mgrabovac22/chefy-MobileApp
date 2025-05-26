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
        if (isset($_GET['id'])) {
            $id = intval($_GET['id']);
            $stmt = $pdo->prepare("SELECT * FROM ingridients WHERE id = :id");
            $stmt->execute([':id' => $id]);
            $ingredient = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($ingredient) {
                echo json_encode($ingredient);
            } else {
                echo json_encode(["error" => "Ingredient not found"]);
            }
        } else {
            $stmt = $pdo->query("SELECT * FROM ingridients");
            $ingredients = $stmt->fetchAll(PDO::FETCH_ASSOC);

            echo json_encode([
                'count' => count($ingredients),
                'results' => $ingredients
            ]);
        }
        break;

    default:
        echo json_encode(["error" => "Method not allowed"]);
        break;
}

$pdo = null;
?>
