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
        $baseQuery = "
            SELECT 
                recipes.id,
                recipes.name,
                recipes.description,
                recipes.picture_path,
                recipes.category,
                recipes.making_time,
				recipes.id_user,
                users.username
            FROM recipes
            JOIN users ON recipes.id_user = users.id
        ";
    
        $conditions = [];
        $params = [];
		
		if (isset($_GET['id'])) {
			$conditions[] = "recipes.id = :id";
			$params[':id'] = (int)$_GET['id'];
		}
    
        if (isset($_GET['name'])) {
            $conditions[] = "recipes.name LIKE :name";
            $params[':name'] = "%" . $_GET['name'] . "%";
        }
    
        if (isset($_GET['category'])) {
            $conditions[] = "recipes.category = :category";
            $params[':category'] = $_GET['category'];
        }
    
        if (isset($_GET['making_time'])) {
            $conditions[] = "recipes.making_time <= :making_time";
            $params[':making_time'] = (int)$_GET['making_time'];
        }
    
        if (isset($_GET['ingredients'])) {
            $ingredients = explode(',', $_GET['ingredients']); 
            $placeholders = [];
            foreach ($ingredients as $index => $ingredient) {
                $placeholders[] = ":ingredient$index";
                $params[":ingredient$index"] = trim($ingredient);
            }
    
            $conditions[] = "recipes.id IN (
                SELECT recipeIngridients.id_recipe
                FROM recipeIngridients
                JOIN ingridients ON recipeIngridients.id_ingridient = ingridients.id
                WHERE ingridients.name IN (" . implode(',', $placeholders) . ")
                GROUP BY recipeIngridients.id_recipe
                HAVING COUNT(DISTINCT ingridients.name) = " . count($placeholders) . "
            )";
        }
		
		// Filtriranje prema id_user
		if (isset($_GET['id_user'])) {
			$conditions[] = "recipes.id_user = :id_user";
			$params[':id_user'] = (int)$_GET['id_user'];
		}
		
        if (!empty($conditions)) {
            $baseQuery .= " WHERE " . implode(" AND ", $conditions);
        }
		
		
		/*if (!isset($_GET['id'])){
			// Paginacija: odredite koji je broj stranice i broj zapisa po stranici
			$page = isset($_GET['page']) ? (int)$_GET['page'] : 1; // Zadana stranica je 1
			$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 5; // Zadani limit je 5
			$offset = ($page - 1) * $limit;

			// Dodajte LIMIT i OFFSET u upit
			$baseQuery .= " LIMIT :limit OFFSET :offset";
			$params[':limit'] = $limit;
			$params[':offset'] = $offset;
		}*/
		
		
    
        $stmt = $pdo->prepare($baseQuery);
        $stmt->execute($params);
    
        $recipes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
        echo json_encode(['count' => count($recipes), 'results' => $recipes]);
        break;

    case 'POST':
		
		// Provjerite je li JSON podatak poslan
		$data = json_decode($_POST['recipe'], true);
		

		// Provjerite je li sve u redu
		if (!isset($data['name'], $data['description'], $data['making_time'], $data['category'], $data['id_user'])) {
			echo json_encode(["error" => $data ]);
			exit();
		}
		
		

		// Provjera da li je slika prisutna
		if (!isset($_FILES['image'])) {
			echo json_encode(["error" => "Image file is missing"]);
			exit();
		}


		// Spremanje podataka u bazu
		$stmt = $pdo->prepare("INSERT INTO recipes (name, description, making_time, picture_path, category, ID_User) VALUES (:name, :description, :making_time, '', :category, :id_user)");
		$stmt->execute([
			':name' => $data['name'],
			':description' => $data['description'],
			':making_time' => $data['making_time'],
			':category' => $data['category'],
			':id_user' => $data['id_user']
		]);

		$recipeId = $pdo->lastInsertId(); // Dohvati ID novog recepta

		// Upload slike
		$image = $_FILES['image'];
		$imagePath = "/images/" . $recipeId . ".jpg";  // Postavi novi naziv slike
		$targetPath = __DIR__ . $imagePath; // Putanja na serveru

		

		if (move_uploaded_file($image['tmp_name'], $targetPath)) {
			
			
			// Ažuriraj bazu s putanjom slike
			$stmt = $pdo->prepare("UPDATE recipes SET picture_path = :picture_path WHERE id = :id");
			$stmt->execute([
				':picture_path' => $imagePath,
				':id' => $recipeId
			]);

			

			echo json_encode(["message" => "Recipe created successfully", "id" => $recipeId, "image_path" => $imagePath, "error" => "Nema"]);
		} else {
			
			echo json_encode(["error" => "Image upload failed", "image_error_code" => "test"]);
		}
		
		break;
		
    case 'DELETE':
        if (!isset($_GET['id'])) {
            echo json_encode(["error" => "Missing recipe ID"]);
            exit();
        }

        $id = $_GET['id'];
		
		// Putanja do direktorija sa slikama
		$imageDir = __DIR__ . "/images/";

		// Traženje svih datoteka koje počinju s ID-em recepta
		$files = glob($imageDir . $id . '*'); // Traži sve datoteke koje počinju s ID-em recepta

		// Brisanje pronađenih datoteka
		foreach ($files as $file) {
			if (is_file($file)) {
				unlink($file); // Briše datoteku
			}
		}
		
        $stmt = $pdo->prepare("DELETE FROM recipes WHERE id = :id");
        $stmt->execute([':id' => $id]);

        if ($stmt->rowCount() > 0) {
            echo json_encode(["message" => "Recipe deleted successfully"]);
        } else {
            echo json_encode(["error" => "Recipe not found"]);
        }
        break;
		
	case 'PUT':
		$data = json_decode(file_get_contents("php://input"), true);

		if (!isset($data['id'], $data['name'], $data['description'], $data['making_time'], $data['picture_path'], $data['category'], $data['id_user'])) {
			echo json_encode(["error" => "Missing required fields"]);
			exit();
		}

		$stmt = $pdo->prepare("UPDATE recipes SET name = :name, description = :description, making_time = :making_time, picture_path = :picture_path, category = :category, id_user = :id_user WHERE id = :id");
		$stmt->execute([
			':id' => $data['id'],
			':name' => $data['name'],
			':description' => $data['description'],
			':making_time' => $data['making_time'],
			':picture_path' => $data['picture_path'],
			':category' => $data['category'],
			':id_user' => $data['id_user']
		]);

		if ($stmt->rowCount() > 0) {
			echo json_encode(["message" => "Recipe updated successfully"]);
		} else {
			echo json_encode(["error" => "Recipe not found or no changes made"]);
		}
		break;
    

    default:
        echo json_encode(["error" => "Method not allowed"]);
        break;
}

$pdo = null;
?>
