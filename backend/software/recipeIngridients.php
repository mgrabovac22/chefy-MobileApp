<?php
	header("Content-Type: application/json");

	// Konfiguracija baze podataka
	$host = "localhost";
	$db_name = "smartcoders";
	$username = "smartcoders";
	$password = "y39T>(";

	// Povezivanje na bazu podataka
	try {
		$pdo = new PDO("mysql:host=$host;dbname=$db_name", $username, $password);
		$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
	} catch (PDOException $e) {
		echo json_encode(["error" => "Database connection failed"]);
		exit();
	}

	// Dohvaćanje metode zahtjeva
	$method = $_SERVER['REQUEST_METHOD'];

	// Rukovanje različitim metodama zahtjeva
	switch ($method) {
		case 'GET':
			// Dohvaćanje `id_recipe` iz URL parametara
			if (!isset($_GET['id_recipe'])) {
				echo json_encode(["error" => "Missing id_recipe parameter"]);
				exit();
			}

			$id_recipe = intval($_GET['id_recipe']); // Sanitizacija ulaza

			try {
				// Priprema upita
				$stmt = $pdo->prepare("
					SELECT 
						recipeIngridients.id_ingridient,
						recipeIngridients.id_recipe,
						recipeIngridients.id_unit,
						recipeIngridients.quantity,
						ingridients.name AS ingredient_name,
						units.display_name AS unit_name
					FROM 
						recipeIngridients
					INNER JOIN 
						ingridients ON recipeIngridients.id_ingridient = ingridients.id
					INNER JOIN 
						units ON recipeIngridients.id_unit = units.id
					WHERE 
						recipeIngridients.id_recipe = :id_recipe
				");

				// Izvršenje upita
				$stmt->bindParam(':id_recipe', $id_recipe, PDO::PARAM_INT);
				$stmt->execute();

				$ingredients = $stmt->fetchAll(PDO::FETCH_ASSOC);
				$count = count($ingredients);

				// Vraćanje podataka kao JSON
				echo json_encode([
					'count' => $count,
					'results' => $ingredients
				]);
			} catch (Exception $e) {
				echo json_encode(["error" => "Failed to fetch ingredients"]);
				
			}
			break;


		case 'POST':
    try {
        // Dohvaćanje podataka iz POST zahtjeva
        $json = file_get_contents("php://input");
        $data = json_decode($json, true);

        // Provjera je li JSON valjan
        if (!is_array($data)) {
            echo json_encode(["error" => "Neispravan JSON format"]);
            exit;
        }

        // Početak transakcije
        $pdo->beginTransaction();

        // Priprema SQL izraza za unos
        $stmt = $pdo->prepare("INSERT INTO recipeIngridients (id_recipe, id_ingridient, id_unit, quantity) 
                               VALUES (:id_recipe, :id_ingridient, :id_unit, :quantity)");

        // Iteracija kroz listu sastojaka
        foreach ($data as $ingredient) {
            // Provjera da li svi potrebni ključevi postoje
            if (!isset($ingredient['ingredientName'], $ingredient['recipeId'], $ingredient['unitName'], $ingredient['quantity'])) {
                throw new Exception("Nedostaju podaci za neki od sastojaka.");
            }

            // Dohvaćanje ID-a sastojka
            $stmt_ing = $pdo->prepare("SELECT id FROM ingridients WHERE name = :ingredientName LIMIT 1");
            $stmt_ing->bindParam(':ingredientName', $ingredient['ingredientName'], PDO::PARAM_STR);
            $stmt_ing->execute();
            $ingredientId = $stmt_ing->fetchColumn();

            // Dohvaćanje ID-a jedinice mjere
            $stmt_unit = $pdo->prepare("SELECT id FROM units WHERE display_name = :unitName LIMIT 1");
            $stmt_unit->bindParam(':unitName', $ingredient['unitName'], PDO::PARAM_STR);
            $stmt_unit->execute();
            $unitId = $stmt_unit->fetchColumn();

            // Ako bilo koji ID nije pronađen, preskačemo unos i prijavljujemo grešku
            if (!$ingredientId || !$unitId) {
                throw new Exception("Neispravan naziv sastojka ili jedinice: {$ingredient['ingredientName']} - {$ingredient['unitName']}");
            }

            // Izvršavanje upita
            $stmt->execute([
                ':id_recipe' => $ingredient['recipeId'],
                ':id_ingridient' => $ingredientId,
                ':id_unit' => $unitId,
                ':quantity' => $ingredient['quantity']
            ]);
        }

        // Potvrda transakcije
        $pdo->commit();
        echo json_encode(["message" => "Svi sastojci uspješno dodani u recept"]);

    } catch (Exception $e) {
        // Ako je došlo do greške, poništi transakciju
        $pdo->rollBack();
        echo json_encode(["error" => "Greška: " . $e->getMessage()]);
    }
    break;

		default:
			echo json_encode(["error" => "Method not allowed"]);
			break;
	}

	// Zatvaranje veze prema bazi
	$pdo = null;
?>
