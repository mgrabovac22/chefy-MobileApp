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
            $stmt = $pdo->query("SELECT * FROM avatarOptionCategories");
			
            $avatarOptionCategories = $stmt->fetchAll(PDO::FETCH_ASSOC);
			
            $count = count($avatarOptionCategories);
			
            echo json_encode([
                'count' => $count,
                'results' => $avatarOptionCategories
            ]);
            break;
		

    default:
        echo json_encode(["error" => "Method not allowed"]);
        break;

	}//switch
 
// Close the database connection
$pdo = null;
?>