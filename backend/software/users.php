<?php
session_start();
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
        $headers = apache_request_headers();
        $userId = $headers['user_id'] ?? null;

        if ($userId) {
            try {
                $stmt = $pdo->prepare("SELECT id, username, email, firstName, lastName FROM users WHERE id = :id");
                $stmt->bindParam(':id', $userId, PDO::PARAM_INT);
                $stmt->execute();
                $user = $stmt->fetch(PDO::FETCH_ASSOC);

                if ($user) {
                    echo json_encode($user);
                } else {
                    echo json_encode(["error" => "No user found"]);
                }
            } catch (PDOException $e) {
                echo json_encode(["error" => "Database query failed"]);
            }
        } else {
            echo json_encode(["error" => "User ID not provided"]);
        }
        break;

    case 'POST':
        $json = file_get_contents("php://input");
        $data = json_decode($json, true);

        if (isset($data['register'])) {
            $username = $data['username'] ?? null;
            $password = $data['password'] ?? null;
            $email = $data['email'] ?? null;
            $firstName = $data['firstName'] ?? null;
            $lastName = $data['lastName'] ?? null;

            if (!$username || !$password || !$email) {
                echo json_encode(["error" => "Missing required fields"]);
                exit();
            }

            $hashedPassword = password_hash($password, PASSWORD_BCRYPT);

            try {
                $stmt = $pdo->prepare("INSERT INTO users (username, password, email, firstName, lastName) VALUES (:username, :password, :email, :firstName, :lastName)");
                $stmt->bindParam(':username', $username);
                $stmt->bindParam(':password', $hashedPassword);
                $stmt->bindParam(':email', $email);
                $stmt->bindParam(':firstName', $firstName);
                $stmt->bindParam(':lastName', $lastName);

                if ($stmt->execute()) {
					$userId = $pdo->lastInsertId();
                    echo json_encode(["success" => true, "message" => "User successfully registered", "user_id" => $userId]);
                } else {
                    echo json_encode(["error" => "Error during user registration"]);
                }
            } catch (PDOException $e) {
                echo json_encode(["error" => "Database query failed"]);
            }
        } else {
            echo json_encode(["error" => "Invalid request"]);
        }
        break;

    default:
        echo json_encode(["error" => "Method not allowed"]);
        break;
}

$pdo = null;
?>
