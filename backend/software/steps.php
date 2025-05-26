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
        if (isset($_GET['id_recipe'])) {
            $id_recipe = intval($_GET['id_recipe']);
            $stmt = $pdo->prepare("SELECT * FROM steps WHERE ID_Recipe = :id_recipe ORDER BY stage ASC");
            $stmt->execute([':id_recipe' => $id_recipe]);
            $steps = $stmt->fetchAll(PDO::FETCH_ASSOC);

            if ($steps) {
                echo json_encode([
                    'id_recipe' => $id_recipe,
                    'steps' => $steps
                ]);
            } else {
                echo json_encode(["error" => "No steps found for the specified recipe"]);
            }
        } else {
            echo json_encode(["error" => "Recipe ID is required"]);
        }
        break;
		
	case 'POST':
		
		// Provjerite je li JSON podatak poslan
		$data = json_decode($_POST['step'], true);

		// Provjerite je li sve u redu
		if (!isset($data['name'], $data['stage'], $data['making_time'], $data['id_recipe'])) {
			echo json_encode(["error" => "Missing required fields"]);
			exit();
		}

		// Provjera da li je slika prisutna
		if (!isset($_FILES['image'])) {
			echo json_encode(["error" => "Image file is missing"]);
			exit();
		}

		// Spremanje podataka u bazu (privremeno bez slike)
		$stmt = $pdo->prepare("INSERT INTO steps (name, description, stage, making_time, image_path, ID_Recipe) VALUES (:name, :description, :stage, :making_time, '', :id_recipe)");
		$stmt->execute([
			':name' => $data['name'],
			':description' => $data['description'],
			':stage' => $data['stage'],
			':making_time' => $data['making_time'],
			':id_recipe' => $data['id_recipe']
		]);

		$stepId = $pdo->lastInsertId(); // Dohvati ID novog koraka

		// Upload slike
		$image = $_FILES['image'];
		$imagePath = "/images/" . $data['id_recipe'] . "-" . $data['stage'] . ".jpg";  // Postavi novi naziv slike
		$targetPath = __DIR__ . $imagePath; // Putanja na serveru

		if (move_uploaded_file($image['tmp_name'], $targetPath)) {
			// Ažuriraj bazu s putanjom slike
			$stmt = $pdo->prepare("UPDATE steps SET image_path = :image_path WHERE id = :id");
			$stmt->execute([
				':image_path' => $imagePath,
				':id' => $stepId
			]);

			echo json_encode(["message" => "Step created successfully", "id" => $stepId, "image_path" => $imagePath]);
		} else {
			echo json_encode(["error" => "Image upload failed"]);
		}
		break;
    
	case 'PUT':
		$data = json_decode(file_get_contents("php://input"), true);

		if (!isset($data['id'], $data['name'], $data['stage'], $data['making_time'], $data['image_path'], $data['id_recipe'])) {
			echo json_encode(["error" => "Missing required fields"]);
			exit();
		}

		$stmt = $pdo->prepare("UPDATE steps SET name = :name, stage = :stage, making_time = :making_time, image_path = :image_path, ID_Recipe = :id_recipe WHERE id = :id");
		$stmt->execute([
			':id' => $data['id'],
			':name' => $data['name'],
			':stage' => $data['stage'],
			':making_time' => $data['making_time'],
			':image_path' => $data['image_path'],
			':id_recipe' => $data['id_recipe']
		]);

		if ($stmt->rowCount() > 0) {
			echo json_encode(["message" => "Step updated successfully"]);
		} else {
			echo json_encode(["error" => "Step not found or no changes made"]);
		}
		break;

	case 'DELETE':
		if (!isset($_GET['id'])) {
			echo json_encode(["error" => "Missing step ID"]);
			exit();
		}

		$id = intval($_GET['id']);
		$stmt = $pdo->prepare("DELETE FROM steps WHERE id = :id");
		$stmt->execute([':id' => $id]);

		if ($stmt->rowCount() > 0) {
			echo json_encode(["message" => "Step deleted successfully"]);
		} else {
			echo json_encode(["error" => "Step not found"]);
		}
		break;
		
    default:
        echo json_encode(["error" => "Method not allowed"]);
        break;
}

$pdo = null;
?>
