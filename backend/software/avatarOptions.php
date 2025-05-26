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
		
			if (isset($_GET['id_user'])) {
                $id_user = $_GET['id_user'];
                $stmt = $pdo->prepare("SELECT * FROM avatarOptions WHERE id_user = :id_user");
                $stmt->execute([':id_user' => "$id_user"]);
            } else {
                $stmt = $pdo->query("SELECT * FROM avatarOptions");
            }
		
			
            $avatarOptions = $stmt->fetchAll(PDO::FETCH_ASSOC);
			
            $count = count($avatarOptions);
			
            echo json_encode([
                'count' => $count,
                'results' => $avatarOptions
            ]);
            break;
			
		case 'POST':
			// Dohvaćanje podataka iz tijela zahtjeva
			$json = file_get_contents("php://input");

			// Pretvaranje JSON podataka u PHP asocijativno polje
			$data = json_decode($json, true);

			// Provjera je li JSON ispravan i je li niz (lista)
			if (!is_array($data)) {
				echo json_encode(["error" => "Neispravan JSON format"]);
				exit;
			}

			$pdo->beginTransaction(); // Početak transakcije

			try {
				// Priprema SQL izraza za umetanje
				$stmt = $pdo->prepare("INSERT INTO avatarOptions (id_user, id_avatarOptionCategory, name) 
									   VALUES (:id_user, :id_avatarOptionCategory, :name)");

				foreach ($data as $option) {
					// Provjera potrebnih podataka
					if (!isset($option['id_user'], $option['id_avatarOptionCategory'], $option['name'])) {
						throw new Exception("Nedostaju potrebni podaci za jedan ili više unosa");
					}

					// Povezivanje parametara i izvršavanje upita
					$stmt->bindParam(':id_user', $option['id_user']);
					$stmt->bindParam(':id_avatarOptionCategory', $option['id_avatarOptionCategory']);
					$stmt->bindParam(':name', $option['name']);
					$stmt->execute();
				}

				$pdo->commit(); // Potvrda transakcije
				echo json_encode(["message" => "Sve opcije uspješno dodane"]);
			} catch (Exception $e) {
				$pdo->rollBack(); // Poništavanje transakcije u slučaju greške
				echo json_encode(["error" => "Greška prilikom dodavanja opcija: " . $e->getMessage()]);
			}
			break;
			
		case 'PUT':
			// Dohvaćanje podataka iz PUT zahtjeva
			$json = file_get_contents("php://input");
			
			// Pretvaranje JSON podataka u PHP asocijativno polje
			$data = json_decode($json, true);
			
			//
			if (!is_array($data)) {
            echo json_encode(["error" => "Neispravan JSON format"]);
            exit;
        }

			$pdo->beginTransaction(); // Početak transakcije

			try {
				// Priprema SQL izraza za ažuriranje
				$stmt = $pdo->prepare("UPDATE avatarOptions 
									   SET name = :name 
									   WHERE id_user = :id_user 
									   AND id_avatarOptionCategory = :id_avatarOptionCategory");

				foreach ($data as $option) {
					// Provjera potrebnih podataka
					if (!isset($option['id_user'], $option['id_avatarOptionCategory'], $option['name'])) {
						throw new Exception("Nedostaju potrebni podaci za jedan ili više unosa");
					}

					// Povezivanje parametara i izvršavanje upita
					$stmt->bindParam(':id_user', $option['id_user']);
					$stmt->bindParam(':id_avatarOptionCategory', $option['id_avatarOptionCategory']);
					$stmt->bindParam(':name', $option['name']);
					$stmt->execute();
				}

				$pdo->commit(); // Potvrda transakcije
				echo json_encode(["message" => "Sve opcije uspješno ažurirane"]);
			} catch (Exception $e) {
				$pdo->rollBack(); // Poništavanje transakcije u slučaju greške
				echo json_encode(["error" => "Greška prilikom ažuriranja opcija: " . $e->getMessage()]);
			}
			break;

    default:
        echo json_encode(["error" => "Method not allowed"]);
        break;

	}//switch
 
// Close the database connection
$pdo = null;
?>