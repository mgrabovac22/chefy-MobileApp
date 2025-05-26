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
    case 'POST':
        $input = json_decode(file_get_contents("php://input"), true);

        if ((!isset($input['username']) && !isset($input['email'])) || !isset($input['password'])) {
            echo json_encode(["error" => "Missing email/username or password"]);
            exit();
        }

        $identifier = trim($input['username'] ?? $input['email']);
        $password = trim($input['password']);

        try {
            $query = strpos($identifier, '@') !== false
                ? "SELECT * FROM users WHERE email = :identifier"
                : "SELECT * FROM users WHERE username = :identifier";

            $stmt = $pdo->prepare($query);
            $stmt->bindParam(':identifier', $identifier, PDO::PARAM_STR);
            $stmt->execute();
            $user = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($user && password_verify($password, $user['password'])) {
                echo json_encode([
                    "success" => true,
                    "message" => "Login successful",
                    "user" => [
                        "id" => $user['id'],
                        "username" => $user['username'],
                        "email" => $user['email']
                    ]
                ]);
            } else {
                echo json_encode(["success" => false, "message" => "Invalid username/email or password"]);
            }
        } catch (PDOException $e) {
            echo json_encode(["error" => "Database query failed"]);
        }
        break;

    default:
        echo json_encode(["error" => "Method not allowed"]);
        break;
}

$pdo = null;
?>
