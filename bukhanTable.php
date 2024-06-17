<!DOCTYPE html>
<html>
<head>
	<meta charset = "UTF-8">
	<meta http-equiv = "refresh" content = "30">
	<style type = "text/css">
		.spec{
			text-align:center;
		}
		.con{
			text-align:left;
		}
		</style>
</head>

<body>
	<h1 align = "center">북한산</h1>
	<div class = "spec">
		# <b>ANIMAL DETECT</b>
		<br></br>
	</div>

	<table border = '1' style = "width = 50%" align = "center">
	<tr align = "center">
		<th>SPECIES</th>
		<th>COUNT</th>
		<th>CAMERA</th>
		<th>TIME</th>
	</tr>

	<?php
		$conn = mysqli_connect("10.10.14.64", "lmg", "1234");
		mysqli_select_db($conn, "wild");
		$result = mysqli_query($conn, "select * from wild_animal2");
		while($row = mysqli_fetch_array($result))
		{
			echo "<tr align = center>";
			echo '<th>'.$row['species'].'</td>';
			echo '<th>'.$row['count'].'</td>';
			echo '<th>'.$row['camera_id'].'</td>';
			echo '<th>'.$row['createdAt'].'</td>';
			echo "</tr>";
		}
		mysqli_close($conn);
	?>
	</table>
</body>
</html>
