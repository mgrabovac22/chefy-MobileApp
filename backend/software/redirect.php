<?php
// Dohvati ID recepta iz GET parametra
$recipeId = $_GET['id'];

// Preusmjeravanje na chefy deep link
header("Location: chefy://recipe/$recipeId");
exit;
?>