<?php
    $conn = mysqli_connect("localhost", "lmg", "1234");
    mysqli_select_db($conn, "wild");

    $query = "SELECT camera_id, species, COUNT(*) AS count FROM wild_animal GROUP BY camera_id, species ORDER BY camera_id, species";
    $result = mysqli_query($conn, $query);

    $data = array();
    $noData = true;

    if($result) {
        while($row = mysqli_fetch_assoc($result)) {
            $camera_id = $row['camera_id'];
            $species = $row['species'];
            $count = 0; 

            $count_result = mysqli_query($conn, "SELECT SUM(count) AS total_count FROM wild_animal WHERE camera_id = '$camera_id' AND species = '$species'");
            $count_row = mysqli_fetch_assoc($count_result);
            $count = intval($count_row['total_count']);

            $data[] = array($camera_id, $species, $count);

            $noData = false;
        }
    }

    mysqli_close($conn);
?>

<!-- Google Chart -->
<script src="//www.google.com/jsapi"></script>
<script>
    var data = <?php echo json_encode($data); ?>;
    google.load('visualization', '1.0', {'packages':['corechart']});

    google.setOnLoadCallback(function() {
        // 데이터가 없는 경우
        if (<?php echo json_encode($noData); ?>) {
            document.getElementById('chart_div').innerHTML = '데이터가 없습니다.';
        } else {
            var chartData = new google.visualization.DataTable();
            chartData.addColumn('string', 'Camera ID'); 

            var speciesList = [];
            for (var i = 0; i < data.length; i++) {
                if (speciesList.indexOf(data[i][1]) === -1) {
                    speciesList.push(data[i][1]);
                    chartData.addColumn('number', data[i][1]);
                }
            }

            var uniqueCameraIDs = [...new Set(data.map(item => item[0]))];
            chartData.addRows(uniqueCameraIDs.length);
            for (var i = 0; i < uniqueCameraIDs.length; i++) {
                chartData.setCell(i, 0, uniqueCameraIDs[i]);
                for (var j = 0; j < speciesList.length; j++) {
                    var count = 0;
                    for (var k = 0; k < data.length; k++) {
                        if (data[k][0] === uniqueCameraIDs[i] && data[k][1] === speciesList[j]) {
                            count = data[k][2];
                            break;
                        }
                    }
                    chartData.setCell(i, j + 1, count);
                }
            }

            var options = {
                title: 'Species Count by Camera ID', // 제목
                width: 800,
                height: 600,
                legend: { position: 'top' },
                hAxis: { title: 'Camera ID' }, // x축 제목
                vAxis: { title: 'Count' }
            };

            var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));
            chart.draw(chartData, options);
        }
    });
</script>

<!-- 그래프를 보여줄 div -->
<div id="chart_div"></div>

<!--데이터 베이스 테이블 -->
<?php
    // 데이터베이스 연결
    $conn = mysqli_connect("10.10.14.64", "lmg", "1234");
    mysqli_select_db($conn, "wild");

    //createdAt 역순으로 정렬
    $query = "SELECT * FROM wild_animal ORDER BY createdAt DESC LIMIT 10";
    $result = mysqli_query($conn, $query);

    // 데이터베이스 연결 해제
    mysqli_close($conn);
?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="refresh" content="30">
    <style type="text/css">
        .spec {
            text-align: center;
        }
        .con {
            text-align: left;
        }
    </style>
</head>

<body>
    <div class="spec">
        # <b>Recently Detected</b>
        <br><br>
    </div>

    <table border="1" style="width: 50%;" align="center">
        <tr align="center">
            <th>SPECIES</th>
            <th>COUNT</th>
            <th>CAMERA</th>
            <th>TIME</th>
        </tr>

        <?php
            // 결과 출력
            while($row = mysqli_fetch_array($result)) {
                echo "<tr align=center>";
                echo '<td>'.$row['species'].'</td>';
                echo '<td>'.$row['count'].'</td>';
                echo '<td>'.$row['camera_id'].'</td>';
                echo '<td>'.$row['createdAt'].'</td>';
                echo "</tr>";
            }
        ?>
    </table>
</body>
</html>

