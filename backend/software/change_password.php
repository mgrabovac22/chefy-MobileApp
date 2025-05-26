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


if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["error" => "Invalid request method"]);
    exit();
}


$input = json_decode(file_get_contents("php://input"), true);


if (!isset($input['user_id'], $input['current_password'], $input['new_password'])) {
    echo json_encode(["error" => "Missing required fields"]);
    exit();
}

$user_id = $input['user_id'];
$current_password = $input['current_password'];
$new_password = $input['new_password'];

try {
    
    $stmt = $pdo->prepare("SELECT password FROM users WHERE id = :user_id");
    $stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $stmt->execute();
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        echo json_encode(["error" => "User not found"]);
        exit();
    }

   
    if (!password_verify($current_password, $user['password'])) {
        echo json_encode(["error" => "Netocna trenutna lozinka"]);
        exit();
    }

   
    $hashed_new_password = password_hash($new_password, PASSWORD_BCRYPT);

   
    $update_stmt = $pdo->prepare("UPDATE users SET password = :new_password WHERE id = :user_id");
    $update_stmt->bindParam(':new_password', $hashed_new_password, PDO::PARAM_STR);
    $update_stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);

    if ($update_stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Password updated successfully"]);
    } else {
        echo json_encode(["error" => "Failed to update password"]);
    }
} catch (PDOException $e) {
    echo json_encode(["error" => "Database query failed"]);
}

$pdo = null;
?>
